package io.github.eyinfo.okrx.cache;

import java.time.Duration;
import java.time.Instant;

import io.github.eyinfo.okrx.beans.RequestCacheElement;
import io.github.eyinfo.okrx.beans.RequestCacheElement_;
import io.objectbox.query.QueryBuilder;

public class NetCacheManager {
    private static volatile NetCacheManager instance;

    public static NetCacheManager getInstance() {
        if (instance == null) {
            synchronized (NetCacheManager.class) {
                if (instance == null) {
                    instance = new NetCacheManager();
                }
            }
        }
        return instance;
    }

    private long preDetectionTime = 0;

    public void cacheRequestData(String cacheKey, String data, Duration duration) {
        RequestCacheElement element = new RequestCacheElement();
        element.setId(cacheKey.hashCode());
        element.setCacheKey(cacheKey);
        element.setData(data);
        long timestamp = Instant.now().plusMillis(duration.toMillis()).toEpochMilli();
        element.setCacheTime(timestamp);
        RequestDbManager.getInstance().insertOrUpdate(RequestCacheElement.class, element);
        removeStaleData();
    }

    public String getRequestCacheData(String cacheKey) {
        removeStaleData();
        RequestCacheElement element = RequestDbManager.getInstance().findOne(RequestCacheElement.class, queryBuilder -> {
            queryBuilder.equal(RequestCacheElement_.cacheKey, cacheKey, QueryBuilder.StringOrder.CASE_INSENSITIVE);
        });
        if (element != null) {
            long cacheTime = element.getCacheTime();
            if (cacheTime > System.currentTimeMillis()) {
                return element.getData();
            }
        }
        return "";
    }

    private void removeStaleData() {
        long diff = System.currentTimeMillis() - preDetectionTime;
        if (diff >= 10000) {
            preDetectionTime = System.currentTimeMillis();
            RequestDbManager.getInstance().delete(RequestCacheElement.class, queryBuilder -> {
                queryBuilder.less(RequestCacheElement_.cacheTime, System.currentTimeMillis());
            });
        }
    }
}
