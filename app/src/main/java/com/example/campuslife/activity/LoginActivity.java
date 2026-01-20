package com.example.campuslife.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campuslife.R;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;
import com.example.campuslife.api.AuthApi;


import com.example.campuslife.api.DeviceTokenApi;
import com.example.campuslife.auth.LoginRequest;
import com.example.campuslife.auth.TokenResponse;
import com.example.campuslife.auth.TokenStore;
import com.example.campuslife.fragment.HomeFragment;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUser, edtPass;
    private TextView tvForgot;
    private View btnLogin, progress;
    private TokenStore tokenStore;
    private AuthApi authApi;
    private boolean isCalling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        authApi = ApiClient.authNoAuth(this);

        edtUser = findViewById(R.id.edtEmail);
        edtPass = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        //progress = findViewById(R.id.progress);
        tvForgot = findViewById(R.id.tvForgot);
        tvForgot.setOnClickListener(v->{
            Intent intent = new Intent(this,ForgotActivity.class);
            startActivity(intent);
        });
        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        if (isCalling) return;

        String u = edtUser.getText().toString().trim();
        String p = edtPass.getText().toString().trim();
        if (u.isEmpty() || p.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ username & password", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();
        showLoading(true);

        isCalling = true;
        authApi.login(new LoginRequest(u, p)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> resp) {
                showLoading(false);
                isCalling = false;

                if (!resp.isSuccessful() || resp.body() == null) {
                    if (resp.code() == 401) {
                        Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Lỗi server: " + resp.code(), Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                TokenResponse body = resp.body();

                if (body.status && body.body != null) {
                    String access = body.body.token;
                    String refresh = body.body.refreshToken;

                    TokenStore.saveToken(LoginActivity.this, access);

                    if (refresh != null && !refresh.isEmpty()) {
                        TokenStore.saveRefreshToken(LoginActivity.this, refresh);
                    }

                    TokenStore.saveUsername(LoginActivity.this, u);


                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(token -> {

                                Log.d("FCM", "🔥 FCM token after login = " + token);

                                Map<String, String> bodyToken = new HashMap<>();
                                bodyToken.put("token", token);

                                ApiClient.device(LoginActivity.this)
                                        .saveToken(bodyToken)
                                        .enqueue(new Callback<ApiResponse<Void>>() {
                                            @Override
                                            public void onResponse(Call<ApiResponse<Void>> call,
                                                                   Response<ApiResponse<Void>> response) {
                                                Log.d("FCM", " Token saved to backend");
                                            }

                                            @Override
                                            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                                                Log.e("FCM", " Save token failed", t);
                                            }
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FCM", " Cannot get FCM token", e);
                            });
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                }
                else {
                    Toast.makeText(LoginActivity.this,
                            body.message == null ? "Đăng nhập thất bại" : body.message,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                showLoading(false);
                isCalling = false;
                Toast.makeText(LoginActivity.this, "Không thể kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showLoading(boolean show) {
        if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
        if (btnLogin != null) btnLogin.setEnabled(!show);
    }

    private void hideKeyboard() {
        View v = getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
