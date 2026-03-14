package com.example.campuslife.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;

public class ImageCompressUtil {

    public static File compressToJpeg(Context context, String inputPath) throws Exception {
        if (context == null)
            throw new IllegalArgumentException("context null");
        if (inputPath == null)
            throw new IllegalArgumentException("inputPath null");

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(inputPath, o);

        int max = 1280;
        o.inSampleSize = calcInSampleSize(o.outWidth, o.outHeight, max, max);
        o.inJustDecodeBounds = false;
        o.inPreferredConfig = Bitmap.Config.RGB_565;

        Bitmap bmp = BitmapFactory.decodeFile(inputPath, o);
        if (bmp == null)
            throw new IllegalStateException("Cannot decode image");

        File out = new File(context.getCacheDir(), "evidence_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(out)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
        } finally {
            bmp.recycle();
        }
        return out;
    }

    private static int calcInSampleSize(int w, int h, int reqW, int reqH) {
        int inSampleSize = 1;
        if (h > reqH || w > reqW) {
            final int halfH = h / 2;
            final int halfW = w / 2;
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }
}
