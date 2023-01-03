package com.ngyewkong.springbatchdemo.request;

public class JobParamsRequest {

    // set up the parameter key:value pair
    private String paramKey;

    private String paramValue;

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }
}
