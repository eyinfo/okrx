package io.github.eyinfo.okrx.beans;

import java.io.InputStream;

import io.github.eyinfo.okrx.enums.ResponseDataType;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2019/3/21
 * Description:响应数据
 * Modifier:
 * ModifyContent:
 */
public class ResponseData {

    /**
     * 响应数据类型
     */
    private ResponseDataType responseDataType = ResponseDataType.object;
    /**
     * 内容(json\int\long\double\float)
     */
    private String response = "";
    /**
     * 字节数据
     */
    private byte[] bytes = null;
    /**
     * 流数据
     */
    private InputStream stream = null;

    public ResponseDataType getResponseDataType() {
        if (responseDataType == null) {
            responseDataType = ResponseDataType.object;
        }
        return responseDataType;
    }

    public void setResponseDataType(ResponseDataType responseDataType) {
        this.responseDataType = responseDataType;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public InputStream getStream() {
        return stream;
    }

    public void setStream(InputStream stream) {
        this.stream = stream;
    }
}
