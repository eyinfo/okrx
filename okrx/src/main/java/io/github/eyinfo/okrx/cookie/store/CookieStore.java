package io.github.eyinfo.okrx.cookie.store;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2018/9/29
 * Description:
 * Modifier:
 * ModifyContent:
 */
public interface CookieStore {
    /**
     * 保存url对应所有cookie
     */
    void saveCookie(HttpUrl url, List<Cookie> cookie);

    /**
     * 保存url对应所有cookie
     */
    void saveCookie(HttpUrl url, Cookie cookie);

    /**
     * 加载url所有的cookie
     */
    List<Cookie> loadCookie(HttpUrl url);

    /**
     * 获取当前所有保存的cookie
     */
    List<Cookie> getAllCookie();

    /**
     * 获取当前url对应的所有的cookie
     */
    List<Cookie> getCookie(HttpUrl url);

    /**
     * 根据url和cookie移除对应的cookie
     */
    boolean removeCookie(HttpUrl url, Cookie cookie);

    /**
     * 根据url移除所有的cookie
     */
    boolean removeCookie(HttpUrl url);

    /**
     * 移除所有的cookie
     */
    boolean removeAllCookie();
}
