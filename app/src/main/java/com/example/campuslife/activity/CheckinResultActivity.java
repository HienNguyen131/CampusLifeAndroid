package com.example.campuslife.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.entity.CheckInQrRequest;
import com.example.campuslife.entity.CheckInRespone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckinResultActivity extends AppCompatActivity {

    private ImageView imgResult;
    private TextView tvTitle, tvMessage;
    private Button btnBackHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin_result);

        imgResult = findViewById(R.id.imgResult);
        tvTitle = findViewById(R.id.tvResultTitle);
        tvMessage = findViewById(R.id.tvResultMessage);
        btnBackHome =findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(v -> finish());
        String qr = getIntent().getStringExtra("qr");
        callCheckInApi(qr);
    }

    private void callCheckInApi(String qr) {

        CheckInQrRequest req = new CheckInQrRequest(qr);

        ApiClient.activityRegistrations(this)
                .checkInQr(req)
                .enqueue(new Callback<ApiResponse<CheckInRespone>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<CheckInRespone>> call,
                                           Response<ApiResponse<CheckInRespone>> response) {

                        if (!response.isSuccessful()) {
                            showError("Server error");
                            return;
                        }

                        ApiResponse<CheckInRespone> res = response.body();

                        if (res != null && res.isStatus()) {
                            showSuccess(res.getData());
                        } else {
                            showError(res != null ? res.getMessage() : "Unknown error");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<CheckInRespone>> call, Throwable t) {
                        showError("Network error: " + t.getMessage());
                    }
                });
    }


    private void showSuccess(CheckInRespone data) {

        imgResult.setImageResource(R.drawable.ic_success);
        tvTitle.setText("Check-in Successful!");

        String studentName = data.getStudentName() != null
                ? data.getStudentName()
                : "Student";

        tvMessage.setText("Welcome, " + studentName + "!");
    }


    private void showError(String msg) {
        imgResult.setImageResource(R.drawable.ic_fail);
        tvTitle.setText("Check-in Failed");
        tvMessage.setText(msg);

    }
}
