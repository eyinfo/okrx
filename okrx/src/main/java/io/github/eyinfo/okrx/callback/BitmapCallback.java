package io.github.eyinfo.okrx.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eyinfo.android_pure_utils.events.Action1;
import com.eyinfo.android_pure_utils.logs.Logger;

import java.io.IOException;
import java.io.InputStream;

import io.github.eyinfo.okrx.RequestCodeUtils;
import io.github.eyinfo.okrx.beans.CompleteBitmapResponse;
import io.github.eyinfo.okrx.beans.SuccessBitmapResponse;
import io.github.eyinfo.okrx.enums.RequestState;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2019-09-29
 * Description:
 * Modifier:
 * ModifyContent:
 */
public class BitmapCallback implements Callback {

    private Action1<SuccessBitmapResponse> successAction;
    private Action1<CompleteBitmapResponse> completeAction;

    public BitmapCallback(Action1<SuccessBitmapResponse> successAction,
                          Action1<CompleteBitmapResponse> completeAction) {
        this.successAction = successAction;
        this.completeAction = completeAction;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        if (completeAction == null) {
            return;
        }
        int code = RequestCodeUtils.getCodeByError(e.getMessage());
        completeAction.call(new CompleteBitmapResponse(RequestState.Error, code));
        completeAction.call(new CompleteBitmapResponse(RequestState.Completed, code));
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        int code = response.code();
        try {
            if (!response.isSuccessful()) {
                if (completeAction != null) {
                    completeAction.call(new CompleteBitmapResponse(RequestState.Error, code));
                }
            } else {
                ResponseBody body = response.body();
                if (body == null) {
                    if (completeAction != null) {
                        completeAction.call(new CompleteBitmapResponse(RequestState.Error, code));
                    }
                } else {
                    if (successAction != null) {
                        InputStream stream = body.byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(stream);
                        SuccessBitmapResponse bitmapResponse = new SuccessBitmapResponse();
                        bitmapResponse.setBitmap(bitmap);
                        bitmapResponse.setCode(code);
                        successAction.call(bitmapResponse);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        } finally {
            if (completeAction != null) {
                completeAction.call(new CompleteBitmapResponse(RequestState.Completed, code));
            }
        }
    }
}
