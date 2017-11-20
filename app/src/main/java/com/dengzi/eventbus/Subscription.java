package com.dengzi.eventbus;

/**
 * @Title: Subscription业务bean
 * 包含两个属性，一个是 subscriber 订阅者（反射执行对象）
 * 一个是 SubscriberMethod 注解方法的所有属性参数值
 * @Author: djk
 * @Time: 2017/11/20
 * @Version:1.0.0
 */
final class Subscription {
    final Object subscriber;
    final SubscriberMethod subscriberMethod;
    volatile boolean active;

    Subscription(Object subscriber, SubscriberMethod subscriberMethod) {
        this.subscriber = subscriber;
        this.subscriberMethod = subscriberMethod;
        active = true;
    }

}