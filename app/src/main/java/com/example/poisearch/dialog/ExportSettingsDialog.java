package com.example.poisearch.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.poisearch.R;
import com.example.poisearch.utils.ExcelExporter;

import java.io.File;

public class ExportSettingsDialog extends DialogFragment {

    public interface ExportSettingsCallback {
        void onExportConfirmed(String customPath);
    }

    private static final String PREFS_NAME = "export_settings";
    private static final String KEY_EXPORT_PATH = "export_path";
    private static final String KEY_USE_CUSTOM_PATH = "use_custom_path";

    private ExportSettingsCallback callback;
    private EditText etCustomPath;
    private RadioGroup radioGroup;
    private RadioButton rbDefaultPath;
    private RadioButton rbCustomPath;
    private TextView tvDefaultPath;

    public void setCallback(ExportSettingsCallback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireContext();
        
        // 加载保存的设置
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPath = prefs.getString(KEY_EXPORT_PATH, "");
        boolean useCustomPath = prefs.getBoolean(KEY_USE_CUSTOM_PATH, false);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_export_settings, null);

        // 初始化视图
        radioGroup = view.findViewById(R.id.radioGroup);
        rbDefaultPath = view.findViewById(R.id.rbDefaultPath);
        rbCustomPath = view.findViewById(R.id.rbCustomPath);
        tvDefaultPath = view.findViewById(R.id.tvDefaultPath);
        etCustomPath = view.findViewById(R.id.etCustomPath);
        Button btnBrowse = view.findViewById(R.id.btnBrowse);

        // 显示默认路径（用户友好的路径）
        String defaultPath = ExcelExporter.getUserFriendlyPath(context);
        tvDefaultPath.setText("默认路径: " + defaultPath);

        // 恢复保存的设置
        if (useCustomPath && !savedPath.isEmpty()) {
            rbCustomPath.setChecked(true);
            etCustomPath.setText(savedPath);
        } else {
            rbDefaultPath.setChecked(true);
            etCustomPath.setText(savedPath.isEmpty() ? getSuggestedCustomPath() : savedPath);
        }

        // 监听单选按钮变化
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            etCustomPath.setEnabled(checkedId == R.id.rbCustomPath);
            btnBrowse.setEnabled(checkedId == R.id.rbCustomPath);
        });

        // 浏览按钮（简化版，实际应用可以使用文件选择器）
        btnBrowse.setOnClickListener(v -> {
            // 显示常用路径建议
            showPathSuggestions(context);
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("导出设置")
                .setView(view)
                .setPositiveButton("导出", (dialog, which) -> {
                    boolean isCustom = rbCustomPath.isChecked();
                    String path = isCustom ? etCustomPath.getText().toString().trim() : null;

                    // 保存设置
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(KEY_USE_CUSTOM_PATH, isCustom);
                    if (isCustom) {
                        editor.putString(KEY_EXPORT_PATH, path);
                    }
                    editor.apply();

                    if (callback != null) {
                        callback.onExportConfirmed(path);
                    }
                })
                .setNegativeButton("取消", null);

        return builder.create();
    }

    private String getSuggestedCustomPath() {
        // 建议的路径
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return new File(downloadDir, ExcelExporter.DEFAULT_FOLDER_NAME).getAbsolutePath();
    }

    private void showPathSuggestions(Context context) {
        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String documentsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        
        String[] suggestions = {
            "下载目录: " + new File(downloadPath, ExcelExporter.DEFAULT_FOLDER_NAME).getAbsolutePath(),
            "文档目录: " + new File(documentsPath, ExcelExporter.DEFAULT_FOLDER_NAME).getAbsolutePath(),
            "应用私有目录: " + ExcelExporter.getDefaultPath(context)
        };

        new AlertDialog.Builder(context)
                .setTitle("选择常用路径")
                .setItems(suggestions, (dialog, which) -> {
                    String path = suggestions[which].substring(suggestions[which].indexOf("/"));
                    etCustomPath.setText(path);
                    rbCustomPath.setChecked(true);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 获取保存的导出路径
     */
    public static String getSavedExportPath(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean useCustomPath = prefs.getBoolean(KEY_USE_CUSTOM_PATH, false);
        if (useCustomPath) {
            String path = prefs.getString(KEY_EXPORT_PATH, "");
            if (!path.isEmpty()) {
                return path;
            }
        }
        return null;
    }

    /**
     * 清除保存的路径设置
     */
    public static void clearSavedPath(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_EXPORT_PATH).remove(KEY_USE_CUSTOM_PATH).apply();
    }
}
