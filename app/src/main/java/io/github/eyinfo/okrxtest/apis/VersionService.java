package io.github.eyinfo.okrxtest.apis;

import com.eyinfo.android_pure_utils.events.Func2;

import java.util.HashMap;

import io.github.eyinfo.okrx.BaseOkRxService;
import io.github.eyinfo.okrx.BaseSubscriber;
import io.github.eyinfo.okrx.annotations.APIUrlInterfaceClass;
import io.github.eyinfo.okrx.annotations.ApiCheckAnnotation;
import io.github.eyinfo.okrx.beans.RetrofitParams;
import io.github.eyinfo.okrx.enums.DataType;
import io.github.eyinfo.okrx.events.OnSuccessfulListener;
import io.github.eyinfo.okrxtest.beans.VersionBody;

@APIUrlInterfaceClass(BaseUrlConfig.class)
public class VersionService extends BaseOkRxService {

    @ApiCheckAnnotation
    public void requestVersion() {
        BaseSubscriber<VersionBody, VersionService> subscriber = new BaseSubscriber<>(this);
        subscriber.setOnSuccessfulListener(new OnSuccessfulListener<VersionBody>() {
            @Override
            public void onSuccessful(VersionBody versionBody, DataType dataType, Object... extras) {
                VersionBody data = versionBody.getData();
            }
        });
        requestObject(IVersionAPI.class, this, subscriber, new Func2<RetrofitParams, IVersionAPI, HashMap<String, Object>>() {
            @Override
            public RetrofitParams call(IVersionAPI versionAPI, HashMap<String, Object> stringObjectHashMap) {
                return versionAPI.requestVersion();
            }
        });
    }
}
