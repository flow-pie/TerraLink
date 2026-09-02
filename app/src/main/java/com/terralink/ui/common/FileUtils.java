package com.terralink.ui.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import okhttp3.ResponseBody;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static void saveAndOpenPdf(Context context, ResponseBody body, String fileName) {
        if (context == null || body == null) return;
        
        try {
            // Use external cache dir so FileProvider can share it
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null) cacheDir = context.getCacheDir();
            
            File file = new File(cacheDir, fileName + ".pdf");
            
            Log.d(TAG, "Saving PDF to: " + file.getAbsolutePath());

            try (InputStream is = body.byteStream();
                 FileOutputStream fos = new FileOutputStream(file)) {
                
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
            }

            if (file.exists() && file.length() > 0) {
                Log.d(TAG, "PDF saved successfully, size: " + file.length());
                
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                
                context.startActivity(Intent.createChooser(intent, "Open " + fileName));
            } else {
                Log.e(TAG, "File was not created or is empty");
                SnackbarUtils.showError(null, "Failed to create PDF file");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving/opening PDF", e);
            SnackbarUtils.showError(null, "Error opening PDF: " + e.getMessage());
        }
    }
}
