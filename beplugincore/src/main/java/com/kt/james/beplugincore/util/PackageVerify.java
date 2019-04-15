package com.kt.james.beplugincore.util;

import android.content.pm.Signature;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * author: James
 * 2019/4/11 23:51
 * version: 1.0
 */
public class PackageVerify {

    /**
     * Copy from Android SDK
     */
        private static String TAG = "PackageVerifyer";

        private static final Object mSync = new Object();
        private static WeakReference<byte[]> mReadBuffer;

        /**
         * @param sourcePath
         * @param simpleMode
         * @return
         */
        public static Signature[] collectCertificates(String sourcePath, boolean simpleMode) {

            //其实可以直接通过getPackageManager().getPackageArchiveInfo()获取插件的签名信息
            //不用这么麻烦

            Signature mSignatures[] = null;
            WeakReference<byte[]> readBufferRef;
            byte[] readBuffer = null;
            synchronized (mSync) {
                readBufferRef = mReadBuffer;
                if (readBufferRef != null) {
                    mReadBuffer = null;
                    readBuffer = readBufferRef.get();
                }
                if (readBuffer == null) {
                    readBuffer = new byte[8192];
                    readBufferRef = new WeakReference<byte[]>(readBuffer);
                }
            }

            try {
                JarFile jarFile = new JarFile(sourcePath);

                Certificate[] certs = null;

                if (simpleMode) {
                    // if SIMPLE MODE,, then we
                    // can trust it...  we'll just use the AndroidManifest.xml
                    // to retrieve its signatures, not validating all of the
                    // files.
                    JarEntry jarEntry = jarFile.getJarEntry("AndroidManifest.xml");
                    certs = loadCertificates(jarFile, jarEntry, readBuffer);
                    if (certs == null) {
                        jarFile.close();

                        LogUtil.e("INSTALL_PARSE_FAILED_NO_CERTIFICATES");
                        return null;
                    }
                } else {
                    Enumeration entries = jarFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry je = (JarEntry)entries.nextElement();
                        if (je.isDirectory()) continue;
                        if (je.getName().startsWith("META-INF/")) continue;
                        Certificate[] localCerts = loadCertificates(jarFile, je,
                                readBuffer);
                        if (localCerts == null) {
                            jarFile.close();

                            LogUtil.e("INSTALL_PARSE_FAILED_NO_CERTIFICATES");
                            return null;
                        } else if (certs == null) {
                            certs = localCerts;
                        } else {
                            // Ensure all certificates match.
                            for (int i=0; i<certs.length; i++) {
                                boolean found = false;
                                for (int j=0; j<localCerts.length; j++) {
                                    if (certs[i] != null &&
                                            certs[i].equals(localCerts[j])) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found || certs.length != localCerts.length) {
                                    jarFile.close();
                                    LogUtil.e("INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES");
                                    return null;
                                }
                            }
                        }
                    }
                }
                jarFile.close();

                synchronized (mSync) {
                    mReadBuffer = readBufferRef;
                }

                if (certs != null && certs.length > 0) {
                    final int N = certs.length;
                    mSignatures = new Signature[certs.length];
                    for (int i=0; i<N; i++) {
                        mSignatures[i] = new Signature(
                                certs[i].getEncoded());
                    }
                } else {
                    LogUtil.e("INSTALL_PARSE_FAILED_NO_CERTIFICATES");
                    return null;
                }
            } catch (CertificateEncodingException e) {
                LogUtil.e("INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING");
                return null;
            } catch (IOException e) {
                LogUtil.e("INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING");
                return null;
            } catch (RuntimeException e) {
                LogUtil.e("INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION");
                return null;
            }

            return mSignatures;
        }


        private static Certificate[] loadCertificates(JarFile jarFile, JarEntry je,
                                                      byte[] readBuffer) {
            try {
                // We must read the stream for the JarEntry to retrieve
                // its certificates.
                InputStream is = new BufferedInputStream(jarFile.getInputStream(je));
                while (is.read(readBuffer, 0, readBuffer.length) != -1) {
                    // not using
                }
                is.close();
                return je != null ? je.getCertificates() : null;
            } catch (IOException e) {
                LogUtil.printException("PackageVerify.loadCertificates", e);
            } catch (RuntimeException e) {
                LogUtil.printException("PackageVerify.loadCertificates", e);
            }
            return null;
        }

        public static boolean isSignaturesSame(Signature[] s1, Signature[] s2) {
            if (s1 == null) {
                return false;
            }
            if (s2 == null) {
                return false;
            }
            HashSet<Signature> set1 = new HashSet<Signature>();
            for (Signature sig : s1) {
                set1.add(sig);
            }
            HashSet<Signature> set2 = new HashSet<Signature>();
            for (Signature sig : s2) {
                set2.add(sig);
            }
            // Make sure s2 contains all signatures in s1.
            if (set1.equals(set2)) {
                return true;
            }
            return false;
        }

}
