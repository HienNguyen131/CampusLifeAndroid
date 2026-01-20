package com.example.campuslife.auth;

import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request();
        String path = req.url().encodedPath();

        if (path.startsWith("/api/auth/")) {
            return chain.proceed(req);
        }


        String token = TokenStore.getToken(context);

        if (!TextUtils.isEmpty(token)) {
            req = req.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        if (req.header("Accept") == null) {
            req = req.newBuilder()
                    .addHeader("Accept", "application/json")
                    .build();
        }


        if (req.body() != null && req.header("Content-Type") == null) {
            req = req.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .build();
        }

        return chain.proceed(req);
    }
}
