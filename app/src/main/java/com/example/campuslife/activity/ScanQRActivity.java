package com.example.campuslife.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.campuslife.R;
import com.example.campuslife.activity.CheckinResultActivity;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.entity.CheckInQrRequest;
import com.example.campuslife.entity.CheckInRespone;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.common.util.concurrent.ListenableFuture;


import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;

public class ScanQRActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 1001;
    private PreviewView previewView;
    private Camera camera;
    private Button btnEnterCode;
    private boolean qrScanned = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        previewView = findViewById(R.id.previewView);
        btnEnterCode = findViewById(R.id.btnEnterCode);
        Button btnEnterCode = findViewById(R.id.btnEnterCode);

        btnEnterCode.setOnClickListener(v -> showEnterCodeDialog());

        if (!hasCameraPermission()) {
            requestCameraPermission();
        } else {
            startCamera();
        }
    }
    private void showEnterCodeDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_enter_code, null);
        dialog.setContentView(view);

        EditText edtCode = view.findViewById(R.id.edtEventCode);
        Button btnVerify = view.findViewById(R.id.btnVerifyCode);

        btnVerify.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();

            if (code.isEmpty()) {
                edtCode.setError("Please enter code");
                return;
            }
            Log.d("MANUAL_DATA", "Manual value = [" + code + "] length=" + code.length());


            dialog.dismiss();
            Intent intent = new Intent(this, CheckinResultActivity.class);
            intent.putExtra("qr", code);
            startActivity(intent);
            finish();
        });

        dialog.show();
    }


    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            startCamera();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera(ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient();

        imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(this),
                image -> scanBarcode(scanner, image)
        );

        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.unbindAll();
        camera = cameraProvider.bindToLifecycle(
                this, selector, preview, imageAnalysis
        );

    }

    @SuppressLint("UnsafeOptInUsageError")
    private void scanBarcode(BarcodeScanner scanner, ImageProxy imageProxy) {
        if (qrScanned) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage img = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.getImageInfo().getRotationDegrees()
            );

            scanner.process(img)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();
                            if (value != null && !qrScanned) {
                                qrScanned = true;
                                imageProxy.close();
                                Log.d("QR_DATA", "QR value = [" + value + "] length=" + value.length());

                                openResult(value);
                                return;
                            }
                        }
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    private void openResult(String qrValue) {

        Intent intent = new Intent(this, CheckinResultActivity.class);
        intent.putExtra("qr", qrValue);
        startActivity(intent);
        finish();
    }

}
