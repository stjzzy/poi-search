package com.example.poisearch;

import android.app.Application;

import com.example.poisearch.license.LicenseGate;

/**
 * 自定义 Application：注册全局授权门禁。
 * 未激活时，除白名单页面（ActivationActivity / OnlinePurchaseActivity）外，
 * 所有 Activity 会被自动拦截跳转到激活页。
 */
public class PoiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(LicenseGate.create(this));
    }
}
