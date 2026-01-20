package com.example.campuslife.fcm;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.campuslife.R;
import com.example.campuslife.activity.EventDetailActivity;
import com.example.campuslife.activity.LoginActivity;
import com.example.campuslife.activity.NotificationActivity;
import com.example.campuslife.api.ApiClient;
import com.example.campuslife.api.ApiResponse;

import com.example.campuslife.api.DeviceTokenApi;
import com.example.campuslife.auth.TokenStore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d("FCM", "New token = " + token);

        if (!TokenStore.isLoggedIn(this)) {
            Log.d("FCM", "User chưa login, chưa gửi token");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("token", token);

        ApiClient.device(getApplicationContext())
                .saveToken(body)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        Log.d("FCM", "Token sent to backend: " + response.code());
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Log.e("FCM", "Send token failed", t);
                    }
                });
    }


    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        NotificationHelper.ensureChannel(this);


        String title = "CampusLife";
        String body = "Bạn có thông báo mới";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null)
                title = message.getNotification().getTitle();

            if (message.getNotification().getBody() != null)
                body = message.getNotification().getBody();
        }


        String type = message.getData().get("type");
        String activityIdStr = message.getData().get("activityId");

        Intent intent;


        if (!TokenStore.isLoggedIn(this)) {
            intent = new Intent(this, LoginActivity.class);
        }

        else if ("ACTIVITY_REGISTRATION".equals(type) && activityIdStr != null) {
            try {
                long activityId = Long.parseLong(activityIdStr);
                intent = new Intent(this, EventDetailActivity.class);
                intent.putExtra("activity_id", activityId);
            } catch (NumberFormatException e) {
                intent = new Intent(this, NotificationActivity.class);
            }
        }

        else {
            intent = new Intent(this, NotificationActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        (int) System.currentTimeMillis(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notify)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);


        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }
}
