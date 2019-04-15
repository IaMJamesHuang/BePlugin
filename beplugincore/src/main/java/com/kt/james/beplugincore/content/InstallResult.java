package com.kt.james.beplugincore.content;

import java.io.Serializable;

/**
 * author: James
 * 2019/4/11 15:34
 * version: 1.0
 */
public class InstallResult implements Serializable {

    private int result;

    private String packageName;

    private String version;

    public InstallResult(int result) {
        this.result = result;
    }

    public InstallResult(int result, String packageName, String version) {
        this.result = result;
        this.packageName = packageName;
        this.version = version;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
