package com.example.poisearch.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.poisearch.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LocationDialog extends Dialog {

    private RadioGroup locationTypeGroup;
    private TextInputEditText latInput;
    private TextInputEditText lngInput;
    private TextInputEditText addressInput;
    private MaterialButton confirmButton;
    private MaterialButton cancelButton;

    private double currentLat;
    private double currentLng;
    private OnLocationSelectedListener listener;

    public interface OnLocationSelectedListener {
        void onLocationSelected(double lat, double lng, String address);
        void onUseCurrentLocation();
    }

    public LocationDialog(@NonNull Context context, double currentLat, double currentLng) {
        super(context);
        this.currentLat = currentLat;
        this.currentLng = currentLng;
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_location);

        locationTypeGroup = findViewById(R.id.locationTypeGroup);
        latInput = findViewById(R.id.latInput);
        lngInput = findViewById(R.id.lngInput);
        addressInput = findViewById(R.id.addressInput);
        confirmButton = findViewById(R.id.confirmButton);
        cancelButton = findViewById(R.id.cancelButton);

        // 填充当前坐标
        latInput.setText(String.valueOf(currentLat));
        lngInput.setText(String.valueOf(currentLng));

        // 监听类型选择
        locationTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioCurrent) {
                latInput.setEnabled(false);
                lngInput.setEnabled(false);
                addressInput.setEnabled(false);
                latInput.setText(String.valueOf(currentLat));
                lngInput.setText(String.valueOf(currentLng));
            } else {
                latInput.setEnabled(true);
                lngInput.setEnabled(true);
                addressInput.setEnabled(true);
            }
        });

        confirmButton.setOnClickListener(v -> {
            int checkedId = locationTypeGroup.getCheckedRadioButtonId();
            
            if (checkedId == R.id.radioCurrent) {
                if (listener != null) {
                    listener.onUseCurrentLocation();
                }
                dismiss();
            } else if (checkedId == R.id.radioCustom) {
                try {
                    double lat = Double.parseDouble(latInput.getText().toString().trim());
                    double lng = Double.parseDouble(lngInput.getText().toString().trim());
                    String address = addressInput.getText() != null ? addressInput.getText().toString().trim() : "";
                    
                    // 验证坐标范围
                    if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
                        Toast.makeText(getContext(), "坐标范围无效", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (listener != null) {
                        listener.onLocationSelected(lat, lng, address);
                    }
                    dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "请输入有效的坐标", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }
}
