package com.example.poisearch;
import com.example.poisearch.license.LicenseValidator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * 激活页：展示机器码、录入激活码。
 * 需配合 res/layout/activity_activation.xml 与 res/anim/shake.xml 使用。
 */
public class ActivationActivity extends AppCompatActivity {

    private TextView tvMachineId, tvStatus;
    private EditText etCode;
    private Button btnCopy, btnActivate;
    private String machineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        tvMachineId = findViewById(R.id.tv_machine_id);
        tvStatus    = findViewById(R.id.tv_status);
        etCode      = findViewById(R.id.et_code);
        btnCopy     = findViewById(R.id.btn_copy);
        btnActivate = findViewById(R.id.btn_activate);

        machineId = LicenseValidator.getMachineId(this);
        tvMachineId.setText(machineId);

        if (LicenseValidator.isActivated(this)) {
            tvStatus.setText("当前设备已激活");
            tvStatus.setTextColor(0xFF2E7D32);
        }

        btnCopy.setOnClickListener(v -> copyMachineId());
        btnActivate.setOnClickListener(v -> tryActivate());
    }

    private void copyMachineId() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("MachineId", machineId));
            Toast.makeText(this, "机器码已复制", Toast.LENGTH_SHORT).show();
        }
    }

    private void tryActivate() {
        String code = etCode.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "请输入激活码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (LicenseValidator.isValidLicense(this, code)) {
            LicenseValidator.setActivated(this, true);
            tvStatus.setText("激活成功！");
            tvStatus.setTextColor(0xFF2E7D32);
            Toast.makeText(this, "激活成功，正在进入…", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                setResult(RESULT_OK);
                finish();
            }, 800);
        } else {
            tvStatus.setText("激活码无效，请检查后重试");
            tvStatus.setTextColor(0xFFD32F2F);
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            etCode.startAnimation(shake);
            etCode.setBackgroundResource(R.drawable.edittext_error);
        }
    }
}
