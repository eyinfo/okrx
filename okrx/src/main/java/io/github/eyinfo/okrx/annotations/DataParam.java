package io.github.eyinfo.okrx.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.github.eyinfo.okrx.enums.ResponseDataType;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2017/6/6
 * Description:
 * Modifier:
 * ModifyContent:
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataParam {
    /**
     * 数据返回类型
     *
     * @return
     */
    Class value() default Class.class;

    /**
     * 数据返回描述
     *
     * @return
     */
    String description() default "";

    /**
     * 当子类没有属性时，设置此属性后；结果返回后可自动对相应的属性进行设置;
     *
     * @return
     */
    DataPropertyItem[] properties() default {};

    /**
     * true-集合(示例List<?>);false-对象;
     *
     * @return 默认false
     */
    boolean isCollection() default false;

    /**
     * 响应数据类型
     * (默认ResponseDataType.object[实体对象\string\int\double\float\long])
     * 若需返回字节或流可通过此参数指定(框架内部原因不能通过value()指定类型来区分)
     *
     * @return ResponseDataType
     */
    ResponseDataType responseDataType() default ResponseDataType.object;

    /**
     * 解析字段映射
     *
     * @return field1->field2...
     */
    String parsingFieldMapping() default "";
}
