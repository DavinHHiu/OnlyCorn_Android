package com.example.onlycorn.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {
    public static BitmapFactory.Options getImageSize(Context context, Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        ContentResolver contentResolver = context.getContentResolver();
        try (InputStream inputStream = contentResolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(inputStream, null, options);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return options;
    }

    public static int getVideoSize(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());

        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        try {
            retriever.release();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (height != null) {
            return Integer.parseInt(height);
        }
        return -1;
    }

    public static String getType(Context context, Uri uri) {
        return context.getContentResolver().getType(uri);
    }
}