package io.github.eyinfo.okrx;

import android.content.Context;
import android.text.TextUtils;

import com.eyinfo.android_pure_utils.events.OnEntryCall;
import com.eyinfo.android_pure_utils.observable.BaseObservable;
import com.eyinfo.android_pure_utils.observable.call.OnSubscribeConsumer;
import com.eyinfo.android_pure_utils.utils.JsonUtils;
import com.eyinfo.android_pure_utils.utils.ObjectJudge;
import com.eyinfo.mpkv.MPLocalKV;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.github.eyinfo.okrx.cookie.CookieJarImpl;
import io.github.eyinfo.okrx.cookie.store.CookieStore;
import io.github.eyinfo.okrx.cookie.store.SPCookieStore;
import io.github.eyinfo.okrx.events.OnAuthListener;
import io.github.eyinfo.okrx.events.OnBeanParsingJsonListener;
import io.github.eyinfo.okrx.events.OnConfigParamsListener;
import io.github.eyinfo.okrx.events.OnGlobalRequestParamsListener;
import io.github.eyinfo.okrx.events.OnGlobalReuqestHeaderListener;
import io.github.eyinfo.okrx.events.OnHeaderCookiesListener;
import io.github.eyinfo.okrx.events.OnNetworkConnectListener;
import io.github.eyinfo.okrx.events.OnRequestAlarmApiListener;
import io.github.eyinfo.okrx.events.OnRequestErrorListener;
import io.github.eyinfo.okrx.properties.OkRxConfigParams;
import lombok.Getter;
import okhttp3.Call;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * Author Gs
 * Email:gs_12@foxmail.com
 * CreateTime:2017/6/1
 * Description: OkGo基础
 * Modifier:
 * ModifyContent:
 */
@SuppressWarnings("unchecked")
public class OkRx extends BaseObservable {

    private static OkRx okRx = null;
    //是否要重新获取配置参数
    private boolean isUpdateConfig = false;
    private OkRxConfigParams okRxConfigParams = null;
    private OnConfigParamsListener onConfigParamsListener = null;
    //application context 在cookies持久化时使用(为空时cookies使用内存持久化)
    private Context applicationContext = null;
    //接口请求结果json解析处理监听
    private OnBeanParsingJsonListener parsingJsonListener = null;
    //请求头监听
    private OnGlobalReuqestHeaderListener globalReuqestHeaderListener = null;
    /**
     * -- GETTER --
     * 获取全局请求参数回调
     *
     * @return OnGlobalRequestParamsListener
     */
    //全局请求参数
    @Getter
    private OnGlobalRequestParamsListener globalRequestParamsListener = null;
    //监听请求头参数
    private HashMap headers = null;
    /**
     * -- GETTER --
     * 获取http请求失败回调监听
     *
     * @return OnRequestErrorListener
     */
    //网络请求失败监听
    @Getter
    private OnRequestErrorListener onRequestErrorListener = null;
    /**
     * -- GETTER --
     * 获取授权相关监听
     *
     * @return OnAuthListener
     */
    //网络请求时用户授权相关回调监听
    @Getter
    private OnAuthListener onAuthListener = null;
    //在连接失败判断用,外面无须调用;
    //用于http socket connect fail处理
    @Getter
    private Set<String> failDomainList = new HashSet<String>();
    /**
     * -- GETTER --
     * 获取http cookies追加监听
     *
     * @return OnHeaderCookiesListener
     */
    //头部cookies监听
    @Getter
    private OnHeaderCookiesListener onHeaderCookiesListener = null;
    //跟踪日志是否带固件配置信息(默认false)
    private boolean isHasFirmwareConfigInformationForTraceLog = false;
    //ok http client
    private volatile OkHttpClient httpClient;
    //请求缓存队列(class name<->requestKey-call)
    private final HashMap<String, Map<String, Call>> requestQueue = new HashMap<>();
    /**
     * -- GETTER --
     * 接口请求总时间超过警报最大时间事件
     *
     * @return OnRequestAlarmApiListener
     */
    //接口请求总时间超过警报最大时间(OkRxConfigParams->requestAlarmMaxTime)时回调
    @Getter
    private OnRequestAlarmApiListener onRequestAlarmApiListener = null;

    @Getter
    private OnNetworkConnectListener onNetworkConnectListener;

    private String cacheGroup = "e984402acc49bcae";

    public static OkRx getInstance() {
        if (okRx == null) {
            okRx = new OkRx();
        }
        return okRx;
    }

    /**
     * 设置全局配置参数监听
     *
     * @param listener 全局配置参数监听
     */
    public OkRx setOnConfigParamsListener(OnConfigParamsListener listener) {
        this.onConfigParamsListener = listener;
        return this;
    }

    static class OkRxEntryCall implements OnEntryCall {

        private Object target;

        public OkRxEntryCall(Object target) {
            this.target = target;
        }

        @Override
        public Object onEntryResult() {
            return target;
        }
    }

    /**
     * 获取请求头回调监听
     *
     * @return OnGlobalReuqestHeaderListener
     */
    public OnGlobalReuqestHeaderListener getOnGlobalReuqestHeaderListener() {
        return globalReuqestHeaderListener;
    }

    /**
     * 设置全局请求头回调监听
     *
     * @param listener OnGlobalReuqestHeaderListener
     * @return
     */
    public OkRx setOnGlobalReuqestHeaderListener(OnGlobalReuqestHeaderListener listener) {
        this.globalReuqestHeaderListener = listener;
        return this;
    }

    /**
     * 设置全局请求参数回调
     *
     * @param listener OnGlobalRequestParamsListener
     * @return OkRx
     */
    public OkRx setGlobalRequestParamsListener(OnGlobalRequestParamsListener listener) {
        this.globalRequestParamsListener = listener;
        return this;
    }

    /**
     * okrx初始化
     * (一般在Application初始化时调用)
     *
     * @param context 上下文
     */
    public OkRx initialize(Context context) {
        this.applicationContext = context;
        this.isUpdateConfig = true;
        if (onConfigParamsListener != null) {
            okRxConfigParams = onConfigParamsListener.onConfigParamsCall(getDefaultConfigParams());
        }
        if (okRxConfigParams == null) {
            okRxConfigParams = new OkRxConfigParams();
        }
        return this;
    }

    //构建相关配置
    public void build() {
        if (onConfigParamsListener != null) {
            okRxConfigParams = onConfigParamsListener.onConfigParamsCall(getDefaultConfigParams());
        }
        if (okRxConfigParams == null) {
            okRxConfigParams = new OkRxConfigParams();
        }
        //缓存okRxConfigParams参数
        httpClient = newHttpClient(okRxConfigParams);
    }

    /**
     * 重新构建http client
     *
     * @param okRxConfigParams 全局配置参数
     * @return OkHttpClient
     */
    public OkHttpClient newHttpClient(OkRxConfigParams okRxConfigParams) {
        //创建http client对象
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //连接超时
        builder.connectTimeout(okRxConfigParams.getConnectTimeout(), TimeUnit.MILLISECONDS);
        //读取超时
        builder.readTimeout(okRxConfigParams.getReadTimeOut(), TimeUnit.MILLISECONDS);
        //写入超时
        builder.writeTimeout(okRxConfigParams.getWriteTimeOut(), TimeUnit.MILLISECONDS);
        //设置失败时重连次数,请求头信息
        builder.addInterceptor(new RequestRetryIntercepter(okRxConfigParams.getRetryCount(), okRxConfigParams.getHeaders()));
        //cookies持久化
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(applicationContext)));
        //添加证书信任
        try {
            SslSocketManager.SSLParams sslParams1 = SslSocketManager.getSslSocketFactory();
            if (sslParams1 != null) {
                builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.protocols(Collections.singletonList(Protocol.HTTP_1_1));
        //全局请求参数设置
        return builder.build();
    }

    /**
     * 获取http client对象
     *
     * @return OkHttpClient
     */
    public OkHttpClient getOkHttpClient() {
        if (httpClient == null) {
            synchronized (OkHttpClient.class) {
                if (httpClient == null) {
                    OkRxConfigParams configParams = getOkRxConfigParams();
                    httpClient = newHttpClient(configParams);
                }
            }
        }
        return httpClient;
    }

    /**
     * 获取okrx全局配置参数
     * (不要在application初始化时调用)
     *
     * @return
     */
    public OkRxConfigParams getOkRxConfigParams() {
        if (okRxConfigParams == null || isUpdateConfig) {
            if (onConfigParamsListener != null) {
                okRxConfigParams = onConfigParamsListener.onConfigParamsCall(getDefaultConfigParams());
            }
            isUpdateConfig = false;
        }
        //再次判断若全局参数为空则重新创建参数
        if (okRxConfigParams == null) {
            okRxConfigParams = new OkRxConfigParams();
        }
        return okRxConfigParams;
    }

    /**
     * 获取默认参数配置
     *
     * @return OkRxConfigParams
     */
    public OkRxConfigParams getDefaultConfigParams() {
        if (okRxConfigParams == null) {
            okRxConfigParams = new OkRxConfigParams();
        }
        return okRxConfigParams;
    }

    /**
     * 获取json解析监听对象
     *
     * @return json解析监听对象
     */
    public OnBeanParsingJsonListener getOnBeanParsingJsonListener() {
        return parsingJsonListener;
    }

    /**
     * 接口返回结果json解析需要自行处理的须实现此监听
     *
     * @param parsingJsonListener json解析监听对象
     * @return OkRx
     */
    public OkRx setOnBeanParsingJsonListener(OnBeanParsingJsonListener parsingJsonListener) {
        this.parsingJsonListener = parsingJsonListener;
        return this;
    }

    /**
     * 设置http请求时header参数(全局)
     * (持久存储)
     * 根据业务场景也可以在setOnGlobalReuqestHeaderListener()设置的监听中回调
     *
     * @param headers header params
     * @return OkRx
     */
    public OkRx setHeaderParams(HashMap<String, String> headers) {
        this.headers = headers;
        String json = ObjectJudge.isNullOrEmpty(headers) ? "" : JsonUtils.toJson(headers);
        MPLocalKV.getInstance().put("NetRequestHttpHeaderParams", json);
        return this;
    }

    /**
     * 获取请求头参数
     *
     * @return key-value header params
     */
    public HashMap<String, String> getHeaderParams() {
        if (ObjectJudge.isNullOrEmpty(headers)) {
            String params = MPLocalKV.getInstance().getString("NetRequestHttpHeaderParams", "");
            headers = JsonUtils.parseT(params, HashMap.class);
        }
        return headers;
    }

    /**
     * 设置http请求失败回调监听
     *
     * @param listener http失败回调监听
     */
    public OkRx setOnRequestErrorListener(OnRequestErrorListener listener) {
        this.onRequestErrorListener = listener;
        return this;
    }

    /**
     * 设置授权相关监听
     *
     * @param listener 授权相关监听
     */
    public OkRx setOnAuthListener(OnAuthListener listener) {
        this.onAuthListener = listener;
        return this;
    }

    /**
     * 设置http cookies追加监听
     *
     * @param listener OnHeaderCookiesListener
     * @return OkRx
     */
    public OkRx setOnHeaderCookiesListener(OnHeaderCookiesListener listener) {
        this.onHeaderCookiesListener = listener;
        return this;
    }

    /**
     * 清除token信息
     * 在用户退出登录时调用
     */
    public void clearToken() {
        OkHttpClient client = getOkHttpClient();
        if (client == null) {
            return;
        }
        CookieJar cookieJar = client.cookieJar();
        if (!(cookieJar instanceof CookieJarImpl)) {
            return;
        }
        CookieJarImpl cookieImpl = (CookieJarImpl) cookieJar;
        CookieStore cookieStore = cookieImpl.getCookieStore();
        if (cookieStore == null) {
            return;
        }
        cookieStore.removeAllCookie();
    }

    /**
     * 设置跟踪日志是否带固件配置信息(默认false)
     *
     * @param isHasFirmwareConfigInformationForTraceLog true-对于请求失败跟踪日志带有设备相关配置信息;反之则不带;
     * @return OkRx
     */
    public OkRx setHasFirmwareConfigInformationForTraceLog(boolean isHasFirmwareConfigInformationForTraceLog) {
        this.isHasFirmwareConfigInformationForTraceLog = isHasFirmwareConfigInformationForTraceLog;
        return this;
    }

    /**
     * 获取跟踪日志是否带固件配置信息(默认false)
     *
     * @return true-对于请求失败跟踪日志带有设备相关配置信息;反之则不带;
     */
    public boolean isHasFirmwareConfigInformationForTraceLog() {
        return this.isHasFirmwareConfigInformationForTraceLog;
    }

    /**
     * 取消所有请求
     */
    public void cancelAllRequest() {
        for (Map.Entry<String, Map<String, Call>> entry : requestQueue.entrySet()) {
            Map<String, Call> map = entry.getValue();
            if (map == null) {
                continue;
            }
            for (Map.Entry<String, Call> callEntry : map.entrySet()) {
                Call value = callEntry.getValue();
                if (value == null) {
                    continue;
                }
                value.cancel();
            }
        }
    }

    /**
     * 取消接口调用类对应的请求
     *
     * @param useClass 接口所调用的类
     */
    public void cancelRequest(Class useClass) {
        if (useClass == null) {
            return;
        }
        String className = useClass.getName();
        if (!requestQueue.containsKey(className)) {
            return;
        }
        Map<String, Call> callMap = requestQueue.get(className);
        if (callMap == null) {
            return;
        }
        for (Map.Entry<String, Call> entry : callMap.entrySet()) {
            Call value = entry.getValue();
            if (value == null) {
                continue;
            }
            value.cancel();
        }
    }

    /**
     * 从requestQueue中移除请求不作取消(相关请求已经结束)
     *
     * @param requestKey request key
     */
    public void removeRequest(String requestKey) {
        if (TextUtils.isEmpty(requestKey)) {
            return;
        }
        super.buildSubscribe(requestKey, new OnSubscribeConsumer<String, Object>() {
            @Override
            public void onSubscribe(String requestKey, Object o) throws Exception {
                synchronized (requestQueue) {
                    for (Map.Entry<String, Map<String, Call>> entry : requestQueue.entrySet()) {
                        Map<String, Call> value = entry.getValue();
                        if (value == null) {
                            continue;
                        }
                        if (!value.containsKey(requestKey)) {
                            return;
                        }
                        value.remove(requestKey);
                        if (value.isEmpty()) {
                            requestQueue.remove(entry.getKey());
                        }
                    }
                }
            }
        }, null, null);
    }

    /**
     * 添加请求到队列
     *
     * @param useClassName 请求所使用的类
     * @param requestKey   url+[params]
     * @param call         请求对象
     */
    public void putRequest(String useClassName, String requestKey, Call call) {
        if (TextUtils.isEmpty(useClassName) || TextUtils.isEmpty(requestKey) || call == null) {
            return;
        }
        Map<String, Call> map = null;
        if (requestQueue.containsKey(useClassName)) {
            map = requestQueue.get(useClassName);
        }
        boolean inqueue = false;
        if (map == null) {
            inqueue = true;
            map = new HashMap<String, Call>();
        }
        if (!map.containsKey(requestKey)) {
            map.put(requestKey, call);
        }
        if (inqueue) {
            requestQueue.put(useClassName, map);
        }
    }

    /**
     * 设置接口请求总时间超过警报最大时间事件
     *
     * @param listener OnRequestAlarmApiListener
     * @return OkRx
     */
    public OkRx setOnRequestAlarmApiListener(OnRequestAlarmApiListener listener) {
        this.onRequestAlarmApiListener = listener;
        return this;
    }

    public OkRx setOnNetworkConnectListener(OnNetworkConnectListener listener) {
        this.onNetworkConnectListener = listener;
        return this;
    }
}