package io.github.eyinfo.okrx.beans;

public class FlutterDataResponse {

    //返回类型
    private String type;

    private String data;

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data == null ? "" : data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
