package io.github.eyinfo.okrx.requests;

import android.text.TextUtils;

import com.eyinfo.android_pure_utils.events.Action1;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import io.github.eyinfo.okrx.OkRx;
import io.github.eyinfo.okrx.beans.CompleteResponse;
import io.github.eyinfo.okrx.beans.ResponseData;
import io.github.eyinfo.okrx.beans.RetrofitParams;
import io.github.eyinfo.okrx.beans.SuccessResponse;
import io.github.eyinfo.okrx.beans.TransParams;
import io.github.eyinfo.okrx.cache.NetCacheManager;
import io.github.eyinfo.okrx.callback.StringCallback;
import io.github.eyinfo.okrx.enums.CallStatus;
import io.github.eyinfo.okrx.enums.ErrorType;
import io.github.eyinfo.okrx.enums.RequestState;
import io.github.eyinfo.okrx.enums.ResponseDataType;
import io.github.eyinfo.okrx.events.OnNetworkConnectListener;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.Timeout;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2017/11/15
 * Description:
 * Modifier:
 * ModifyContent:
 */
public class OkRxRequest extends BaseRequest {

    public void call(TransParams transParams, Action1<SuccessResponse> successAction, Action1<CompleteResponse> completeAction) {
        if (TextUtils.isEmpty(transParams.getUrl())) {
            if (completeAction != null) {
                completeAction.call(new CompleteResponse(RequestState.Completed, ErrorType.none, 0));
            }
            return;
        }
        setCancelIntervalCacheCall(false);
        RetrofitParams retrofitParams = transParams.getRetrofitParams();
        CallStatus callStatus = retrofitParams.getCallStatus();
        String ckey = String.format("%s%s", retrofitParams.getCacheKey(), getAllParamsJoin(transParams.getHeaders(), retrofitParams));
        if (!cacheDealWith(callStatus, successAction, ckey, retrofitParams)) {
            //此时结束处理
            return;
        }
        //如果网络未连接则不作请求
        OnNetworkConnectListener onNetworkConnectListener = OkRx.getInstance().getOnNetworkConnectListener();
        if (completeAction != null && onNetworkConnectListener != null && !onNetworkConnectListener.isConnected()) {
            completeAction.call(new CompleteResponse(RequestState.Error, ErrorType.netRequest, 0));

            completeAction.call(new CompleteResponse(RequestState.Completed, ErrorType.none, 0));
            return;
        }
        Request.Builder builder = getBuilder(transParams.getUrl(), transParams.getHeaders(), retrofitParams);
        if (builder == null) {
            completeAction.call(new CompleteResponse(RequestState.Completed, ErrorType.businessProcess, 0));
            return;
        }
        Request request = builder.build();
        OkHttpClient client = OkRx.getInstance().getOkHttpClient();
        StringCallback callback = new StringCallback(successAction, completeAction) {
            @Override
            protected void onSuccessCall(ResponseData responseData, RetrofitParams retrofitParams, HashMap<String, String> headers) {
                ResponseDataType responseDataType = responseData.getResponseDataType();
                if (responseDataType != ResponseDataType.object) {
                    return;
                }
                CallStatus callStatus = retrofitParams.getCallStatus();
                if (callStatus != CallStatus.OnlyNet && !TextUtils.isEmpty(retrofitParams.getCacheKey())) {
                    String ckey = String.format("%s%s", retrofitParams.getCacheKey(), getAllParamsJoin(headers, retrofitParams));
                    String mkey = String.format("%s_%s", String.valueOf(ckey.hashCode()), retrofitParams.getCacheKey());
                    NetCacheManager.getInstance().cacheRequestData(mkey, responseData.getResponse(), retrofitParams.getCacheTime());
                }
            }
        };
        callback.setRequestKey(ckey);
        callback.setHeaders(retrofitParams.getHeadParams());
        callback.setRetrofitParams(retrofitParams);
        callback.setCancelIntervalCacheCall(isCancelIntervalCacheCall());
        //数据类型
        callback.setDataClass(retrofitParams.getDataClass());
        callback.setCallStatus(callStatus);
        callback.setResponseDataType(retrofitParams.getResponseDataType());
        //请求失败后是否重试
        callback.setFailureRetry(retrofitParams.isFailureRetry());
        callback.setFailureRetryCount(retrofitParams.getFailureRetryCount());
        //设置请求开始时间
        callback.setRequestStartTime(System.currentTimeMillis());
        //绑定cookies
        bindCookies(client, request.url());
        Call call = client.newCall(request);
        //设置当前请求超时时间
        long timeoutMillis = retrofitParams.getTimeoutMillis();
        if (timeoutMillis > 0) {
            Timeout timeout = call.timeout();
            timeout.timeout(timeoutMillis, TimeUnit.MILLISECONDS);
        }
        //请求网络
        call.enqueue(callback);
    }
}
