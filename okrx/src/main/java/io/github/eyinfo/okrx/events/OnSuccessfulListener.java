package io.github.eyinfo.okrx.events;

import io.github.eyinfo.okrx.enums.DataType;
import io.github.eyinfo.okrx.enums.ErrorType;

/**
 * @Author lijinghuan
 * @Email:ljh0576123@163.com
 * @CreateTime:2018/1/22
 * @Description:
 * @Modifier:
 * @ModifyContent:
 */
public abstract class OnSuccessfulListener<T> {
    /**
     * http请求状态码
     */
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 接口回调
     *
     * @param t        返回数据对象
     * @param dataType 返回数据类型
     * @param extras   扩展参数
     */
    public abstract void onSuccessful(T t, DataType dataType, Object... extras);

    /**
     * 请求失败回调
     *
     * @param t         数据
     * @param errorType 错误类型
     * @param extras    扩展参数
     */
    public void onError(T t, ErrorType errorType, Object... extras) {
        //失败回调
    }

    /**
     * 请求失败回调
     *
     * @param errorType 错误类型
     * @param extras    扩展参数
     */
    public void onError(ErrorType errorType, Object... extras) {
        //失败回调
    }

    /**
     * 请求完成回调
     *
     * @param extras 扩展参数
     */
    public void onCompleted(Object... extras) {
        //完成回调
    }
}
