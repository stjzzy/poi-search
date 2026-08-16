package com.example.poisearch;
import com.example.poisearch.license.LicenseConfig;
import com.example.poisearch.license.LicenseValidator;
import com.example.poisearch.license.FissionReporter;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 在线购买激活码页面（完整麦客表单对接版）。
 *
 * 填写机器码、手机号、邀请码后提交麦客表单，获取微信支付二维码；
 * 提交时带上本应用名称（自动取应用清单 label，回退到 LicenseConfig.APP_NAME，
 * 对应麦客「应用名」字段 MIKE_FIELD_APP_NAME），便于多个应用共用同一个麦客表单时按应用区分订单。
 *
 * 所有麦客表单参数来自 LicenseConfig（MIKE_*），换应用只改 LicenseConfig 一处即可。
 */
public class OnlinePurchaseActivity extends Activity {

    private static final String TAG = "OnlinePurchase";

    private TextView tvMachineId, tvPriceDisplay, tvOrderNo, tvAppName, tvWechatId;
    private EditText etPhone, etReferralCode;
    private Button btnCopyMachineId, btnSubmit, btnSaveQr, btnCopyWechat;
    private LinearLayout layoutResult, layoutWechat;
    private ImageView ivQrCode;

    private String machineId;
    private String appName;
    private Bitmap qrBitmap;
    private static final int REQ_PERMISSION_SAVE_QR = 2002;
    private Bitmap pendingQrBitmap;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_purchase);

        machineId = LicenseValidator.getMachineId(this);

        String label = "";
        try {
            label = getApplicationInfo().loadLabel(getPackageManager()).toString();
        } catch (Exception ignored) {}
        appName = (label == null || label.isEmpty()) ? LicenseConfig.APP_NAME : label;

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvMachineId    = findViewById(R.id.tv_machine_id);
        tvPriceDisplay = findViewById(R.id.tv_price_display);
        tvOrderNo      = findViewById(R.id.tv_order_no);
        tvAppName      = findViewById(R.id.tv_app_name);
        tvWechatId     = findViewById(R.id.tv_wechat_id);
        etPhone        = findViewById(R.id.et_phone);
        etReferralCode = findViewById(R.id.et_referral_code);
        btnCopyMachineId = findViewById(R.id.btn_copy_machine_id);
        btnSubmit      = findViewById(R.id.btn_submit);
        btnSaveQr      = findViewById(R.id.btn_save_qr);
        btnCopyWechat  = findViewById(R.id.btn_copy_wechat);
        layoutResult   = findViewById(R.id.layout_result);
        layoutWechat   = findViewById(R.id.layout_wechat);

        tvMachineId.setText(machineId);

        if (tvAppName != null) tvAppName.setText(appName);
        if (tvWechatId != null) tvWechatId.setText(LicenseConfig.SERVICE_WECHAT);
        if (layoutWechat != null
                && (LicenseConfig.SERVICE_WECHAT == null || LicenseConfig.SERVICE_WECHAT.isEmpty())) {
            layoutWechat.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION_SAVE_QR) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                if (pendingQrBitmap != null) {
                    saveQrToGallery(pendingQrBitmap);
                    pendingQrBitmap = null;
                }
            } else {
                Toast.makeText(this, "未获得存储权限，无法保存图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupListeners() {
        btnCopyMachineId.setOnClickListener(v -> copyToClipboard("MachineId", machineId, "机器码已复制"));

        etReferralCode.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String code = s.toString().trim();
                double price = code.isEmpty() || !isValidReferralCode(code)
                        ? LicenseConfig.PRICE_WITHOUT_REFERRAL
                        : LicenseConfig.PRICE_WITH_REFERRAL;
                tvPriceDisplay.setText("¥" + (long) price);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSubmit.setOnClickListener(v -> doSubmit());

        btnSaveQr.setOnClickListener(v -> {
            if (qrBitmap != null) {
                saveQrToGallery(qrBitmap);
            }
        });

        btnCopyWechat.setOnClickListener(v ->
                copyToClipboard("WechatId", LicenseConfig.SERVICE_WECHAT, "客服微信已复制"));
    }

    private boolean isValidReferralCode(String code) {
        if (code == null || code.isEmpty()) return false;
        if (!code.matches("^[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}$")) return false;
        if (machineId != null && code.equalsIgnoreCase(machineId)) return false;
        return true;
    }

    private void doSubmit() {
        String phone    = etPhone.getText().toString().trim();
        String referral = etReferralCode.getText().toString().trim();

        if (phone.isEmpty() || phone.length() < 11) {
            Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!referral.isEmpty() && !isValidReferralCode(referral)) {
            Toast.makeText(this,
                    "邀请码格式不正确（应为 XXXX-XXXX-XXXX，且不能填自己的机器码）",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("提交中...");

        boolean hasReferral = isValidReferralCode(referral);
        double price = hasReferral ? LicenseConfig.PRICE_WITH_REFERRAL : LicenseConfig.PRICE_WITHOUT_REFERRAL;

        FissionReporter.report(this, "purchase", machineId, referral, price);

        executor.execute(() -> {
            String result = submitForm(phone, referral, machineId, price);
            mainHandler.post(() -> handleResult(result));
        });
    }

    private String submitForm(String phone, String referral, String deviceId,
                              double price) {
        HttpURLConnection conn = null;
        try {
            String cvsJson = buildCvsJson(phone, referral, deviceId, price);
            String postBody = "d=" + URLEncoder.encode(cvsJson, "UTF-8");

            URL url = new URL(LicenseConfig.MIKE_FORM_SUBMIT_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
            conn.setRequestProperty("Referer", LicenseConfig.MIKE_FORM_BASE_HOST + "/" + LicenseConfig.MIKE_FORM_T);
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postBody.getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            Log.d(TAG, "HTTP状态码: " + code);

            InputStream is = (code >= 200 && code < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            return sb.toString();

        } catch (Exception e) {
            Log.e(TAG, "提交异常: " + e.getMessage(), e);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String buildCvsJson(String phone, String referral, String deviceId,
                                 double price) throws Exception {
        int optionValue = (int) Math.round(price / 0.01);

        String cpJson = "{"
            + "\"" + LicenseConfig.MIKE_FIELD_QTY + "\":{\"" + LicenseConfig.MIKE_FIELD_OPTION + "\":" + optionValue + "},"
            + "\"" + LicenseConfig.MIKE_FIELD_PHONE + "\":[\"" + phone + "\"],"
            + "\"" + LicenseConfig.MIKE_FIELD_REFERRAL + "\":\"" + (referral.isEmpty() ? "无邀请码" : referral) + "\","
            + "\"" + LicenseConfig.MIKE_FIELD_DEVICE + "\":\"" + deviceId + "\","
            + "\"" + LicenseConfig.MIKE_FIELD_APP_NAME + "\":\"" + appName + "\""
            + "}";

        String extJson = "{\"cashier\":{\"c\":1,\"tp\":" + price
            + ",\"d\":2,\"m\":2,\"pvt\":10}}";

        String cJson = "{\"cp\":" + cpJson + ",\"ext\":" + extJson + "}";

        String cvsJson = "{\"cvs\":{\"i\":" + LicenseConfig.MIKE_FORM_I
            + ",\"t\":\"" + LicenseConfig.MIKE_FORM_T + "\""
            + ",\"s\":" + LicenseConfig.MIKE_FORM_S
            + ",\"acc\":\"" + LicenseConfig.MIKE_FORM_ACC + "\""
            + ",\"r\":\"\""
            + ",\"c\":" + cJson
            + "}}";

        Log.d(TAG, "提交JSON: " + cvsJson);
        return cvsJson;
    }

    private void handleResult(String responseJson) {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("立即支付 获取激活码");

        if (responseJson == null) {
            Toast.makeText(this, "网络请求失败，请检查网络后重试", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "服务器返回: " + responseJson);

        try {
            JSONObject json = new JSONObject(responseJson);
            int r = json.optInt("r", -1);

            if (r == 0) {
                JSONObject cashier = json.optJSONObject("cashier");
                String orderNo = cashier != null ? cashier.optString("ordiNo", "—") : "—";
                String qrPath  = cashier != null ? cashier.optString("rWx_qrPath", "") : "";
                String qrCodeUrl = cashier != null ? cashier.optString("rWx_qrCode", "") : "";

                tvOrderNo.setText("订单号：" + orderNo);
                layoutResult.setVisibility(View.VISIBLE);

                if (!qrPath.isEmpty()) {
                    loadQrImage(LicenseConfig.MIKE_FORM_BASE_HOST + qrPath);
                } else if (!qrCodeUrl.isEmpty()) {
                    ivQrCode.setBackgroundColor(android.graphics.Color.parseColor("#E5E7EB"));
                    Toast.makeText(this, "二维码链接已获取，请在微信中打开", Toast.LENGTH_LONG).show();
                }

                Toast.makeText(this, "提交成功！请扫码支付后联系客服", Toast.LENGTH_LONG).show();
            } else {
                String msg = json.optString("msg", "提交失败，请重试");
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "解析返回结果失败: " + e.getMessage());
            tvOrderNo.setText("服务器响应异常，请联系客服");
            layoutResult.setVisibility(View.VISIBLE);
            Toast.makeText(this, "提交已发送，如未收到二维码请联系客服", Toast.LENGTH_LONG).show();
        }
    }

    private void loadQrImage(final String imageUrl) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(imageUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Referer", LicenseConfig.MIKE_FORM_BASE_HOST + "/" + LicenseConfig.MIKE_FORM_T);
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                if (bmp != null) {
                    qrBitmap = bmp;
                    mainHandler.post(() -> {
                        ivQrCode.setImageBitmap(bmp);
                        ivQrCode.setBackgroundColor(android.graphics.Color.WHITE);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "加载二维码图片失败: " + e.getMessage());
                mainHandler.post(() ->
                    Toast.makeText(this, "二维码图片加载失败，请手动打开支付链接", Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private void saveQrToGallery(Bitmap bitmap) {
        if (bitmap == null) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                this.pendingQrBitmap = bitmap;
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQ_PERMISSION_SAVE_QR);
                return;
            }
        }

        try {
            String fileName = "PayQR_" + System.currentTimeMillis() + ".jpg";
            Uri uri = null;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PayQR");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, fileName);
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }

            if (uri != null) {
                try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(uri, values, null, null);
                }

                Toast.makeText(this, "二维码已保存至相册！", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "保存失败: " + e.getMessage());
            Toast.makeText(this, "保存失败，请截图保存二维码", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyToClipboard(String label, String text, String toastMsg) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText(label, text));
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
