package com.dengzi.eventbus;

import android.os.Handler;
import android.os.Looper;

import com.dengzi.eventbus.Activity.MainActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.dengzi.eventbus.ThreadMode.*;

/**
 * @author Djk
 * @Title:
 * @Time: 2017/11/20.
 * @Version:1.0.0
 */
public class EventBus {
    private static volatile EventBus mInstance;

    // key 是 Event 参数的类 value 存放的是 Subscription 的集合列表
    // Subscription 包含两个属性，一个是 subscriber 订阅者（反射执行对象），一个是 SubscriberMethod 注解方法的所有属性参数值
    private final Map<Class<?>, CopyOnWriteArrayList<Subscription>> subscriptionsByEventType;

    // key 是所有的订阅者 value 是所有订阅者里面方法的参数的class
    private final Map<Object, List<Class<?>>> typesBySubscriber;

    private EventBus() {
        subscriptionsByEventType = new HashMap<>();
        typesBySubscriber = new HashMap<>();
    }

    public static EventBus getDefault() {
        if (mInstance == null) {
            synchronized (EventBus.class) {
                if (mInstance == null) {
                    mInstance = new EventBus();
                }
            }
        }
        return mInstance;
    }


    /**
     * 注册一个事件
     *
     * @param subscriber
     */
    public void register(Object subscriber) {
        // 获取class
        Class<?> subscriberClass = subscriber.getClass();
        // 获取所有的方法
        Method[] methods = subscriberClass.getDeclaredMethods();
        for (Method method : methods) {
            // 获取带Subscribe注解的方法
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                Class<?>[] typeClass = method.getParameterTypes();
                if (typeClass.length == 1) {
                    Class<?> typeClazz = typeClass[0];
                    CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(typeClazz);
                    if (subscriptions == null) {
                        subscriptions = new CopyOnWriteArrayList<>();
                        subscriptionsByEventType.put(typeClazz, subscriptions);
                    }
                    SubscriberMethod subscriberMethod = new SubscriberMethod(
                            method,
                            typeClazz,
                            subscribe.threadMode(),
                            subscribe.priority(),
                            subscribe.sticky()
                    );
                    Subscription subscription = new Subscription(subscriber, subscriberMethod);
                    subscriptions.add(subscription);

                    List<Class<?>> typeList = typesBySubscriber.get(subscriber);
                    if (typeList == null) {
                        typeList = new ArrayList<>();
                        typesBySubscriber.put(subscriber, typeList);
                    }
                    typeList.add(typeClazz);
                }
            }
        }
    }

    /**
     * 取消注册一个事件
     *
     * @param subscriber
     */
    public void unregister(Object subscriber) {
        List<Class<?>> typeList = typesBySubscriber.get(subscriber);
        if (typeList != null) {
            removeTypeList(typeList);
        }
    }

    private void removeTypeList(List<Class<?>> typeList) {
        Iterator<Class<?>> iterator = typeList.iterator();
        while (iterator.hasNext()) {
            iterator.remove();
        }
    }

    /**
     * 发送一个消息
     *
     * @param msgObject
     */
    public void post(final Object msgObject) {
        Class<?> typeClass = msgObject.getClass();
        // 判断是不是主线程
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(typeClass);
        for (final Subscription subscription : subscriptions) {
            ThreadMode threadMode = subscription.subscriberMethod.threadMode;
            switch (threadMode) {
                case MAIN:// 在主线程中执行
                    if (isMainThread) {
                        invokeMethod(subscription, msgObject);
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                invokeMethod(subscription, msgObject);
                            }
                        });
                    }
                    break;
                case BACKGROUND:// 子线程：如果发布事件的线程是主线程，那么调用线程池中的子线程来执行订阅方法；否则直接执行；
                    if (isMainThread) {
                        AsyncPoster.enqueue(subscription, msgObject);
                    } else {
                        invokeMethod(subscription, msgObject);
                    }
                    break;
                case ASYNC:
                    AsyncPoster.enqueue(subscription, msgObject);
                    break;
                case POSTING:// 同一个线程，在哪个线程发送事件，那么该方法就在哪个线程执行
                    invokeMethod(subscription, msgObject);
                    break;
            }
        }
    }

    /**
     * 反射执行方法
     *
     * @param subscription
     * @param msgObject
     */
    private void invokeMethod(Subscription subscription, Object msgObject) {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, msgObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
