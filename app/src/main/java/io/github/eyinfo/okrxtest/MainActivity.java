package io.github.eyinfo.okrxtest;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.eyinfo.android_pure_utils.events.Action1;
import com.eyinfo.android_pure_utils.utils.JsonUtils;

import io.github.eyinfo.okrx.OkRx;
import io.github.eyinfo.okrx.OkRxManager;
import io.github.eyinfo.okrx.beans.ResponseData;
import io.github.eyinfo.okrx.beans.SuccessResponse;
import io.github.eyinfo.okrx.enums.RequestContentType;
import io.github.eyinfo.okrx.events.OnBeanParsingJsonListener;

public class MainActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        OkRx.getInstance().initialize(this)
                .setOnBeanParsingJsonListener(new OnBeanParsingJsonListener() {
                    @Override
                    public Object onBeanParsingJson(String response, Class dataClass, boolean isCollectionDataType) {
                        if (isCollectionDataType) {
                            return JsonUtils.parseArray(response, dataClass);
                        } else {
                            return JsonUtils.parseT(response, dataClass);
                        }
                    }
                })
                .build();

        requestTest();
    }

    private void requestTest() {
        try {
            String url = "http://geease.cn:11000/mock/11/version/update";
            OkRxManager.getInstance().request(url, null, null, RequestContentType.None, null, new Action1<SuccessResponse>() {
                @Override
                public void call(SuccessResponse successResponse) {
                    ResponseData responseData = successResponse.getResponseData();
                    String response = responseData.getResponse();
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
