package com.example.campuslife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.AuthApi;
import com.example.campuslife.api.ForgotApi;
import com.example.campuslife.entity.ForgotPasswordRequest;
import com.example.campuslife.entity.SubmitResultResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.core.content.ContentProviderCompat.requireContext;

public class ForgotActivity extends AppCompatActivity {
    private TextInputEditText edtEmail;
    private TextView btnSignIn;
    private MaterialButton btnSendCode;
    private ForgotApi forgotApi;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        edtEmail= findViewById(R.id.edtEmail);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnSendCode= findViewById(R.id.btnSendCode);
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
        btnSendCode.setEnabled(false);
        forgotApi= ApiClient.forgot(this);
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                btnSendCode.setEnabled(!email.isEmpty());
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        btnSendCode.setOnClickListener(v->forgotPass());
    }
    private void forgotPass() {

        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(ForgotActivity.this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            return;
        }

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        forgotApi.forgotPassword(request)
                .enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            Toast.makeText(ForgotActivity.this,
                                    "Mã xác thực đã được gửi đến email!",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(ForgotActivity.this, LoginActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);

                        } else {
                            Toast.makeText(ForgotActivity.this,
                                    "Không thể gửi mã. Vui lòng thử lại!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Toast.makeText(ForgotActivity.this,
                                "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
