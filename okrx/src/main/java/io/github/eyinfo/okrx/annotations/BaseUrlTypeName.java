package io.github.eyinfo.okrx.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.eyinfo.okrx.enums.RequestContentType;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2017/6/9
 * Description:全局接口请求配置地址
 * Modifier:
 * ModifyContent:
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseUrlTypeName {

    /**
     * 获取接口请求基地址的标识
     *
     * @return
     */
    int value() default 0;

    /**
     * 接口请求时获取本地登录信息token名称
     *
     * @return
     */
    String tokenName() default "token";

    /**
     * 接口请求参数提交类型(form、json)
     *
     * @return
     */
    RequestContentType contentType() default RequestContentType.None;

    /**
     * api未登录编码标识
     * <p>
     * return
     */
    String[] apiUnloginCodes() default "";
}
