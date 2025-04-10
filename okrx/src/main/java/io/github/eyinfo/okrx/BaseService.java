package io.github.eyinfo.okrx;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.eyinfo.android_pure_utils.events.Action1;
import com.eyinfo.android_pure_utils.events.Action2;
import com.eyinfo.android_pure_utils.events.Func2;
import com.eyinfo.android_pure_utils.logs.Logger;
import com.eyinfo.android_pure_utils.observable.ObservableComponent;
import com.eyinfo.android_pure_utils.utils.ConvertUtils;
import com.eyinfo.android_pure_utils.utils.GlobalUtils;
import com.eyinfo.android_pure_utils.utils.JsonUtils;
import com.eyinfo.android_pure_utils.utils.ObjectJudge;
import com.eyinfo.android_pure_utils.utils.PathsUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.github.eyinfo.okrx.annotations.ApiCheckAnnotation;
import io.github.eyinfo.okrx.annotations.DataKeyField;
import io.github.eyinfo.okrx.annotations.DetailCacheParsingField;
import io.github.eyinfo.okrx.annotations.ReturnCodeFilter;
import io.github.eyinfo.okrx.beans.CompleteResponse;
import io.github.eyinfo.okrx.beans.ResponseData;
import io.github.eyinfo.okrx.beans.ResponseParsing;
import io.github.eyinfo.okrx.beans.ResultParams;
import io.github.eyinfo.okrx.beans.RetrofitParams;
import io.github.eyinfo.okrx.beans.SuccessResponse;
import io.github.eyinfo.okrx.beans.TransParams;
import io.github.eyinfo.okrx.cache.NetCacheManager;
import io.github.eyinfo.okrx.enums.CallStatus;
import io.github.eyinfo.okrx.enums.DataType;
import io.github.eyinfo.okrx.enums.ErrorType;
import io.github.eyinfo.okrx.enums.RequestState;
import io.github.eyinfo.okrx.enums.RequestType;
import io.github.eyinfo.okrx.enums.ResponseDataType;
import io.github.eyinfo.okrx.events.OnApiRetCodesFilterListener;
import io.github.eyinfo.okrx.events.OnAuthListener;
import io.github.eyinfo.okrx.events.OnBeanParsingJsonListener;
import io.github.eyinfo.okrx.events.OnGlobalRequestParamsListener;
import io.github.eyinfo.okrx.events.OnGlobalReuqestHeaderListener;
import io.github.eyinfo.okrx.events.OnSuccessfulListener;
import io.github.eyinfo.okrx.properties.ByteRequestItem;
import io.github.eyinfo.okrx.properties.OkRxConfigParams;
import io.github.eyinfo.okrx.properties.OkRxValidParam;


/**
 * Author lijinghuan
 * Email:ljh0576123@163.com
 * CreateTime:2016/6/14
 * Description:后期合并请求方式(逐步改进)
 */
public class BaseService {

    private Handler mhandler = new Handler(Looper.getMainLooper());
    private ReturnCodeFilter returnCodeFilter = null;

    protected void onRequestCompleted() {
        //请求完成(结束)
    }

    protected void onRequestError() {
        //请求错误
    }

    protected <S extends BaseService> void baseConfig(BaseService baseService, BaseSubscriber<Object, S> baseSubscriber, RetrofitParams retrofitParams, OkRxValidParam validParam, String useClass) {
        try {
            if (!TextUtils.isEmpty(retrofitParams.getRequestUrl())) {
                String requestUrl = retrofitParams.getRequestUrl();
                //全局头设置的信息
                HashMap<String, String> mHeaders = bindGlobalHeaders();
                //接口头信息
                mHeaders.putAll(retrofitParams.getHeadParams());
                //设置返回码监听
                if (returnCodeFilter == null) {
                    returnCodeFilter = validParam.getReturnCodeFilter();
                }
                retrofitParams.setInvokeMethodName(validParam.getInvokeMethodName());
                if (retrofitParams.getRequestType() == RequestType.BYTES) {
                    HashMap<String, Object> updateByteParams = getUploadByteParams(retrofitParams);
                    List<ByteRequestItem> uploadByteItems = getUploadByteItems(retrofitParams);
                    subBytes(requestUrl, mHeaders, updateByteParams, retrofitParams, uploadByteItems, baseService, baseSubscriber);
                } else {
                    //绑定全局请求参数(delQuery和params参数同一作用域)
                    OnGlobalRequestParamsListener globalRequestParamsListener = OkRx.getInstance().getGlobalRequestParamsListener();
                    if (globalRequestParamsListener != null) {
                        HashMap<String, Object> globalParams = globalRequestParamsListener.onGlobalParams();
                        if (!ObjectJudge.isNullOrEmpty(globalParams)) {
                            TreeMap<String, Object> params = retrofitParams.getParams();
                            for (Map.Entry<String, Object> entry : globalParams.entrySet()) {
                                if (!params.containsKey(entry.getKey())) {
                                    params.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }
                    request(requestUrl, mHeaders, retrofitParams, useClass, baseService, baseSubscriber);
                }
            } else {
                finishedRequest(baseService);
            }
        } catch (Exception e) {
            finishedRequest(baseService);
            Logger.error(e);
        }
    }

    //绑定全局头信息
    private HashMap<String, String> bindGlobalHeaders() {
        HashMap<String, String> headParams = new HashMap<String, String>();
        HashMap<String, String> defaultHeaderParams = OkRx.getInstance().getHeaderParams();
        if (!ObjectJudge.isNullOrEmpty(defaultHeaderParams)) {
            headParams.putAll(defaultHeaderParams);
        }
        //从监听对象中获取
        OnGlobalReuqestHeaderListener headerListener = OkRx.getInstance().getOnGlobalReuqestHeaderListener();
        if (headerListener == null) {
            return headParams;
        }
        HashMap<String, String> globalHeaderParams = headerListener.onHeaderParams();
        if (ObjectJudge.isNullOrEmpty(globalHeaderParams)) {
            return headParams;
        }
        headParams.putAll(globalHeaderParams);
        return headParams;
    }

    private void finishedRequest(final BaseService baseService) {
        if (ObjectJudge.isMainThread()) {
            baseService.onRequestCompleted();
        } else {
            mhandler.post(new Runnable() {
                @Override
                public void run() {
                    baseService.onRequestCompleted();
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <S extends BaseService> void successDealWith(SuccessResponse successResponse,
                                                         BaseService baseService,
                                                         BaseSubscriber<Object, S> baseSubscriber) {
        boolean isBasicData = false;
        RetrofitParams retrofitParams = successResponse.getRetrofitParams();
        Class dataClass = retrofitParams.getDataClass();
        //解析后结果数据
        ResponseParsing responseParsing = new ResponseParsing();
        responseParsing.setDataClass(dataClass);
        //数据类型object\byte\stream
        ResponseData responseData = successResponse.getResponseData();
        ResponseDataType responseDataType = responseData.getResponseDataType();
        responseParsing.setResponseDataType(responseDataType);
        if (responseDataType == ResponseDataType.object) {
            if (dataClass == String.class ||
                    dataClass == Integer.class ||
                    dataClass == Double.class ||
                    dataClass == Float.class ||
                    dataClass == Long.class) {
                //如果dataClass为基础数据类型则不进行解析
                isBasicData = true;
                responseParsing.setData(responseData.getResponse());
            } else {
                OnBeanParsingJsonListener jsonListener = OkRx.getInstance().getOnBeanParsingJsonListener();
                if (jsonListener == null) {
                    if (retrofitParams.isCollectionDataType()) {
                        responseParsing.setData(JsonUtils.parseArray(responseData.getResponse(), dataClass));
                    } else {
                        responseParsing.setData(JsonUtils.parseT(responseData.getResponse(), dataClass));
                    }
                } else {
                    responseParsing.setData(jsonListener.onBeanParsingJson(responseData.getResponse(), dataClass, retrofitParams.isCollectionDataType()));

                    if (responseParsing.getData() != null && retrofitParams.getCacheTime() != null && !TextUtils.isEmpty(retrofitParams.getCacheKey())) {
                        DetailCacheParam detailCacheParam = new DetailCacheParam();
                        detailCacheParam.cacheKey = retrofitParams.getCacheKey();
                        detailCacheParam.data = responseParsing.getData();
                        detailCacheParam.isCollectionDataType = retrofitParams.isCollectionDataType();
                        detailCacheParam.duration = retrofitParams.getCacheTime();
                        detailCacheParam.parsingFieldMapping = retrofitParams.getParsingFieldMapping();
                        detailCacheComponent.build(detailCacheParam);
                    }
                }
            }
            //如果空则回调错误
            //如果从缓存过来的且对象为空则不处理
            if (responseParsing.getData() == null) {
                if (successResponse.getDataType() == DataType.CacheData) {
                    return;
                }
                //如果仅是EmptyForOnlyCache(空缓存状态则返回成功回调)
                if (successResponse.getDataType() == DataType.EmptyForOnlyCache) {
                    successCall(baseService, baseSubscriber, responseParsing, retrofitParams, successResponse.getDataType(), successResponse.getCode());
                } else {
                    sendErrorAction(baseService, baseSubscriber, ErrorType.businessProcess, successResponse.getCode());
                    finishedRequest(baseService);
                }
                return;
            }
        } else if (responseDataType == ResponseDataType.byteData) {
            //字节数据
            responseParsing.setBytes(responseData.getBytes());
            //如果空则回调错误
            if (responseParsing.getBytes() == null) {
                sendErrorAction(baseService, baseSubscriber, ErrorType.businessProcess, successResponse.getCode());
                finishedRequest(baseService);
                return;
            }
        } else if (responseDataType == ResponseDataType.stream) {
            //流数据
            responseParsing.setStream(responseData.getStream());
            //如果空则回调错误
            if (responseParsing.getStream() == null) {
                sendErrorAction(baseService, baseSubscriber, ErrorType.businessProcess, successResponse.getCode());
                finishedRequest(baseService);
                return;
            }
        }
        //如果是集合\byte\stream则取消拦截
        if (retrofitParams.isCollectionDataType() || isBasicData || responseDataType != ResponseDataType.object) {
            //成功回调
            if (responseDataType == ResponseDataType.byteData && dataClass != Class.class) {
                if (dataClass == Bitmap.class) {
                    responseParsing.setData(ConvertUtils.toBitmap(responseParsing.getBytes()));
                    responseParsing.setBytes(null);
                } else if (dataClass == String.class) {
                    responseParsing.setData(new String(responseParsing.getBytes()));
                    responseParsing.setBytes(null);
                }
            }
            successCall(baseService, baseSubscriber, responseParsing, retrofitParams, successResponse.getDataType(), successResponse.getCode());
        } else {
            //开启拦截且拦截符合的返回码
            OkRxConfigParams okRxConfigParams = OkRx.getInstance().getOkRxConfigParams();
            if (okRxConfigParams.isNetStatusCodeIntercept()) {
                if (!filterMatchRetCodes(responseParsing.getData())) {
                    successCall(baseService, baseSubscriber, responseParsing, retrofitParams, successResponse.getDataType(), successResponse.getCode());
                }
            } else {
                successCall(baseService, baseSubscriber, responseParsing, retrofitParams, successResponse.getDataType(), successResponse.getCode());
            }
        }
    }

    private static class DetailCacheParam {
        String cacheKey;
        boolean isCollectionDataType;
        Object data;
        Duration duration;
        String parsingFieldMapping;
    }

    private ObservableComponent<Object, DetailCacheParam> detailCacheComponent = new ObservableComponent<Object, DetailCacheParam>() {

        private String getDataId(Object object) {
            if (object == null) {
                return "";
            }
            String dataId = "";
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(DataKeyField.class)) {
                    Object value = GlobalUtils.getPropertiesValue(object, field.getName());
                    dataId = (value == null ? "" : String.valueOf(value));
                    break;
                }
            }
            return dataId;
        }

        private Object getParsingObject(Object object) {
            if (object == null || object instanceof List) {
                return object;
            }
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(DetailCacheParsingField.class)) {
                    return GlobalUtils.getPropertiesValue(object, field.getName());
                }
            }
            return object;
        }

        private void saveDetals(Object object, String cacheKey, Duration duration) {
            List list = (List) object;
            HashMap<String, String> dataMap = new HashMap<>();
            for (Object item : list) {
                String dataId = getDataId(item);
                if (TextUtils.isEmpty(dataId)) {
                    continue;
                }
                String value = JsonUtils.toJson(item);
                dataMap.put(dataId, value);
            }
            String json = JsonUtils.toJson(dataMap);
            NetCacheManager.getInstance().cacheRequestData(cacheKey, json, duration);
        }

        private Object getParsingObject(Object data, String[] mappingFields) {
            if (ObjectJudge.isNullOrEmpty(mappingFields) || data instanceof List) {
                return data;
            }
            for (String field : mappingFields) {
                Object value = GlobalUtils.getPropertiesValue(data, field);
                if (value instanceof List) {
                    return value;
                } else {
                    data = value;
                }
            }
            return data;
        }

        @Override
        protected Object subscribeWith(DetailCacheParam... detailCacheParams) throws Exception {
            if (!ObjectJudge.isNullOrEmpty(detailCacheParams)) {
                DetailCacheParam param = detailCacheParams[0];
                //根据映射关系找到数据对象
                if (!TextUtils.isEmpty(param.parsingFieldMapping)) {
                    String[] mfields = param.parsingFieldMapping.split("->");
                    param.data = getParsingObject(param.data, mfields);
                }
                //缓存明细数据
                if (!param.isCollectionDataType) {
                    Object parsingObject = getParsingObject(param.data);
                    if (parsingObject instanceof List) {
                        saveDetals(parsingObject, param.cacheKey, param.duration);
                    } else {
                        String dataId = getDataId(parsingObject);
                        if (TextUtils.isEmpty(dataId)) {
                            return null;
                        }
                        String value = JsonUtils.toJson(parsingObject);
                        String key = String.format("%s_%s", dataId, param.cacheKey);
                        NetCacheManager.getInstance().cacheRequestData(key, value, param.duration);
                    }
                    return null;
                }
                if (!(param.data instanceof List)) {
                    return null;
                }
                saveDetals(param.data, param.cacheKey, param.duration);
            }
            return super.subscribeWith(detailCacheParams);
        }
    };

    @SuppressWarnings("unchecked")
    private <S extends BaseService> void successCall(BaseService baseService,
                                                     BaseSubscriber<Object, S> baseSubscriber,
                                                     ResponseParsing responseParsing,
                                                     RetrofitParams retrofitParams,
                                                     DataType dataType,
                                                     int code) {
        if (baseSubscriber == null) {
            return;
        }
        //成功回调
        ResponseDataType responseDataType = responseParsing.getResponseDataType();
        if (responseDataType == ResponseDataType.object) {
            ResultParams resultParams = new ResultParams();
            resultParams.setData(responseParsing.getData());
            resultParams.setDataType(dataType);
            resultParams.setRequestStartTime(retrofitParams.getCurrentRequestTime());
            resultParams.setRequestTotalTime(retrofitParams.getRequestTotalTime());
            resultParams.setCode(code);
            baseSubscriber.onNext(resultParams);
        } else if (responseDataType == ResponseDataType.byteData) {
            //绑定字节
            bindBytes(baseSubscriber, responseParsing, retrofitParams, dataType, retrofitParams.getCurrentRequestTime(), retrofitParams.getRequestTotalTime(), code);
        } else if (responseDataType == ResponseDataType.stream) {
            //绑定流
            bindStream(baseSubscriber, responseParsing, retrofitParams, dataType, retrofitParams.getCurrentRequestTime(), retrofitParams.getRequestTotalTime(), code);
        }
    }

    private <T> boolean filterMatchRetCodes(T data) {
        Class<?> codesListeningClass = returnCodeFilter.retCodesListeningClass();
        if (returnCodeFilter == null || ObjectJudge.isNullOrEmpty(returnCodeFilter.retCodes()) || codesListeningClass == null) {
            return false;
        }
        List<String> codes = Arrays.asList(returnCodeFilter.retCodes());
        String code = String.valueOf(GlobalUtils.getPropertiesValue(data, "code"));
        if (!codes.contains(code)) {
            return false;
        }
        Object obj = JsonUtils.newNull(codesListeningClass);
        if (!(obj instanceof OnApiRetCodesFilterListener)) {
            //若状态码拦截成功且只是未实现处理，也中断之后处理;
            return true;
        }
        OnApiRetCodesFilterListener filterListener = (OnApiRetCodesFilterListener) obj;
        filterListener.onApiRetCodesFilter(code, data);
        return true;
    }

    private <S extends BaseService> void subBytes(String requestUrl,
                                                  HashMap<String, String> httpHeaders,
                                                  HashMap<String, Object> httpParams,
                                                  final RetrofitParams retrofitParams,
                                                  List<ByteRequestItem> byteRequestItems,
                                                  final BaseService baseService,
                                                  final BaseSubscriber<Object, S> baseSubscriber) {
        OkRxManager.getInstance().uploadBytes(
                requestUrl,
                httpHeaders,
                httpParams,
                byteRequestItems,
                retrofitParams,
                new Action1<SuccessResponse>() {
                    @Override
                    public void call(SuccessResponse response) {
                        successDealWith(response, baseService, baseSubscriber);
                    }
                },
                new Action1<CompleteResponse>() {
                    @Override
                    public void call(CompleteResponse response) {
                        if (response.getRequestState() == RequestState.Error) {
                            sendErrorAction(baseService, baseSubscriber, response.getErrorType(), response.getCode());
                        } else if (response.getRequestState() == RequestState.Completed) {
                            finishedRequest(baseService);
                        }
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private <S extends BaseService> void sendErrorAction(final BaseService baseService,
                                                         final BaseSubscriber<Object, S> baseSubscriber,
                                                         final ErrorType errorType,
                                                         final int code) {
        final Action2<ErrorType, Integer> errorAction = new Action2<ErrorType, Integer>() {
            @Override
            public void call(ErrorType errorType, Integer code) {
                if (baseSubscriber == null) {
                    return;
                }
                OnSuccessfulListener successfulListener = baseSubscriber.getOnSuccessfulListener();
                if (successfulListener == null) {
                    return;
                }
                successfulListener.setCode(code == null ? 0 : code);
                successfulListener.onError(null, errorType, baseSubscriber.getExtra());
                successfulListener.onError(errorType, baseSubscriber.getExtra());
                successfulListener.onCompleted(baseSubscriber.getExtra());
            }
        };

        if (ObjectJudge.isMainThread()) {
            errorAction.call(errorType, code);
            baseService.onRequestError();
        } else {
            mhandler.post(new Runnable() {
                @Override
                public void run() {
                    errorAction.call(errorType, code);
                    baseService.onRequestError();
                }
            });
        }
    }

    private <S extends BaseService> void request(String reqreuestUrl,
                                                 HashMap<String, String> headers,
                                                 final RetrofitParams retrofitParams,
                                                 String useClass,
                                                 final BaseService baseService,
                                                 final BaseSubscriber<Object, S> baseSubscriber) {
        TransParams transParams = new TransParams();
        transParams.setUrl(reqreuestUrl);
        transParams.setHeaders(headers);
        transParams.setRetrofitParams(retrofitParams);
        transParams.setUseClass(useClass);
        OkRxManager.getInstance().request(transParams, new Action1<SuccessResponse>() {
                    @Override
                    public void call(SuccessResponse response) {
                        successDealWith(response, baseService, baseSubscriber);
                    }
                },
                new Action1<CompleteResponse>() {
                    @Override
                    public void call(CompleteResponse response) {
                        if (response.getRequestState() == RequestState.Error) {
                            sendErrorAction(baseService, baseSubscriber, response.getErrorType(), response.getCode());
                        } else if (response.getRequestState() == RequestState.Completed) {
                            finishedRequest(baseService);
                        }
                    }
                });
    }

    private List<ByteRequestItem> getUploadByteItems(RetrofitParams retrofitParams) {
        List<ByteRequestItem> lst = new ArrayList<ByteRequestItem>();
        TreeMap<String, Object> params = retrofitParams.getParams();
        if (ObjectJudge.isNullOrEmpty(params)) {
            return lst;
        }
        for (HashMap.Entry<String, Object> entry : params.entrySet()) {
            //参数名
            String key = entry.getKey();
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            //参数值
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if ((value instanceof byte[]) || (value instanceof Byte[])) {
                ByteRequestItem requestItem = new ByteRequestItem();
                requestItem.setFieldName(key);
                requestItem.setBs((byte[]) value);
                lst.add(requestItem);
            }
        }
        return lst;
    }

    private HashMap<String, Object> getUploadByteParams(RetrofitParams retrofitParams) {
        HashMap<String, Object> params2 = new HashMap<String, Object>();
        if (ObjectJudge.isNullOrEmpty(retrofitParams.getParams())) {
            return params2;
        }
        TreeMap<String, Object> params = retrofitParams.getParams();
        for (HashMap.Entry<String, Object> entry : params.entrySet()) {
            //参数名
            String key = entry.getKey();
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            //参数值
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof Integer) {
                params2.put(key, value);
            } else if (value instanceof Long) {
                params2.put(key, value);
            } else if (value instanceof String) {
                params2.put(key, value);
            } else if (value instanceof Double) {
                params2.put(key, value);
            } else if (value instanceof Float) {
                params2.put(key, value);
            } else if (value instanceof Boolean) {
                params2.put(key, value);
            } else if (value instanceof List) {
                params2.put(key, JsonUtils.toJson(value));
            }
        }
        return params2;
    }

    @SuppressWarnings("unchecked")
    private <T, S extends BaseService> void finishedRequest(final ErrorType errorType, final BaseSubscriber<T, S> baseSubscriber) {
        if (ObjectJudge.isMainThread()) {
            OnSuccessfulListener successfulListener = baseSubscriber.getOnSuccessfulListener();
            if (successfulListener != null) {
                successfulListener.onError(null, errorType, baseSubscriber.getExtra());
                successfulListener.onError(errorType, baseSubscriber.getExtra());
                successfulListener.onCompleted(baseSubscriber.getExtra());
            }
        } else {
            mhandler.post(new Runnable() {
                @Override
                public void run() {
                    OnSuccessfulListener successfulListener = baseSubscriber.getOnSuccessfulListener();
                    if (successfulListener != null) {
                        successfulListener.onError(null, errorType, baseSubscriber.getExtra());
                        successfulListener.onError(errorType, baseSubscriber.getExtra());
                        successfulListener.onCompleted(baseSubscriber.getExtra());
                    }
                }
            });
        }
    }

    protected <I, S extends BaseService> void requestObject(Class<I> apiClass,
                                                            S server,
                                                            final BaseSubscriber<Object, S> baseSubscriber,
                                                            OkRxValidParam validParam,
                                                            Func2<String, S, Integer> urlAction,
                                                            Func2<RetrofitParams, I, HashMap<String, Object>> decApiAction,
                                                            HashMap<String, Object> params,
                                                            String useClass) {
        try {
            //若需要登录验证则打开登录页面
            if (validParam.isNeedLogin()) {
                OnAuthListener authListener = OkRx.getInstance().getOnAuthListener();
                if (authListener != null) {
                    authListener.onLoginCall(validParam.getInvokeMethodName());
                }
                finishedRequest(ErrorType.businessProcess, baseSubscriber);
                return;
            }
            //验证失败结束请求(需要判断当前请求的接口是否在线程中请求)
            if (!validParam.isFlag()) {
                finishedRequest(ErrorType.businessProcess, baseSubscriber);
                return;
            }
            if (urlAction == null || server == null) {
                finishedRequest(ErrorType.businessProcess, baseSubscriber);
                return;
            }
            OkRxParsing parsing = new OkRxParsing();
            ApiCheckAnnotation apiCheckAnnotation = validParam.getApiCheckAnnotation();
            I decApi = parsing.createAPI(apiClass, apiCheckAnnotation.callStatus());
            if (decApiAction == null || decApi == null || validParam.getApiCheckAnnotation() == null) {
                finishedRequest(ErrorType.businessProcess, baseSubscriber);
                return;
            }
            ApiRequestRunnable<I, S> runnable = new ApiRequestRunnable<>(server, baseSubscriber, validParam, urlAction, decApi, params, decApiAction, useClass, apiCheckAnnotation.detailCacheKey());
            runnable.run();
        } catch (Exception e) {
            finishedRequest(ErrorType.businessProcess, baseSubscriber);
        }
    }

    private class ApiRequestRunnable<I, S extends BaseService> implements Runnable {

        private S server;
        private BaseSubscriber<Object, S> baseSubscriber;
        private OkRxValidParam validParam;
        private Func2<String, S, Integer> urlAction;
        private I decApi;
        private HashMap<String, Object> params;
        private Func2<RetrofitParams, I, HashMap<String, Object>> decApiAction;
        private String useClass;

        public ApiRequestRunnable(S server,
                                  final BaseSubscriber<Object, S> baseSubscriber,
                                  OkRxValidParam validParam,
                                  Func2<String, S, Integer> urlAction,
                                  I decApi,
                                  HashMap<String, Object> params,
                                  Func2<RetrofitParams, I, HashMap<String, Object>> decApiAction,
                                  String useClass,
                                  String detailCacheKey) {
            this.server = server;
            this.baseSubscriber = baseSubscriber;
            this.validParam = validParam;
            this.urlAction = urlAction;
            this.decApi = decApi;
            this.params = params;
            this.decApiAction = decApiAction;
            this.useClass = useClass;
        }

        @Override
        public void run() {
            RetrofitParams retrofitParams = decApiAction.call(decApi, params);
            retrofitParams.setCurrentRequestTime(validParam.getCurrentRequestTime());
            if (!retrofitParams.getFlag()) {
                finishedRequest(ErrorType.businessProcess, baseSubscriber);
                return;
            }
            //若api类未指定base url类型名称则不作请求处理
            if (retrofitParams.getIsJoinUrl() && retrofitParams.getUrlTypeName() == null) {
                finishedRequest(ErrorType.businessProcess, baseSubscriber);
                return;
            }
            if (retrofitParams.getIsJoinUrl() && retrofitParams.getUrlTypeName().value() == 0) {
                finishedRequest(ErrorType.businessProcess, baseSubscriber);
                return;
            }
            ApiCheckAnnotation apiCheckAnnotation = validParam.getApiCheckAnnotation();
            retrofitParams.setTokenValid(apiCheckAnnotation.isTokenValid());
            retrofitParams.setInvokeMethodName(validParam.getInvokeMethodName());
            apiRequest(server, baseSubscriber, validParam, retrofitParams, urlAction, useClass);
        }
    }

    private <I, S extends BaseService> void apiRequest(S server,
                                                       final BaseSubscriber<Object, S> baseSubscriber,
                                                       OkRxValidParam validParam,
                                                       final RetrofitParams retrofitParams,
                                                       Func2<String, S, Integer> urlAction,
                                                       String useClass) {
        //设置此接口允许返回码
        if (!ObjectJudge.isNullOrEmpty(retrofitParams.getAllowRetCodes())) {
            List<String> allowRetCodes = baseSubscriber.getAllowRetCodes();
            allowRetCodes.addAll(retrofitParams.getAllowRetCodes());
        }
        //设置请求地址
        if (retrofitParams.getUrlTypeName() != null) {
            if (retrofitParams.getIsJoinUrl()) {
                String baseUrl = urlAction.call(server, retrofitParams.getUrlTypeName().value());
                retrofitParams.setRequestUrl(PathsUtils.combine(baseUrl, retrofitParams.getRequestUrl()));
                if (retrofitParams.isLastContainsPath() && !retrofitParams.getRequestUrl().endsWith("/")) {
                    retrofitParams.setRequestUrl(retrofitParams.getRequestUrl() + "/");
                }
            }
        }
        //NO_CACHE: 不使用缓存,该模式下,cacheKey,cacheTime 参数均无效
        //DEFAULT: 按照HTTP协议的默认缓存规则，例如有304响应头时缓存。
        //REQUEST_FAILED_READ_CACHE：先请求网络，如果请求网络失败，则读取缓存，如果读取缓存失败，本次请求失败。
        //IF_NONE_CACHE_REQUEST：如果缓存不存在才请求网络，否则使用缓存。
        //FIRST_CACHE_THEN_REQUEST：先使用缓存，不管是否存在，仍然请求网络。
        //缓存的过期时间,单位毫秒
        //为确保未设置缓存请求几乎不做缓存，此处默认缓存时间暂设为5秒
        ApiCheckAnnotation apiCheckAnnotation = validParam.getApiCheckAnnotation();
        retrofitParams.setCacheKey(apiCheckAnnotation.cacheKey());
        //设置缓存时间
        CallStatus status = retrofitParams.getCallStatus();
        if (status != CallStatus.OnlyNet) {
            long milliseconds = ConvertUtils.toMilliseconds(apiCheckAnnotation.cacheTime(), apiCheckAnnotation.cacheTimeUnit());
            retrofitParams.setCacheTime(Duration.ofMillis(milliseconds));
        }
        //拼接完整的url
        //del请求看delQuery参数是不是为空
        if (!ObjectJudge.isNullOrEmpty(retrofitParams.getDelQueryParams())) {
            StringBuilder query = new StringBuilder();
            for (Map.Entry<String, String> entry : retrofitParams.getDelQueryParams().entrySet()) {
                query.append(MessageFormat.format("{0}={1},", entry.getKey(), entry.getValue()));
            }
            if (query.length() > 0) {
                if (!retrofitParams.getRequestUrl().contains("?")) {
                    retrofitParams.setRequestUrl(String.format("%s?%s",
                            retrofitParams.getRequestUrl(),
                            query.substring(0, query.length() - 1)));
                } else {
                    retrofitParams.setRequestUrl(String.format("%s&%s",
                            retrofitParams.getRequestUrl(),
                            query.substring(0, query.length() - 1)));
                }
            }
        }
        server.baseConfig(server, baseSubscriber, retrofitParams, validParam, useClass);
    }

    private <S extends BaseService> void bindStream(BaseSubscriber<Object, S> baseSubscriber, ResponseParsing responseParsing, RetrofitParams retrofitParams, DataType dataType, Long requestStartTime, Long requestTotalTime, int code) {
        ResultParams<Object> resultParams = new ResultParams<>();
        resultParams.setDataType(dataType);
        resultParams.setRequestStartTime(requestStartTime);
        resultParams.setRequestTotalTime(requestTotalTime);
        resultParams.setCode(code);
        if (responseParsing.getDataClass() == File.class && !TextUtils.isEmpty(retrofitParams.getTargetFilePath())) {
            File file = new File(retrofitParams.getTargetFilePath());
            if (file.exists()) {
                ConvertUtils.toFile(file, responseParsing.getStream());
                resultParams.setData(file);
                baseSubscriber.onNext(resultParams);
            } else {
                resultParams.setData(responseParsing.getStream());
                baseSubscriber.onNext(resultParams);
            }
        } else {
            resultParams.setData(responseParsing.getStream());
            baseSubscriber.onNext(resultParams);
        }
    }

    private <S extends BaseService> void bindBytes(BaseSubscriber<Object, S> baseSubscriber, ResponseParsing responseParsing, RetrofitParams retrofitParams, DataType dataType, Long requestStartTime, Long requestTotalTime, int code) {
        ResultParams<Object> resultParams = new ResultParams<>();
        resultParams.setDataType(dataType);
        resultParams.setRequestStartTime(requestStartTime);
        resultParams.setRequestTotalTime(requestTotalTime);
        resultParams.setCode(code);
        if (responseParsing.getBytes() == null) {
            resultParams.setData(responseParsing.getData());
            baseSubscriber.onNext(resultParams);
        } else {
            if (responseParsing.getDataClass() == File.class && !TextUtils.isEmpty(retrofitParams.getTargetFilePath())) {
                File file = new File(retrofitParams.getTargetFilePath());
                if (file.exists()) {
                    ConvertUtils.toFile(file, responseParsing.getBytes());
                    resultParams.setData(file);
                    baseSubscriber.onNext(resultParams);
                } else {
                    resultParams.setData(responseParsing.getBytes());
                    baseSubscriber.onNext(resultParams);
                }
            } else {
                resultParams.setData(responseParsing.getBytes());
                baseSubscriber.onNext(resultParams);
            }
        }
    }
}
