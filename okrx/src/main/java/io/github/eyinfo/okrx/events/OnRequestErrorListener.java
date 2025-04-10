package io.github.eyinfo.okrx.events;

import io.github.eyinfo.okrx.beans.RequestErrorInfo;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2019/3/6
 * Description:网络请求失败监听
 * Modifier:
 * ModifyContent:
 */
public interface OnRequestErrorListener {

    /**
     * 网络请求失败
     *
     * @param errorInfo 网络请求错误信息
     */
    public void onFailure(RequestErrorInfo errorInfo);
}
