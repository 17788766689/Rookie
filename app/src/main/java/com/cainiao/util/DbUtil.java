package com.cainiao.util;

import com.cainiao.base.MyApp;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 数据库操作工具类
 */
public class DbUtil {

    /**
     * 新增
     * @param obj
     * @return
     */
    public static long save(Object obj){
        return MyApp.getLiteOrm().save(obj);
    }

    /**
     * 删除
     * @param obj
     * @return
     */
    public static long delete(Object obj){
        return MyApp.getLiteOrm().delete(obj);
    }

    /**
     * 更新
     * @param obj
     * @return
     */
    public static long update(Object obj){
        return MyApp.getLiteOrm().update(obj);
    }

    /**
     * 更新列表
     * @param collection
     * @return
     */
    public static <T> long update(Collection<T> collection){
        return MyApp.getLiteOrm().update(collection);
    }

    /**
     * 查询
     * @param builder
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> query(QueryBuilder<T> builder){
        return MyApp.getLiteOrm().query(builder);
    }
}