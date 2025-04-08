package io.github.eyinfo.okrxtest.apis;

import io.github.eyinfo.okrx.events.OnRequestApiUrl;

public class BaseUrlConfig implements OnRequestApiUrl {
    @Override
    public String onBaseUrl(Integer apiUrlTypeName) {
        return "http://geease.cn:11000/mock/11";
    }
}
