package com.example.campuslife.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class JwtUtils {

    public static String getRoleFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return "STUDENT";
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return "STUDENT";

            String payloadBase64 = parts[1];
            String payloadString = new String(Base64.decode(payloadBase64, Base64.URL_SAFE), StandardCharsets.UTF_8);
            
            JSONObject jsonObject = new JSONObject(payloadString);
            if (jsonObject.has("role")) {
                return jsonObject.getString("role");
            }

        } catch (Exception e) {
            Log.e("JwtUtils", "Error parsing token payload", e);
        }
        return "STUDENT";
    }
}
