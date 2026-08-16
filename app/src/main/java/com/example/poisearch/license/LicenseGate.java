package com.example.poisearch.license;
import com.example.poisearch.ActivationActivity;
import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 启动门禁：未激活时，除白名单 Activity 外，一律跳转激活页并结束自身。
 * 效果：“输入注册码以前，搜索等其它功能均不可用”，所有接入本 license 的应用行为统一。
 *
 * 接入方式（推荐方式一，零侵入各 Activity）：
 *   自定义 Application 的 onCreate 中调用
 *       registerActivityLifecycleCallbacks(LicenseGate.create(this));
 * 也可在各功能 Activity 的 onCreate 第一行调用（方式二，作为补充/双保险）：
 *       if (LicenseGate.guard(this)) return;
 *
 * 白名单：未激活时也允许打开的页面（通常是“获取激活码”的入口），
 * 默认放行 ActivationActivity 与 OnlinePurchaseActivity，避免“没码→进不去→无法购买”死循环。
 */
public class LicenseGate {

    /** 未激活时仍允许打开的 Activity 简单名（获取/购买激活码相关） */
    private static final Set<String> ALLOWED_WHEN_LOCKED = new HashSet<>(Arrays.asList(
            "ActivationActivity",
            "OnlinePurchaseActivity"
    ));

    /**
     * 检查并拦截未激活的 Activity。
     * @return true 表示已拦截（调用方应 return，不要再初始化界面）
     */
    public static boolean guard(Activity activity) {
        if (LicenseValidator.isActivated(activity)) {
            return false;
        }
        if (ALLOWED_WHEN_LOCKED.contains(activity.getClass().getSimpleName())) {
            return false; // 放行白名单
        }
        Intent intent = new Intent(activity, ActivationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
        return true;
    }

    /**
     * 创建全局 Activity 生命周期回调，自动对所有 Activity 执行门禁（接入方式一）。
     */
    public static Application.ActivityLifecycleCallbacks create(Application app) {
        return new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                guard(activity);
            }

            @Override public void onActivityStarted(Activity activity) { }
            @Override public void onActivityResumed(Activity activity) { }
            @Override public void onActivityPaused(Activity activity) { }
            @Override public void onActivityStopped(Activity activity) { }
            @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }
            @Override public void onActivityDestroyed(Activity activity) { }
        };
    }
}
