package io.github.eyinfo.okrx.requests;

import android.text.TextUtils;

import com.eyinfo.android_pure_utils.events.Action1;

import io.github.eyinfo.okrx.OkRx;
import io.github.eyinfo.okrx.beans.CompleteBitmapResponse;
import io.github.eyinfo.okrx.beans.RetrofitParams;
import io.github.eyinfo.okrx.beans.SuccessBitmapResponse;
import io.github.eyinfo.okrx.callback.BitmapCallback;
import io.github.eyinfo.okrx.enums.RequestState;
import io.github.eyinfo.okrx.enums.RequestType;
import io.github.eyinfo.okrx.events.OnNetworkConnectListener;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2019-09-29
 * Description:
 * Modifier:
 * ModifyContent:
 */
public class OkRxBitmapRequest extends BaseRequest {

    public void call(String url, Action1<SuccessBitmapResponse> successAction, Action1<CompleteBitmapResponse> completeAction) {
        if (TextUtils.isEmpty(url)) {
            if (completeAction != null) {
                completeAction.call(new CompleteBitmapResponse(RequestState.Completed));
            }
            return;
        }
        if (successAction == null) {
            if (completeAction != null) {
                completeAction.call(new CompleteBitmapResponse(RequestState.Completed));
            }
            return;
        }
        //如果网络未连接则不作请求
        OnNetworkConnectListener onNetworkConnectListener = OkRx.getInstance().getOnNetworkConnectListener();
        if (completeAction != null && onNetworkConnectListener != null && !onNetworkConnectListener.isConnected()) {
            completeAction.call(new CompleteBitmapResponse(RequestState.Completed));
            return;
        }
        RetrofitParams retrofitParams = new RetrofitParams();
        retrofitParams.setRequestType(RequestType.GET);
        Request.Builder builder = getBuilder(url, null, retrofitParams);
        if (builder == null) {
            completeAction.call(new CompleteBitmapResponse(RequestState.Completed));
            return;
        }
        Request request = builder.build();
        OkHttpClient client = OkRx.getInstance().getOkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new BitmapCallback(successAction, completeAction));
    }
}
