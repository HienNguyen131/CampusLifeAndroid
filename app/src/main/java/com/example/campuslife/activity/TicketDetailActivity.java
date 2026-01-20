package com.example.campuslife.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.Activity;
import com.example.campuslife.entity.ActivityRegistrationResponse;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.vipulasri.ticketview.TicketView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketDetailActivity extends AppCompatActivity {

    private ImageView btnBack,btnReport,btnCheckin;
    private TextView tvHeader;

    private TextView tvTicketTitle;
    private ImageView imgQrCode;
    private TextView tvTicketCode;
    private TextView tvTicketCodeValue;

    private TextView tvDateValue;
    private TextView tvRegisteredDateValue;
    private TextView tvEventDateValue;
    private TextView tvAddressValue;
    private TextView tvStudentNameValue;
    private TextView tvStudentCodeValue;
    private TextView tvStatusValue;
    private String ticketId ;
    private TicketView ticketView;

    private AppCompatButton btnCancel;
    private long activityId = -1;
    private long studentId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_ticket_detail);
        btnBack = findViewById(R.id.btnBack);
        tvHeader = findViewById(R.id.tv_header_detail_transaction);
        tvTicketTitle = findViewById(R.id.tvTicketTitle);
        imgQrCode = findViewById(R.id.imgQrCode);
        tvTicketCode = findViewById(R.id.tvTicketCode);
        tvTicketCodeValue = findViewById(R.id.TicketCode);
        tvDateValue = findViewById(R.id.tvDateValue);
        tvRegisteredDateValue = findViewById(R.id.tvRegisteredDateValue);
        tvEventDateValue = findViewById(R.id.tvEventDateValue);
        tvAddressValue = findViewById(R.id.tvAddressValue);
        tvStudentNameValue = findViewById(R.id.tvStudentNameValue);
        tvStudentCodeValue = findViewById(R.id.tvStudentCodeValue);
        tvStatusValue = findViewById(R.id.tvStatusValue);
        btnCancel = findViewById(R.id.btnCancel);
        btnReport = findViewById(R.id.btnReport);



        btnReport.setOnClickListener(v->
        {
            Intent intent = new Intent(TicketDetailActivity.this, ReportActivity.class);
            intent.putExtra("activity_id", activityId);
            intent.putExtra("student_id", studentId);
            startActivity(intent);

        });
        btnBack.setOnClickListener(v -> finish());

        btnCancel.setOnClickListener(v -> {
            if (activityId == -1) {
                Toast.makeText(this, "No valid operation code", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiClient.activityRegistrations(this)
                    .cancelRegistration(activityId, null)
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> resp) {
                            if (!resp.isSuccessful() || resp.body() == null) {
                                Toast.makeText(TicketDetailActivity.this,
                                        "HTTP " + resp.code() + " - " + resp.message(),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ApiResponse<Void> r = resp.body();
                            String msg = (r.getMessage() != null && !r.getMessage().isEmpty())
                                    ? r.getMessage()
                                    : (r.isStatus() ? "Unsubscribe successfully" : "Failed to cancel registration");

                            Toast.makeText(TicketDetailActivity.this, msg, Toast.LENGTH_LONG).show();

                            if (r.isStatus()) finish();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            Toast.makeText(TicketDetailActivity.this,
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        });




        long registrationId = getIntent().getLongExtra("registration_id", -1);
        if (registrationId == -1) {
            Toast.makeText(this, "registration_id missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fetchDetail(registrationId);

    }
    private void fetchDetail(Long id) {
        ApiClient.activityRegistrations(this)
                .detail(Long.valueOf(id))
                .enqueue(new Callback<ApiResponse<ActivityRegistrationResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<ActivityRegistrationResponse>> call,
                                           Response<ApiResponse<ActivityRegistrationResponse>> resp) {
                        if (!resp.isSuccessful() || resp.body() == null) {
                            Toast.makeText(TicketDetailActivity.this, "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        ApiResponse<ActivityRegistrationResponse> r = resp.body();
                        if (r.isStatus() && r.getData() != null) {
                            bind(r.getData());
                        } else {
                            Toast.makeText(TicketDetailActivity.this,
                                    r.getMessage() == null ? "Không lấy được dữ liệu" : r.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<ActivityRegistrationResponse>> call, Throwable t) {
                        Toast.makeText(TicketDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                        finish();
                    }
                });
    }
    private String fmtDateOnly(String isoDateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private void bind(ActivityRegistrationResponse a) {
        tvTicketTitle.setText(a.getActivityName() != null ? a.getActivityName() : "");
        tvTicketCodeValue.setText(a.getTicketCode() != null ? a.getTicketCode() : "");
        tvDateValue.setText(
                (fmtDateOnly(a.getCreatedAt())!= null ? fmtDateOnly(a.getCreatedAt()).toString() : "") );
        tvRegisteredDateValue.setText(
                (fmtDateOnly(a.getRegisteredDate())!= null ? fmtDateOnly(a.getRegisteredDate()).toString() : "") );

        tvEventDateValue.setText(
                (fmtDateOnly(a.getActivityStartDate())!= null ? fmtDateOnly(a.getActivityStartDate()).toString() : "") +
                        " - " +
                        (fmtDateOnly(a.getActivityEndDate()) != null ? fmtDateOnly(a.getActivityEndDate()).toString() : "")
        );

        tvAddressValue.setText(a.getActivityLocation() != null ? a.getActivityLocation() : "");
        tvStudentNameValue.setText(a.getStudentName() != null ? a.getStudentName() : "");
        tvStudentCodeValue.setText(a.getStudentCode() != null ? a.getStudentCode() : "");
        tvStatusValue.setText(a.getStatus() != null ? a.getStatus() : "");

        activityId = a.getActivityId();
        studentId = a.getStudentId();


        String status = a.getStatus() != null ? a.getStatus().toUpperCase() : "";
        boolean invalidStatus = status.equals("PENDING") ||
                status.equals("REJECTED") ||
                status.equals("CANCELLED") ||
                status.equals("ATTENDED");


        boolean isExpired = false;
        if (a.getActivityEndDate() != null) {
            try {
                String dateStr = a.getActivityEndDate().toString();
                LocalDate eventEnd;

                if (dateStr.contains("T")) {
                    eventEnd = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
                } else {
                    eventEnd = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                }

                LocalDate today = LocalDate.now();
                if (eventEnd.isBefore(today)) {
                    isExpired = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (invalidStatus || isExpired) {
            imgQrCode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_qr_disabled));
            tvTicketCodeValue.setText("QR code is invalid");
            tvTicketCodeValue.setTextColor(ContextCompat.getColor(this, R.color.error));
        } else if (a.getTicketCode() != null && !a.getTicketCode().isEmpty()) {
            try {
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.encodeBitmap(
                        a.getTicketCode(),
                        BarcodeFormat.QR_CODE,
                        600,
                        600
                );
                imgQrCode.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                tvTicketCodeValue.setText("QR code generation failed");
            }
        } else {
            tvTicketCodeValue.setText("QR code not available");
        }
    }



}
