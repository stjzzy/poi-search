package com.example.poisearch.license;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ============================================================
 *  裂变事件上报器
 * ============================================================
 *  把 share / purchase 事件上报到 LicenseConfig.FISSION_REPORT_URL。
 *  上报体（JSON）：
 *  {
 *    "event": "share" | "purchase",
 *    "machineId": "本机机器码(即邀请码)",
 *    "referralCode": "推荐人邀请码(无则空字符串)",
 *    "appId": "应用标识(LicenseConfig.APP_ID)",
 *    "price": 支付金额(仅 purchase 有)
 *  }
 *  服务端示例见 scripts/fission_server.py。
 *  若 FISSION_REPORT_URL 为空则安静跳过（不影响主流程）。
 * ============================================================
 */
public class FissionReporter {

    private static final String TAG = "FissionReporter";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void report(Context context, String event, String machineId, String referralCode) {
        report(context, event, machineId, referralCode, -1.0);
    }

    public static void report(Context context, String event, String machineId,
                              String referralCode, double price) {
        if (LicenseConfig.FISSION_REPORT_URL == null
                || LicenseConfig.FISSION_REPORT_URL.trim().isEmpty()) {
            return; // 未配置上报地址，跳过
        }
        final String myMachineId = (machineId == null || machineId.isEmpty())
                ? LicenseValidator.getMachineId(context) : machineId;
        final String ref = (referralCode == null) ? "" : referralCode;

        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject body = new JSONObject();
                body.put("event", event);
                body.put("machineId", myMachineId);
                body.put("referralCode", ref);
                body.put("appId", LicenseConfig.APP_ID);
                if (price >= 0) body.put("price", price);

                URL url = new URL(LicenseConfig.FISSION_REPORT_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }
                int code = conn.getResponseCode();
                Log.d(TAG, "上报 " + event + " -> HTTP " + code);
            } catch (Exception e) {
                Log.e(TAG, "上报失败: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }
}
