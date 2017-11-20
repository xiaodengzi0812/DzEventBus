package com.dengzi.eventbus;

import java.lang.reflect.Method;

/**
 * @Title: SubscriberMethod业务bean, 存放一些方法执行的参数
 * @Author: djk
 * @Time: 2017/11/20
 * @Version:1.0.0
 */
public class SubscriberMethod {
    // 执行的方法
    final Method method;
    // 方法执行的线程模式
    final ThreadMode threadMode;
    // 方法属性
    final Class<?> eventType;
    // 优先级
    final int priority;
    // 黏性事件开关
    final boolean sticky;

    public SubscriberMethod(Method method, Class<?> eventType, ThreadMode threadMode, int priority, boolean sticky) {
        this.method = method;
        this.threadMode = threadMode;
        this.eventType = eventType;
        this.priority = priority;
        this.sticky = sticky;
    }
}