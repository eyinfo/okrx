package io.github.eyinfo.okrx.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2017/11/1
 * Description:url相对路径
 * Modifier:
 * ModifyContent:
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlItem {
    /**
     * 相对路径
     *
     * return
     */
    String value() default "";

    /**
     * 相对路径key
     *
     * return
     */
    String key() default "";
}
