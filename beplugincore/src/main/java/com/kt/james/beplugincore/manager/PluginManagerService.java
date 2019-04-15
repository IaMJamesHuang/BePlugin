package com.kt.james.beplugincore.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import com.kt.james.beplugincore.BePluginGlobal;
import com.kt.james.beplugincore.content.InstallResult;
import com.kt.james.beplugincore.PluginCreator;
import com.kt.james.beplugincore.content.PluginInfo;
import com.kt.james.beplugincore.PluginManifestParser;
import com.kt.james.beplugincore.constant.ResultCodeConstant;
import com.kt.james.beplugincore.util.FileUtil;
import com.kt.james.beplugincore.util.LogUtil;
import com.kt.james.beplugincore.util.PackageVerify;
import com.kt.james.beplugincore.util.ProcessUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * author: James
 * 2019/4/11 0:32
 * version: 1.0
 */
public class PluginManagerService {

    private static final String INSTALLED_SP_KEY = "plugins.list";

    private static final String INSTALLED_SP_DIR = "plugins.install";

    private final ConcurrentHashMap<String, PluginInfo> sInstallPlugins = new ConcurrentHashMap<>();

    public PluginManagerService() {
        if (!ProcessUtil.isPluginProcess()) {
            throw new IllegalStateException("此类只用于插件进程");
        }
    }

    public PluginInfo getPluginInfo(String packageName) {
        return sInstallPlugins.get(packageName);
    }

    public PluginInfo getPluginInfoByClazzName(String clazzName) {
        Iterator<Map.Entry<String, PluginInfo>> iterator = sInstallPlugins.entrySet().iterator();
        while (iterator.hasNext()) {
            PluginInfo pluginInfo = iterator.next().getValue();
            if (pluginInfo.containsClazzName(clazzName)) {
                return pluginInfo;
            }
        }
        return null;
    }

    public ArrayList<PluginInfo> getAllPlugins() {
        Iterator<Map.Entry<String, PluginInfo>> iterator = sInstallPlugins.entrySet().iterator();
        ArrayList<PluginInfo> list = null;
        while (iterator.hasNext()) {
            if (list == null) {
                list = new ArrayList<>();
            }
            PluginInfo pluginInfo = iterator.next().getValue();
            list.add(pluginInfo);
        }
        return list;
    }

    public synchronized InstallResult installPlugin(String src) {
        LogUtil.w("开始安装插件，插件路径：" + src);
        long start = System.currentTimeMillis();
        if (!FileUtil.checkPath(src)) {
            return new InstallResult(ResultCodeConstant.SRC_FILE_NOT_FOUND);
        }

        File srcFile = new File(src);
        if (!srcFile.exists() || !srcFile.isFile()) {
            return new InstallResult(ResultCodeConstant.SRC_FILE_NOT_FOUND);
        }

        try {
            src = srcFile.getCanonicalPath();
        } catch (IOException e) {
            LogUtil.printException("PluginManagerService.installPlugin", e);
            return new InstallResult(ResultCodeConstant.INSTALL_FAIL);
        }

        //将apk复制到应用的私用目录下，防止安装过程中被篡改
        if (!src.startsWith(BePluginGlobal.getHostApplication().getCacheDir().getAbsolutePath())) {
            String tempPath = BePluginGlobal.getHostApplication().getCacheDir().getAbsolutePath()
                    + File.separator + System.currentTimeMillis() + "_" + srcFile.getName();
            if (FileUtil.copyFile(src, tempPath)) {
                src = tempPath;
            } else {
                LogUtil.e("复制apk到私用目录失败");
                new File(tempPath).delete();
                return new InstallResult(ResultCodeConstant.COPY_FILE_FAIL);
            }
        }

        //解析AndroidManifest.xml的信息
        PluginInfo pluginInfo = PluginManifestParser.parserPluginManifest(src);
        if (pluginInfo == null) {
            LogUtil.e("解析AndroidManifest.xml文件失败");
            new File(src).delete();
            return new InstallResult(ResultCodeConstant.PARSE_MANIFEST_FAIL);
        }

        //判断插件适用系统版本
        if (pluginInfo.getMinSdkVersion() != null && Build.VERSION.SDK_INT < Integer.valueOf(pluginInfo.getMinSdkVersion())) {
            LogUtil.e("系统的SDK小于插件支持的最小SDK: " + src);
            new File(src).delete();
            return new InstallResult(ResultCodeConstant.MIN_API_NOT_SUPPORTED, pluginInfo.getPackageName(), pluginInfo.getVersionName());
        }

        //包签名验证
        // 验证插件APK签名，如果被篡改过，将获取不到证书
        // 之所以把验证签名步骤在放在验证适用系统版本之后，
        // 是因为不同的minSdkVersion在签名时使用的sha算法长度不同，
        // 也即高版本的minSdkVersion的插件，即使签名没有被篡改过，在低版本的系统中仍然会校验失败
        // 所以先校验minSdkVersion，再校验签名
        Signature[] pluginSignatures = PackageVerify.collectCertificates(src, false);
        if (pluginSignatures == null) {
            LogUtil.e("APK证书校验失败 " + src);
            new File(src).delete();
            return new InstallResult(ResultCodeConstant.SIGNATURES_INVALIDATE);
        }

        //检查当前宿主版本是否匹配当前非独立插件的版本
        PackageManager packageManager = BePluginGlobal.getHostApplication().getPackageManager();
        String requireHostVersionName = pluginInfo.getRequiredHostVersionName();
        if (!pluginInfo.isStandalone() && !TextUtils.isEmpty(requireHostVersionName)) {
            try {
                PackageInfo hostInfo = packageManager.getPackageInfo(BePluginGlobal.getHostApplication().getPackageName(), PackageManager.GET_META_DATA);
                if (!requireHostVersionName.equals(hostInfo.versionName)) {
                    LogUtil.e("当前宿主版本不匹配插件指定的宿主版本 " + src);
                    new File(src).delete();
                    return new InstallResult(ResultCodeConstant.HOST_VERSION_NOT_SUPPORT_CURRENT_PLUGIN, pluginInfo.getPackageName(), pluginInfo.getVersionName());
                }
            } catch (PackageManager.NameNotFoundException e) {
                LogUtil.printException("PluginManagerService.installPlugin", e);
            }
        }

        //检查插件是否存在
        PluginInfo oldPluginInfo = getPluginInfo(pluginInfo.getPackageName());
        if (oldPluginInfo != null) {
            LogUtil.d("该插件已经安装过", oldPluginInfo.getPackageName(), oldPluginInfo.getInstallPath(), oldPluginInfo.getVersionName());
            //卸载、热更新逻辑
        }

        //复制插件apk到插件目录
        String destApkPath = generateInstallPath(pluginInfo.getPackageName(), pluginInfo.getVersionName());
        if (!FileUtil.copyFile(src, destApkPath)) {
            LogUtil.e("复制插件APK到插件目录失败", pluginInfo.getPackageName());
            new File(src).delete();
            return new InstallResult(ResultCodeConstant.COPY_FILE_FAIL, pluginInfo.getPackageName(), pluginInfo.getVersionName());
        }

        //删掉临时文件
        new File(src).delete();

        //添加到已安装插件
        pluginInfo.setInstallPath(destApkPath);
        pluginInfo.setInstallationTime(System.currentTimeMillis());
        PackageInfo info = pluginInfo.getPackageInfo(PackageManager.GET_GIDS);
        if (info != null) {
            pluginInfo.setApplicationTheme(info.applicationInfo.theme);
            pluginInfo.setApplicationIcon(info.applicationInfo.icon);
            pluginInfo.setApplicationLogo(info.applicationInfo.logo);
        }
        if (!updateInstallPluginMap(pluginInfo)) {
            LogUtil.e("安装插件失败 ", src);
            new File(destApkPath).delete();
            return new InstallResult(ResultCodeConstant.INSTALL_FAIL, pluginInfo.getPackageName(), pluginInfo.getVersionName());
        }

        //通过创建classloader来触发dexopt
        LogUtil.d("正在进行dexopt", pluginInfo.getInstallPath());
        File apkParent = new File(destApkPath).getParentFile();
        //清除上一次dexopt产生的文件
        FileUtil.deleteDir(new File(apkParent, "dalvik-cache"));
        ClassLoader classLoader = PluginCreator.createPluginClassLoader(destApkPath, pluginInfo.isStandalone());
        //触发dexopt
        try {
            classLoader.loadClass(Object.class.getName());
        } catch (ClassNotFoundException e) {
            LogUtil.printException("PluginManagerService.installPlugin", e);
        }
        LogUtil.d("dexopt完毕");

        long end = System.currentTimeMillis();
        LogUtil.d("插件安装完成，耗时：" + end);
        LogUtil.d("安装路径", pluginInfo.getInstallPath());

        return new InstallResult(ResultCodeConstant.INSTALL_SUCCESS, pluginInfo.getPackageName(), pluginInfo.getVersionName());
    }

    public synchronized void loadInstalledPlugins() {
        if (sInstallPlugins.size() == 0) {
            ConcurrentHashMap<String, PluginInfo> map = readPlugins(INSTALLED_SP_KEY);
            if (map != null) {
                sInstallPlugins.putAll(map);
            }
        }
    }

    private String generateInstallPath(String packageName, String version) {
        if (packageName.indexOf(File.separatorChar) >= 0 || version.indexOf(File.separatorChar) >= 0) {
            throw new IllegalArgumentException("path contains a path separator");
        }
        return getPluginRootDir() + File.separator + packageName + File.separator + version + File.separator + "plugin.apk";
    }

    private String getPluginRootDir() {
        return BePluginGlobal.getHostApplication().getDir("plugin_dir", Context.MODE_PRIVATE).getAbsolutePath();
    }

    private boolean updateInstallPluginMap(PluginInfo pluginInfo) {
        sInstallPlugins.put(pluginInfo.getPackageName(), pluginInfo);
        boolean result = savePlugins(INSTALLED_SP_KEY, sInstallPlugins);
        if (!result) {
            sInstallPlugins.remove(pluginInfo.getPackageName());
        }
        return result;
    }

    private boolean savePlugins(String key, ConcurrentHashMap<String, PluginInfo> plugins) {

        ObjectOutputStream objectOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(plugins);
            objectOutputStream.flush();

            byte[] data = byteArrayOutputStream.toByteArray();
            String list = Base64.encodeToString(data, Base64.DEFAULT);

            getSharedPreference().edit().putString(key, list).apply();
            return true;
        } catch (Exception e) {
            LogUtil.printException("PluginManagerService.savePlugins", e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    LogUtil.printException("PluginManagerService.savePlugins", e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    LogUtil.printException("PluginManagerService.savePlugins", e);
                }
            }
        }
        return false;
    }

    private ConcurrentHashMap<String, PluginInfo> readPlugins(String key) {
        String list = getSharedPreference().getString(key, "");
        Serializable object = null;
        if (!TextUtils.isEmpty(list)) {
            ByteArrayInputStream byteArrayInputStream = null;
            ObjectInputStream objectInputStream = null;
            try {
                byteArrayInputStream = new ByteArrayInputStream(Base64.decode(list, Base64.DEFAULT));
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
                object = (Serializable) objectInputStream.readObject();
            } catch (Exception e) {
                LogUtil.printException("PluginManager.readPlugins", e);
            } finally {
                if (objectInputStream != null) {
                    try {
                        objectInputStream.close();
                    } catch (IOException e) {
                        LogUtil.printException("PluginManagerService.readPlugins", e);
                    }
                }
                if (byteArrayInputStream != null) {
                    try {
                        byteArrayInputStream.close();
                    } catch (IOException e) {
                        LogUtil.printException("PluginManagerService.readPlugins", e);
                    }
                }
            }
        }
        return (ConcurrentHashMap<String, PluginInfo>) object;
    }

    private SharedPreferences getSharedPreference() {
        return BePluginGlobal.getHostApplication().getSharedPreferences(INSTALLED_SP_DIR,
                Context.MODE_PRIVATE);
    }

}
