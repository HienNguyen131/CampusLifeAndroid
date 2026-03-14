package com.example.campuslife.api;

import android.content.Context;
import com.example.campuslife.BuildConfig;
import com.example.campuslife.activity.ForgotActivity;
import com.example.campuslife.auth.AuthInterceptor;
import com.example.campuslife.auth.RefreshAuthenticator;
import com.example.campuslife.auth.TokenStore;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static volatile Retrofit retrofitApi;
    private static volatile Retrofit retrofitNoAuth;

    private ApiClient() {
    }

    public static AuthApi authNoAuth(Context ctx) {
        if (retrofitNoAuth == null) {
            synchronized (ApiClient.class) {
                if (retrofitNoAuth == null) {
                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient noAuth = new OkHttpClient.Builder()
                            .addInterceptor(log)
                            .retryOnConnectionFailure(true)
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();

                    retrofitNoAuth = new Retrofit.Builder()
                            .baseUrl(ensureSlash(BuildConfig.BASE_URL))
                            .client(noAuth)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofitNoAuth.create(AuthApi.class);
    }

    private static Retrofit getRetrofitApi(Context ctx) {
        if (retrofitApi == null) {
            synchronized (ApiClient.class) {
                if (retrofitApi == null) {
                    final Context appCtx = ctx.getApplicationContext();

                    HttpLoggingInterceptor log = new HttpLoggingInterceptor();
                    log.setLevel(BuildConfig.DEBUG
                            ? HttpLoggingInterceptor.Level.BODY
                            : HttpLoggingInterceptor.Level.BASIC);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor(appCtx))
                            .authenticator(new RefreshAuthenticator(appCtx))
                            .addInterceptor(log)
                            .retryOnConnectionFailure(true)
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();

                    retrofitApi = new Retrofit.Builder()
                            .baseUrl(ensureSlash(BuildConfig.BASE_URL))
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofitApi;
    }

    public static <T> T create(Context ctx, Class<T> clz) {
        return getRetrofitApi(ctx).create(clz);
    }

    // Expose services dùng Bearer
    public static ActivityApi activities(Context ctx) {
        return create(ctx, ActivityApi.class);
    }

    public static DepartmentApi departments(Context ctx) {
        return create(ctx, DepartmentApi.class);
    }

    public static RegistrationApi activityRegistrations(Context ctx) {
        return create(ctx, RegistrationApi.class);
    }

    public static ReportAPI activityReports(Context ctx) {
        return create(ctx, ReportAPI.class);
    }

    public static FeedbackApi activityFeedback(Context ctx) {
        return create(ctx, FeedbackApi.class);
    }

    public static MiniGameApi miniGames(Context ctx) {
        return create(ctx, MiniGameApi.class);
    }

    public static ProfileAPI profile(Context ctx) {
        return create(ctx, ProfileAPI.class);
    }

    public static PreparationApi preparation(Context ctx) {
        return create(ctx, PreparationApi.class);
    }

    public static ActivityReminderAPI reminder(Context ctx) {
        return create(ctx, ActivityReminderAPI.class);
    }

    public static SemesterApi semester(Context ctx) {
        return create(ctx, SemesterApi.class);
    }

    public static ScoreApi score(Context ctx) {
        return create(ctx, ScoreApi.class);
    }

    public static ParticipationApi participation(Context ctx) {
        return create(ctx, ParticipationApi.class);
    }

    public static PhotoApi photo(Context ctx) {
        return create(ctx, PhotoApi.class);
    }

    public static SeriesApi series(Context ctx) {
        return create(ctx, SeriesApi.class);
    }

    public static NotificationsApi notifications(Context ctx) {
        return create(ctx, NotificationsApi.class);
    }

    public static ForgotApi forgot(Context ctx) {
        return create(ctx, ForgotApi.class);
    }

    public static AddressApi address(Context ctx) {
        return create(ctx, AddressApi.class);
    }

    public static DeviceTokenApi device(Context ctx) {
        return create(ctx, DeviceTokenApi.class);
    }

    public static synchronized void reset() {
        retrofitApi = null;
        retrofitNoAuth = null;
    }

    private static String ensureSlash(String base) {
        return base.endsWith("/") ? base : base + "/";
    }
}
