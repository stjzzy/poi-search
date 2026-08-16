package com.example.poisearch.license;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ============================================================
 *  一机一码授权校验器  (MD5 + 盐 对称方案)
 * ============================================================
 *  机器码 = MD5(AndroidId) 取前12位 -> XXXX-XXXX-XXXX
 *  激活码 = MD5(机器码 + SECRET_SALT) 取前12位 -> XXXX-XXXX-XXXX
 *
 *  算法与开发者端 scripts/gen_code.py 完全一致，确保离线可校验。
 *  不同应用用不同 SECRET_SALT -> 同设备注册码不同（见 LicenseConfig）。
 * ============================================================
 */
public class LicenseValidator {

    private static final String PREFS_NAME = "license_prefs";
    private static final String KEY_ACTIVATED = "activated";

    /**
     * 获取设备的唯一机器码（与开发者端算号器输入的“机器码”一致）。
     * 注意：仅依赖 Android ID，不含盐，因此同一设备在各应用上机器码相同；
     *       但“激活码”因盐不同而不同。
     */
    public static String getMachineId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (androidId == null || androidId.isEmpty()) {
            androidId = "DEFAULT_DEVICE_ID";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(androidId.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            String upper = hex.toString().toUpperCase();
            return upper.substring(0, 4) + "-" + upper.substring(4, 8) + "-" + upper.substring(8, 12);
        } catch (NoSuchAlgorithmException e) {
            return androidId;
        }
    }

    /**
     * 由机器码计算激活码（与开发者端 gen_code.py 一致）。
     */
    public static String computeActivationCode(String machineId) {
        try {
            String combined = machineId + LicenseConfig.SECRET_SALT;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(combined.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String hash = sb.toString().toUpperCase();
            return hash.substring(0, 4) + "-" + hash.substring(4, 8) + "-" + hash.substring(8, 12);
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }

    /**
     * 校验用户填入的激活码是否匹配本机。
     */
    public static boolean isValidLicense(Context context, String activationCode) {
        if (activationCode == null || activationCode.trim().isEmpty()) return false;
        String expected = computeActivationCode(getMachineId(context));
        return expected.equalsIgnoreCase(activationCode.trim());
    }

    /* ---------- 激活状态持久化 ---------- */

    public static boolean isActivated(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_ACTIVATED, false);
    }

    public static void setActivated(Context context, boolean activated) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ACTIVATED, activated).apply();
    }
}
