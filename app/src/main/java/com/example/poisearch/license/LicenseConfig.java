package com.example.poisearch.license;

import com.example.poisearch.R;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ============================================================
 *  LicenseConfig - per-app configuration (one-device-one-code + fission marketing)
 * ============================================================
 *  Copy this file to your project, modify constants marked with [MODIFY] below.
 *  All other Java files depend on this config - only change here.
 *
 *  Important: each app MUST use a DIFFERENT SECRET_SALT,
 *  so that the same device gets different activation codes across apps.
 * ============================================================
 */
public final class LicenseConfig {

    private LicenseConfig() {}

    /* ============ 一机一码 ============ */

    /**
     * 派生盐用的「胡椒」：所有应用共用一份，改一次即可。
     * 必须与开发者端 ActivationGenerator.py 的 SALT_PEPPER 完全一致（含大小写）。
     * 应用名只是「可变部分」，真正保密的是这串 PEPPER。
     */
    public static final String SALT_PEPPER = "FISSION_LICENSE_PEPPER_V1";

    /**
     * 密钥盐值，两种用法（二选一）：
     *  - 留空字符串 ""（推荐）：自动按 APP_NAME 派生，公式与 ActivationGenerator.py 的
     *    --app 完全一致：  盐 = MD5(PEPPER + APP_NAME) 取前24位大写
     *    这样开发者端用 --app "应用名" 算码、App 端离线校验，盐自动一致，免维护。
     *  - 非空字符串：当作固定盐使用（与开发者端 --salt 一致，向后兼容）。
     * 注意：一旦改盐（或改 APP_NAME 导致派生盐变化），旧的激活码会全部失效，需重新发卡。
     */
    public static final String SECRET_SALT = "";

    /**
     * 取得当前生效的盐：SECRET_SALT 非空用固定盐，否则按 APP_NAME 自动派生。
     */
    public static String getSalt() {
        if (SECRET_SALT != null && !SECRET_SALT.isEmpty()) {
            return SECRET_SALT;
        }
        return deriveSalt(APP_NAME);
    }

    /**
     * 根据应用名确定性派生盐：MD5(PEPPER + appName) 取前24位大写。
     * 与开发者端 ActivationGenerator.py 的 derive_salt_from_app() 完全一致。
     */
    public static String deriveSalt(String appName) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest((SALT_PEPPER + appName).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) sb.append(String.format("%02X", b));
            return sb.substring(0, 24);
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    /**
     * 应用唯一标识，用于裂变上报时区分不同应用的数据。
     */
    public static final String APP_ID = "poi_search";

    /**
     * 应用名称，展示在激活页 / 海报标题等处。
     * 必须与开发者端 ActivationGenerator.py --app 传的值一字不差。
     */
    public static final String APP_NAME = "附近POI搜索";

    /* ============ 裂变海报 ============ */

    public static final String POSTER_TITLE = "附近POI搜索";

    public static final int POSTER_BG_COLOR = 0xFF1565C0;

    /**
     * 海报上的二维码图片资源。
     * 每个应用放自己的二维码图到 res/drawable，例如 R.drawable.qrcode_download，
     * 没有图时会画一个占位白框并提示"请放入二维码图片"。
     */
    public static final int POSTER_QR_DRAWABLE = R.drawable.qrcode_download;

    public static final String POSTER_HINT = "填码下单立减20元 | 推荐有奖";

    public static final int REWARD_AMOUNT = 50;

    /* ============ 支付 / 上报 ============ */
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
    /**
     * 跳转支付的基础链接（doPayment 会拼接 device=机器码&from=推荐码）。
     * TODO: 替换为你的实际发卡/支付链接
     */
    public static final String PAY_BASE_URL = "https://your-payment-url.example.com";

    /**
     * 裂变事件上报地址（FissionReporter 使用）。
     * 部署 fission_server.py 后填：http://你的服务器IP:5000/report
     * 不需要上报留空字符串即可。
     */
    public static final String FISSION_REPORT_URL = "";

    /**
     * 在线购买页：有推荐码 / 无推荐码的支付金额（元）。
     */
    public static final double PRICE_WITH_REFERRAL    = 180.0;
    public static final double PRICE_WITHOUT_REFERRAL = 200.0;

    /**
     * 客服微信（购买页"复制客服微信"按钮用），留空则隐藏客服区块。
     */
    public static final String SERVICE_WECHAT = "";

    /* ============ 麦客表单对接（在线购买页完整 POST 版） ============ */
    /* TODO: 如需使用在线购买页的麦客表单提交功能，替换以下为你的实际表单参数 */

    public static final String MIKE_FORM_BASE_HOST = "https://your-form-host.mikecrm.com";

    public static final String MIKE_FORM_SUBMIT_URL =
            "https://your-form-host.mikecrm.com/handler/web/form_runtime/handleSubmit.php";

    public static final int    MIKE_FORM_I   = 0;
    public static final String MIKE_FORM_T   = "";
    public static final int    MIKE_FORM_S   = 0;
    public static final String MIKE_FORM_ACC = "";

    public static final String MIKE_FIELD_QTY      = "";
    public static final String MIKE_FIELD_OPTION   = "";
    public static final String MIKE_FIELD_PHONE    = "";
    public static final String MIKE_FIELD_REFERRAL = "";
    public static final String MIKE_FIELD_DEVICE   = "";
    public static final String MIKE_FIELD_APP_NAME = "";
}
