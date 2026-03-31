package com.example.campuslife.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

public class StatusBadgeHelper {

    public static void applyExpenseStatus(TextView tv, String status) {
        if (tv == null || status == null) return;
        switch (status) {
            case "PENDING_LEADER":
                tv.setText("CHỜ LEADER");
                applyBadge(tv, "#FEF9C3", "#CA8A04");
                break;
            case "PENDING_ADMIN":
                tv.setText("CHỜ ADMIN");
                applyBadge(tv, "#FEF3C7", "#D97706");
                break;
            case "APPROVED":
                tv.setText("ĐÃ DUYỆT");
                applyBadge(tv, "#D1FAE5", "#059669");
                break;
            case "REJECTED":
                tv.setText("TỪ CHỐI");
                applyBadge(tv, "#FEE2E2", "#DC2626");
                break;
            default:
                tv.setText(status);
                applyBadge(tv, "#F3F4F6", "#6B7280");
                break;
        }
    }

    public static void applyFundAdvanceStatus(TextView tv, String status) {
        if (tv == null || status == null) return;
        switch (status) {
            case "REQUESTED":
                tv.setText("YÊU CẦU");
                applyBadge(tv, "#EFF6FF", "#3B82F6");
                break;
            case "HOLDING":
                tv.setText("ĐANG GIỮ");
                applyBadge(tv, "#FFF7ED", "#EA580C");
                break;
            case "SETTLED":
                tv.setText("ĐÃ HOÀN");
                applyBadge(tv, "#D1FAE5", "#059669");
                break;
            case "REJECTED":
                tv.setText("TỪ CHỐI");
                applyBadge(tv, "#FEE2E2", "#DC2626");
                break;
            default:
                tv.setText(status);
                applyBadge(tv, "#F3F4F6", "#6B7280");
                break;
        }
    }

    public static void applyTaskStatus(TextView tv, String status) {
        if (tv == null || status == null) return;
        switch (status) {
            case "PENDING":
                tv.setText("CHỜ NHẬN");
                applyBadge(tv, "#F3F4F6", "#6B7280");
                break;
            case "ACCEPTED":
                tv.setText("ĐANG LÀM");
                applyBadge(tv, "#EFF6FF", "#3B82F6");
                break;
            case "COMPLETION_REQUESTED":
                tv.setText("XIN HOÀN THÀNH");
                applyBadge(tv, "#FFF7ED", "#EA580C");
                break;
            case "COMPLETED":
                tv.setText("HOÀN THÀNH");
                applyBadge(tv, "#D1FAE5", "#059669");
                break;
            default:
                tv.setText(status);
                applyBadge(tv, "#F3F4F6", "#6B7280");
                break;
        }
    }

    public static void applyAllocationAdjStatus(TextView tv, String status) {
        if (tv == null || status == null) return;
        switch (status) {
            case "PENDING":
                tv.setText("CHỜ DUYỆT");
                applyBadge(tv, "#FEF9C3", "#CA8A04");
                break;
            case "APPROVED":
                tv.setText("ĐÃ DUYỆT");
                applyBadge(tv, "#D1FAE5", "#059669");
                break;
            case "REJECTED":
                tv.setText("TỪ CHỐI");
                applyBadge(tv, "#FEE2E2", "#DC2626");
                break;
            default:
                tv.setText(status);
                applyBadge(tv, "#F3F4F6", "#6B7280");
                break;
        }
    }

    private static void applyBadge(TextView tv, String bgColor, String textColor) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(bgColor));
        bg.setCornerRadius(100f);
        tv.setBackground(bg);
        tv.setTextColor(Color.parseColor(textColor));
        float density = tv.getContext().getResources().getDisplayMetrics().density;
        int hPad = (int)(10 * density);
        int vPad = (int)(3 * density);
        tv.setPadding(hPad, vPad, hPad, vPad);
    }
}
