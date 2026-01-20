package com.example.campuslife.auth;

import android.content.Context;

import com.example.campuslife.BuildConfig;
import com.example.campuslife.api.AuthApi;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RefreshAuthenticator implements Authenticator {

    private final Context context;
    private final OkHttpClient noAuthClient;
    private final AuthApi noAuthApi;

    public RefreshAuthenticator(Context ctx) {
        this.context = ctx.getApplicationContext();

        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.BASIC);

        this.noAuthClient = new OkHttpClient.Builder()
                .addInterceptor(log)
                .retryOnConnectionFailure(true)
                .build();

        Retrofit r = new Retrofit.Builder()
                .baseUrl(ensureSlash(BuildConfig.BASE_URL))
                .client(noAuthClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        this.noAuthApi = r.create(AuthApi.class);
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        // Ngăn vòng lặp vô hạn
        if (countPriorResponses(response) >= 2) return null;

        String oldToken = TokenStore.getToken(context);
        String newToken = tryRefresh();

        if (newToken == null || newToken.equals(oldToken)) {
            return null; // refresh fail → giữ 401 → app sẽ logout
        }

        return response.request().newBuilder()
                .header("Authorization", "Bearer " + newToken)
                .build();
    }

    private String tryRefresh() {
        String refreshToken = TokenStore.getRefreshToken(context);
        if (refreshToken == null || refreshToken.isEmpty()) return null;

        try {
            retrofit2.Response<TokenResponse> resp =
                    noAuthApi.refresh(new RefreshRequest(refreshToken)).execute();

            if (resp.isSuccessful() && resp.body() != null
                    && resp.body().status && resp.body().body != null) {

                String newAccess = resp.body().body.token;
                String newRefresh = resp.body().body.refreshToken;

                if (newAccess != null && !newAccess.isEmpty())
                    TokenStore.saveToken(context, newAccess);

                if (newRefresh != null && !newRefresh.isEmpty())
                    TokenStore.saveRefreshToken(context, newRefresh);

                return newAccess;
            }

        } catch (Exception ignored) {
        }
        return null;
    }

    private static int countPriorResponses(Response resp) {
        int count = 0;
        while ((resp = resp.priorResponse()) != null) count++;
        return count;
    }

    private static String ensureSlash(String b) {
        return b.endsWith("/") ? b : (b + "/");
    }
}
