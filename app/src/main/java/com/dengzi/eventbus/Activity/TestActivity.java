package com.dengzi.eventbus.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.dengzi.eventbus.EventBus;
import com.dengzi.eventbus.R;

/**
 * @Title:
 * @Author: djk
 * @Time: 2017/11/20
 * @Version:1.0.0
 */
public class TestActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        // post去执行main中的方法
        findViewById(R.id.test_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        EventBus.getDefault().post("被TestActivity修改");
                    }
                }).start();
            }
        });
    }
}
