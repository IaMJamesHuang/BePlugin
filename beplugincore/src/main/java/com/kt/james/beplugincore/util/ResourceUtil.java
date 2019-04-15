package com.kt.james.beplugincore.util;

/**
 * author: James
 * 2019/4/11 22:20
 * version: 1.0
 */
public class ResourceUtil {

    public static String converToHex(String resId) {
        return resId;
    }

    public static int parseResId(String value) {
        String idHex = null;
        if (value != null && value.contains(":")) {
            idHex = value.split(":")[1];
        } else if (value != null && value.startsWith("@") && value.length() == 9) {
            idHex = value.replace("@", "");
        }
        if (idHex != null) {
            try {
                int id = (int)Long.parseLong(idHex, 16);
                return id;
            } catch (Exception e) {
                LogUtil.printException("ResourceUtil.parseResId", e);
            }
        }
        return 0;
    }

}
