package io.github.eyinfo.okrxtest.apis;

import io.github.eyinfo.okrx.annotations.BaseUrlTypeName;
import io.github.eyinfo.okrx.annotations.DataParam;
import io.github.eyinfo.okrx.annotations.GET;
import io.github.eyinfo.okrx.beans.RetrofitParams;
import io.github.eyinfo.okrx.enums.RequestContentType;
import io.github.eyinfo.okrxtest.beans.VersionBody;

@BaseUrlTypeName(value = ApiCodes.normal, contentType = RequestContentType.Json)
public interface IVersionAPI {
    @GET(value = "/version/update")
    @DataParam(value = VersionBody.class)
    RetrofitParams requestVersion();
}
