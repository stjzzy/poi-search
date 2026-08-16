package com.example.poisearch.license;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ============================================================
 *  裂变分销系统
 * ============================================================
 *  - 邀请码 = 机器码（LicenseValidator.getMachineId）
 *  - 合成专属分享海报（二维码图片按应用配置：LicenseConfig.POSTER_QR_DRAWABLE）
 *  - 跳转支付页并携带 本机机器码 + 推荐人邀请码
 *  - 分享时上报 share 事件（FissionReporter）
 * ============================================================
 */
public class ReferralSystem {

    /**
     * 获取本机邀请码（即机器码）。
     */
    public static String getMyInviteCode(Context context) {
        return LicenseValidator.getMachineId(context);
    }

    /**
     * 合成推荐海报（1080x1920）。
     * 二维码图片来自 LicenseConfig.POSTER_QR_DRAWABLE，每个应用放自己的图即可。
     */
    public static Bitmap createMyPoster(Context context) {
        String myId = getMyInviteCode(context);

        int width = 1080;
        int height = 1920;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // 背景色（按应用配置）
        canvas.drawColor(LicenseConfig.POSTER_BG_COLOR);

        // 标题
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(width * 0.09f);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(LicenseConfig.POSTER_TITLE, width / 2f, height * 0.15f, titlePaint);

        // 二维码区域（可配置资源）
        Bitmap qrCode = BitmapFactory.decodeResource(context.getResources(), LicenseConfig.POSTER_QR_DRAWABLE);
        int qrSize = (int) (width * 0.5f);
        int qrLeft = (width - qrSize) / 2;
        int qrTop = (int) (height * 0.25f);

        if (qrCode != null) {
            android.graphics.Rect destRect = new android.graphics.Rect(
                    qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize);
            canvas.drawBitmap(qrCode, null, destRect, null);
        } else {
            Paint qrBg = new Paint();
            qrBg.setColor(Color.WHITE);
            canvas.drawRect(qrLeft - 20, qrTop - 20, qrLeft + qrSize + 20, qrTop + qrSize + 20, qrBg);
            Paint qrText = new Paint(Paint.ANTI_ALIAS_FLAG);
            qrText.setColor(Color.BLACK);
            qrText.setTextSize(40);
            qrText.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("请放入二维码图片", width / 2f, qrTop + qrSize / 2f, qrText);
        }

        Paint qrHint = new Paint(Paint.ANTI_ALIAS_FLAG);
        qrHint.setColor(Color.WHITE);
        qrHint.setTextSize(width * 0.045f);
        qrHint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("扫码下载 APP", width / 2f, qrTop + qrSize + 100, qrHint);

        // 底部深色区：邀请码 + 宣传语
        Paint footerBg = new Paint();
        footerBg.setColor(Color.parseColor("#AA000000"));
        canvas.drawRect(0, height * 0.78f, width, height, footerBg);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(width * 0.065f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("专属邀请码: " + myId, width / 2f, height * 0.87f, textPaint);

        Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setColor(Color.WHITE);
        hintPaint.setTextSize(width * 0.04f);
        hintPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(LicenseConfig.POSTER_HINT, width / 2f, height * 0.93f, hintPaint);

        return result;
    }

    /**
     * 跳转支付页（携带本机机器码与推荐人码）。
     * 用于：用户填了推荐码后点击"去购买"。
     */
    public static void doPayment(Context context, String inputReferralCode) {
        String myMachineId = getMyInviteCode(context);
        String url = LicenseConfig.PAY_BASE_URL
                + "?device=" + myMachineId + "&from=" + (inputReferralCode == null ? "" : inputReferralCode);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "无法打开支付页面，请检查浏览器", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 用户点击"分享/生成海报"时调用：先上报 share 事件，再返回海报 Bitmap。
     */
    public static Bitmap onShare(Context context) {
        FissionReporter.report(context, "share", getMyInviteCode(context), "");
        return createMyPoster(context);
    }

    /**
     * 将海报 Bitmap 保存到系统相册，保存成功后弹 Toast 提示。
     */
    public static boolean savePosterToGallery(Context context, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            Toast.makeText(context, "海报未生成", Toast.LENGTH_SHORT).show();
            return false;
        }

        String fileName = "Poster_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date()) + ".jpg";

        Uri uri = null;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + LicenseConfig.APP_NAME);
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File appDir = new File(dir, LicenseConfig.APP_NAME);
                if (!appDir.exists() && !appDir.mkdirs()) {
                    throw new RuntimeException("创建相册目录失败");
                }
                File file = new File(appDir, fileName);
                values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }

            if (uri == null) {
                throw new RuntimeException("无法创建图片文件");
            }

            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                if (out == null) throw new RuntimeException("无法打开输出流");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                context.getContentResolver().update(uri, values, null, null);
            }

            Toast.makeText(context, "海报已保存到相册", Toast.LENGTH_LONG).show();
            return true;

        } catch (Exception e) {
            Toast.makeText(context, "保存海报失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    /**
     * 将海报 Bitmap 写入临时文件并通过系统分享调起微信/QQ 等。
     * 需要在 AndroidManifest 中注册 FileProvider，authority 建议为 ${applicationId}.fileprovider。
     */
    public static void sharePoster(Context context, Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            Toast.makeText(context, "海报未生成", Toast.LENGTH_SHORT).show();
            return;
        }

        File cacheDir = new File(context.getCacheDir(), "posters");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            Toast.makeText(context, "创建缓存目录失败", Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "poster_" + System.currentTimeMillis() + ".jpg";
        File posterFile = new File(cacheDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(posterFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
        } catch (Exception e) {
            Toast.makeText(context, "生成分享文件失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri;
        try {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", posterFile);
        } catch (Exception e) {
            Toast.makeText(context, "FileProvider 配置错误：" + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "推荐" + LicenseConfig.POSTER_TITLE);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "我的专属邀请码：" + getMyInviteCode(context)
                + "，扫码下载" + LicenseConfig.POSTER_TITLE + "，填码下单有优惠！");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "分享海报");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "没有可用的分享应用", Toast.LENGTH_SHORT).show();
        }
    }
}
