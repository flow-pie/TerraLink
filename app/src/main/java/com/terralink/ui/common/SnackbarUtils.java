package com.terralink.ui.common;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.terralink.R;

public class SnackbarUtils {

    public static void showSuccess(ViewGroup root, String message) {
        showNotification(
                root,
                message,
                R.color.status_green,
                R.drawable.ic_check_circle,
                true
        );
    }

    public static void showError(ViewGroup root, String message) {
        showNotification(
                root,
                message,
                R.color.status_red,
                R.drawable.ic_error,
                false
        );
    }

    public static void showInfo(ViewGroup root, String message) {
        showNotification(
                root,
                message,
                R.color.terracotta_light,
                R.drawable.ic_info,
                true
        );
    }

    public static void showWarning(ViewGroup root, String message) {
        showNotification(
                root,
                message,
                R.color.status_amber,
                R.drawable.ic_warning,
                true
        );
    }

    private static void showNotification(
            ViewGroup root,
            String message,
            int backgroundColor,
            int icon,
            boolean autoDismiss
    ) {

        if (root == null || message == null || message.trim().isEmpty()) {
            return;
        }

        View notification = LayoutInflater.from(root.getContext())
                .inflate(R.layout.snackbar, root, false);

        TextView messageView =
                notification.findViewById(R.id.snackbar_message);

        ImageView iconView =
                notification.findViewById(R.id.snackbar_icon);

        ImageButton closeButton =
                notification.findViewById(R.id.snackbar_close);

        messageView.setText(message);
        iconView.setImageResource(icon);

        int color = ContextCompat.getColor(
                root.getContext(),
                backgroundColor
        );

        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(
                16 * root.getResources()
                        .getDisplayMetrics()
                        .density
        );

        notification.setBackground(background);

        closeButton.setOnClickListener(v ->
                removeNotification(root, notification)
        );

        root.addView(notification);

        notification.setTranslationY(-notification.getHeight());

        notification.post(() -> {
            notification.setTranslationY(-notification.getHeight());

            notification.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        });

        if(autoDismiss){
            notification.postDelayed(
                    () -> removeNotification(root, notification),
                    4000
            );
        }
    }

    private static void removeNotification(
            ViewGroup root,
            View notification
    ) {

        if (notification.getParent() != root) {
            return;
        }

        notification.animate()
                .translationY(-notification.getHeight())
                .setDuration(250)
                .withEndAction(() -> root.removeView(notification))
                .start();
    }
}