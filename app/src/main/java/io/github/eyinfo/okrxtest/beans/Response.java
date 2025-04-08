package io.github.eyinfo.okrxtest.beans;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private int code;
    private String msg;

    private T data;
}
