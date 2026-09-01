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

        ViewGroup parent = findSuitableParent(root);
        if (parent == null) return;

        View notification = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.snackbar, parent, false);

        TextView messageView =
                notification.findViewById(R.id.snackbar_message);

        ImageView iconView =
                notification.findViewById(R.id.snackbar_icon);

        ImageButton closeButton =
                notification.findViewById(R.id.snackbar_close);

        messageView.setText(message);
        iconView.setImageResource(icon);

        int color = ContextCompat.getColor(
                parent.getContext(),
                backgroundColor
        );

        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(
                16 * parent.getResources()
                        .getDisplayMetrics()
                        .density
        );

        notification.setBackground(background);

        closeButton.setOnClickListener(v ->
                removeNotification(parent, notification)
        );

        parent.addView(notification);

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
                    () -> removeNotification(parent, notification),
                    4000
            );
        }
    }

    private static ViewGroup findSuitableParent(ViewGroup root) {
        ViewGroup view = root;
        do {
            if (view instanceof androidx.coordinatorlayout.widget.CoordinatorLayout) {
                return view;
            } else if (view instanceof android.widget.FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return view;
                } else if (!(view instanceof android.widget.ScrollView) && !(view instanceof androidx.core.widget.NestedScrollView)) {
                    return view;
                }
            }
            if (view != null) {
                final android.view.ViewParent parent = view.getParent();
                view = parent instanceof ViewGroup ? (ViewGroup) parent : null;
            }
        } while (view != null);
        return root;
    }

    private static void removeNotification(
            ViewGroup parent,
            View notification
    ) {

        if (notification == null || parent == null || notification.getParent() != parent) {
            return;
        }

        notification.animate()
                .translationY(-notification.getHeight())
                .setDuration(250)
                .withEndAction(() -> parent.removeView(notification))
                .start();
    }
}
