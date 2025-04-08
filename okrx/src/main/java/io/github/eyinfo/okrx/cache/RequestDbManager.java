package io.github.eyinfo.okrx.cache;

import android.content.Context;

import com.eyinfo.android_pure_utils.events.Action1;
import com.eyinfo.storage_objectbox.ObjectBoxAbstractManager;
import com.eyinfo.storage_objectbox.PageInfo;

import java.util.List;

import io.github.eyinfo.okrx.beans.MyObjectBox;
import io.objectbox.query.QueryBuilder;

public class RequestDbManager extends ObjectBoxAbstractManager {
    private static volatile RequestDbManager instance;

    public static RequestDbManager getInstance() {
        if (instance == null) {
            synchronized (RequestDbManager.class) {
                if (instance == null) {
                    instance = new RequestDbManager();
                }
            }
        }
        return instance;
    }

    private final String dbKey = "okrx.objectbox";

    /**
     * 初始化BoxStore
     *
     * @param applicationContext 上下文
     */
    public void init(Context applicationContext) {
        super.init(applicationContext, MyObjectBox.builder(), dbKey);
    }

    /**
     * 插入或更新
     *
     * @param clazz    模型类型
     * @param elements 待插入或更新的实体集合
     * @param <T>
     */
    public <T> void insertOrUpdate(Class<T> clazz, List<T> elements) {
        super.insertOrUpdate(dbKey, clazz, elements);
    }

    /**
     * 插入或更新
     *
     * @param clazz   模型类型
     * @param element 待插入或更新的实体
     * @param <T>
     */
    public <T> void insertOrUpdate(Class<T> clazz, T element) {
        super.insertOrUpdate(dbKey, clazz, element);
    }

    /**
     * 根据id查询
     *
     * @param clazz 模型类型
     * @param id    主键id
     * @param <T>
     * @return
     */
    public <T> T findById(Class<T> clazz, Long id) {
        return super.findById(dbKey, clazz, id);
    }

    /**
     * 根据条件查询
     *
     * @param dbKey 数据库唯一标识
     * @param clazz 模型类型
     * @param call  查询条件
     * @param <T>
     * @return
     */
    public <T> T findOne(Class<T> clazz, Action1<QueryBuilder<T>> call) {
        return super.findOne(dbKey, clazz, call);
    }

    /**
     * 根据条件查询数据集合
     *
     * @param clazz 模型类型
     * @param call  查询条件
     * @param <T>
     * @return
     */
    public <T> List<T> findList(Class<T> clazz, Action1<QueryBuilder<T>> call) {
        return super.findList(dbKey, clazz, call);
    }

    /**
     * 根据条件查询数据集合
     *
     * @param clazz 模型类型
     * @param <T>
     * @return
     */
    public <T> List<T> findAll(Class<T> clazz) {
        return super.findAll(dbKey, clazz);
    }

    /**
     * 查询总数
     *
     * @param clazz 模型类型
     * @param call  查询条件
     * @param <T>
     * @return
     */
    public <T> int count(Class<T> clazz, Action1<QueryBuilder<T>> call) {
        return super.count(dbKey, clazz, call);
    }

    /**
     * 分页查询
     *
     * @param clazz 模型类型
     * @param page  起始页码
     * @param limit 每页数量
     * @param call  查询条件
     * @param <T>
     * @return
     */
    public <T> PageInfo<T> queryPage(Class<T> clazz, int page, int limit, Action1<QueryBuilder<T>> call) {
        return super.queryPage(dbKey, clazz, page, limit, call);
    }

    /**
     * 根据条件删除数据
     *
     * @param clazz 模型类型
     * @param call  查询条件
     * @param <T>
     */
    public <T> void delete(Class<T> clazz, Action1<QueryBuilder<T>> call) {
        super.delete(dbKey, clazz, call);
    }
}
