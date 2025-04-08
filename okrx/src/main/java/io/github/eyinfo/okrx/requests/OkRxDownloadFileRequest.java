package io.github.eyinfo.okrx.requests;

import android.text.TextUtils;

import com.eyinfo.android_pure_utils.events.Action1;
import com.eyinfo.android_pure_utils.utils.ObjectJudge;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

import io.github.eyinfo.okrx.OkRx;
import io.github.eyinfo.okrx.beans.RetrofitParams;
import io.github.eyinfo.okrx.callback.FileCallback;
import io.github.eyinfo.okrx.enums.RequestState;
import io.github.eyinfo.okrx.enums.RequestType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2017/11/15
 * Description:
 * Modifier:
 * ModifyContent:
 */
public class OkRxDownloadFileRequest extends BaseRequest {

    public void call(String url,
                     HashMap<String, String> headers,
                     TreeMap<String, Object> params,
                     File downFile,
                     Action1<Float> progressAction,
                     Action1<File> successAction,
                     Action1<RequestState> completeAction) {
        if (TextUtils.isEmpty(url) || downFile == null || !downFile.exists()) {
            if (completeAction != null) {
                completeAction.call(RequestState.Completed);
            }
            return;
        }
        RetrofitParams retrofitParams = new RetrofitParams();
        retrofitParams.setRequestType(RequestType.GET);
        if (!ObjectJudge.isNullOrEmpty(params)) {
            retrofitParams.getParams().putAll(params);
        }
        Request.Builder builder = getBuilder(url, headers, retrofitParams).get();
        Request request = builder.build();
        OkHttpClient client = OkRx.getInstance().getOkHttpClient();
        //绑定cookies
        bindCookies(client, request.url());
        //请求网络
        client.newCall(request).enqueue(new FileCallback(downFile, progressAction, successAction, completeAction));
    }
}
