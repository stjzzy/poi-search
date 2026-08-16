package com.example.poisearch.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.poisearch.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

public class RadiusDialog extends Dialog {

    private Slider radiusSlider;
    private TextView radiusText;
    private ChipGroup presetChipGroup;
    private MaterialButton confirmButton;
    private MaterialButton cancelButton;

    private int currentRadius;
    private OnRadiusSelectedListener listener;

    public interface OnRadiusSelectedListener {
        void onRadiusSelected(int radius);
    }

    public RadiusDialog(@NonNull Context context, int currentRadius) {
        super(context);
        this.currentRadius = currentRadius;
    }

    public void setOnRadiusSelectedListener(OnRadiusSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_radius);

        // 让对话框更大：宽度占屏幕的 90%
        Window window = getWindow();
        if (window != null) {
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            window.setLayout((int)(size.x * 0.9), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }

        radiusSlider = findViewById(R.id.radiusSlider);
        radiusText = findViewById(R.id.radiusText);
        presetChipGroup = findViewById(R.id.presetChipGroup);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);

        // 设置滑块范围：500米 - 50000米（50公里）
        radiusSlider.setValueFrom(0.5f);
        radiusSlider.setValueTo(50);
        radiusSlider.setStepSize(0.5f);
        radiusSlider.setValue(currentRadius / 1000f);

        updateRadiusText(currentRadius);

        // 滑块监听
        radiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            int radius = (int) (value * 1000);
            updateRadiusText(radius);
            updatePresetSelection(radius);
        });

        // Chip 选择监听
        presetChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            int radius = getPresetRadius(checkedId);
            if (radius > 0) {
                radiusSlider.setValue(radius / 1000f);
                updateRadiusText(radius);
            }
        });

        // 初始选中预设
        updatePresetSelection(currentRadius);

        confirmButton.setOnClickListener(v -> {
            int radius = (int) (radiusSlider.getValue() * 1000);
            if (listener != null) {
                listener.onRadiusSelected(radius);
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void updateRadiusText(int radius) {
        if (radius < 1000) {
            radiusText.setText(radius + " 米");
        } else {
            radiusText.setText(String.format("%.1f 公里", radius / 1000f));
        }
    }

    private void updatePresetSelection(int radius) {
        int checkedId = -1;
        if (radius == 500) checkedId = R.id.preset500m;
        else if (radius == 1000) checkedId = R.id.preset1km;
        else if (radius == 2000) checkedId = R.id.preset2km;
        else if (radius == 5000) checkedId = R.id.preset5km;
        else if (radius == 10000) checkedId = R.id.preset10km;
        else if (radius == 20000) checkedId = R.id.preset20km;
        else if (radius == 50000) checkedId = R.id.preset50km;

        if (checkedId != -1) {
            presetChipGroup.check(checkedId);
        } else {
            presetChipGroup.clearCheck();
        }
    }

    private int getPresetRadius(int checkedId) {
        if (checkedId == R.id.preset500m) return 500;
        if (checkedId == R.id.preset1km) return 1000;
        if (checkedId == R.id.preset2km) return 2000;
        if (checkedId == R.id.preset5km) return 5000;
        if (checkedId == R.id.preset10km) return 10000;
        if (checkedId == R.id.preset20km) return 20000;
        if (checkedId == R.id.preset50km) return 50000;
        return 0;
    }
}
