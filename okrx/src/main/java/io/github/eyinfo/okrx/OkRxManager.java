package io.github.eyinfo.okrx;

import com.eyinfo.android_pure_utils.events.Action1;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import io.github.eyinfo.okrx.beans.CompleteBitmapResponse;
import io.github.eyinfo.okrx.beans.CompleteResponse;
import io.github.eyinfo.okrx.beans.RetrofitParams;
import io.github.eyinfo.okrx.beans.SuccessBitmapResponse;
import io.github.eyinfo.okrx.beans.SuccessResponse;
import io.github.eyinfo.okrx.beans.TransParams;
import io.github.eyinfo.okrx.enums.RequestContentType;
import io.github.eyinfo.okrx.enums.RequestState;
import io.github.eyinfo.okrx.properties.ByteRequestItem;
import io.github.eyinfo.okrx.requests.OkRxBitmapRequest;
import io.github.eyinfo.okrx.requests.OkRxDownloadFileRequest;
import io.github.eyinfo.okrx.requests.OkRxRequest;
import io.github.eyinfo.okrx.requests.OkRxUploadByteRequest;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2017/11/15
 * Description:网络请求类
 * Modifier:
 * ModifyContent:
 */
public class OkRxManager {

    private static OkRxManager okRxManager = null;

    public static OkRxManager getInstance() {
        return okRxManager == null ? okRxManager = new OkRxManager() : okRxManager;
    }

    public void request(TransParams transParams,
                        Action1<SuccessResponse> successAction,
                        Action1<CompleteResponse> completeAction) {
        OkRxRequest request = new OkRxRequest();
        request.call(transParams, successAction, completeAction);
    }

    public void request(String url,
                        HashMap<String, String> headers,
                        RetrofitParams retrofitParams,
                        RequestContentType requestContentType,
                        String useClass,
                        Action1<SuccessResponse> successAction,
                        Action1<CompleteResponse> completeAction) {
        TransParams transParams = new TransParams();
        transParams.setUrl(url);
        transParams.setHeaders(headers);
        if (retrofitParams == null) {
            retrofitParams = new RetrofitParams();
        }
        retrofitParams.setRequestContentType(requestContentType);
        transParams.setRetrofitParams(retrofitParams);
        transParams.setUseClass(useClass);
        this.request(transParams, successAction, completeAction);
    }

    public void download(String url,
                         HashMap<String, String> headers,
                         TreeMap<String, Object> params,
                         File downFile,
                         Action1<Float> progressAction,
                         Action1<File> successAction,
                         Action1<RequestState> completeAction) {
        OkRxDownloadFileRequest request = new OkRxDownloadFileRequest();
        request.call(url, headers, params, downFile, progressAction, successAction, completeAction);
    }

    public void uploadBytes(String url,
                            HashMap<String, String> httpHeaders,
                            HashMap<String, Object> httpParams,
                            List<ByteRequestItem> byteRequestItems,
                            RetrofitParams retrofitParams,
                            Action1<SuccessResponse> successAction,
                            Action1<CompleteResponse> completeAction) {
        OkRxUploadByteRequest request = new OkRxUploadByteRequest();
        request.setRetrofitParams(retrofitParams);
        request.call(url, httpHeaders, httpParams, byteRequestItems, successAction, completeAction);
    }

    public void uploadByte(String url,
                           HashMap<String, String> httpHeaders,
                           HashMap<String, Object> httpParams,
                           ByteRequestItem byteRequestItem,
                           RetrofitParams retrofitParams,
                           Action1<SuccessResponse> successAction,
                           Action1<CompleteResponse> completeAction) {
        List<ByteRequestItem> byteRequestItems = new ArrayList<ByteRequestItem>();
        byteRequestItems.add(byteRequestItem);
        uploadBytes(url, httpHeaders, httpParams, byteRequestItems, retrofitParams, successAction, completeAction);
    }

    public void getBitmap(String url, Action1<SuccessBitmapResponse> successAction, Action1<CompleteBitmapResponse> completeAction) {
        OkRxBitmapRequest request = new OkRxBitmapRequest();
        request.call(url, successAction, completeAction);
    }
}
