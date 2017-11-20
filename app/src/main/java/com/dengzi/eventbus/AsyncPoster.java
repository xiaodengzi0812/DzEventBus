package com.dengzi.eventbus;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Title: 异步执行线程池
 * @Author: djk
 * @Time: 2017/11/20
 * @Version:1.0.0
 */
public class AsyncPoster implements Runnable {
    Subscription subscription;
    Object event;

    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    public AsyncPoster(Subscription subscription, Object event) {
        this.subscription = subscription;
        this.event = event;
    }

    public static void enqueue(Subscription subscription, Object event) {
        AsyncPoster asyncPoster = new AsyncPoster(subscription, event);
        // 用线程池
        executorService.execute(asyncPoster);
    }

    @Override
    public void run() {
        try {
            subscription.subscriberMethod.method.invoke(subscription.subscriber, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
