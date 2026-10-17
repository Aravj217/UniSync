package com.example.unisync.utils;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unisync.R;
import com.example.unisync.models.Event;
import com.example.unisync.models.TeamRequest;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * UIHelper — utility methods for UI operations, validation, and formatting.
 */
public final class UIHelper {

    private UIHelper() {} // Prevent instantiation

    // ════════════════════════════════════════
    //  VALIDATION
    // ════════════════════════════════════════

    /**
     * Validates that a TextInputEditText is not empty.
     * Shows error on the parent TextInputLayout if empty.
     * @return true if the field has content
     */
    public static boolean validateRequired(TextInputEditText editText, String fieldName) {
        TextInputLayout layout = (TextInputLayout) editText.getParent().getParent();
        String text = getText(editText);
        if (TextUtils.isEmpty(text)) {
            layout.setError(fieldName + " is required");
            editText.requestFocus();
            return false;
        }
        layout.setError(null);
        return true;
    }

    /**
     * Get trimmed text from EditText.
     */
    public static String getText(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }

    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email)
                && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate password length.
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password)
                && password.length() >= Constants.MIN_PASSWORD_LENGTH;
    }

    // ════════════════════════════════════════
    //  STATUS BADGES
    // ════════════════════════════════════════

    /**
     * Apply status badge styling to a TextView based on event status.
     */
    public static void applyEventStatusBadge(TextView badge, String status) {
        if (status == null) return;

        Context context = badge.getContext();
        int textColor;
        int bgColor;

        switch (status) {
            case Event.STATUS_DRAFT:
                textColor = context.getColor(R.color.status_draft);
                bgColor = context.getColor(R.color.status_draft_bg);
                break;
            case Event.STATUS_PENDING:
                textColor = context.getColor(R.color.status_pending);
                bgColor = context.getColor(R.color.status_pending_bg);
                break;
            case Event.STATUS_APPROVED:
                textColor = context.getColor(R.color.status_approved);
                bgColor = context.getColor(R.color.status_approved_bg);
                break;
            case Event.STATUS_REJECTED:
                textColor = context.getColor(R.color.status_rejected);
                bgColor = context.getColor(R.color.status_rejected_bg);
                break;
            case Event.STATUS_PUBLISHED:
                textColor = context.getColor(R.color.status_published);
                bgColor = context.getColor(R.color.status_published_bg);
                break;
            case Event.STATUS_CANCELLED:
                textColor = context.getColor(R.color.status_cancelled);
                bgColor = context.getColor(R.color.status_cancelled_bg);
                break;
            default:
                textColor = context.getColor(R.color.text_secondary);
                bgColor = context.getColor(R.color.surface_variant);
                break;
        }

        badge.setText(status);
        badge.setTextColor(textColor);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dpToPx(context, 8));
        badge.setBackground(bg);
        badge.setPadding(dpToPx(context, 12), dpToPx(context, 4),
                dpToPx(context, 12), dpToPx(context, 4));
    }

    /**
     * Apply status badge styling for team request status.
     */
    public static void applyRequestStatusBadge(TextView badge, String status) {
        if (status == null) return;

        Context context = badge.getContext();
        int textColor;
        int bgColor;

        switch (status) {
            case TeamRequest.STATUS_DRAFT:
                textColor = context.getColor(R.color.status_draft);
                bgColor = context.getColor(R.color.status_draft_bg);
                break;
            case TeamRequest.STATUS_PENDING:
                textColor = context.getColor(R.color.status_pending);
                bgColor = context.getColor(R.color.status_pending_bg);
                break;
            case TeamRequest.STATUS_NEEDS_CORRECTION:
                textColor = context.getColor(R.color.status_needs_correction);
                bgColor = context.getColor(R.color.status_needs_correction_bg);
                break;
            case TeamRequest.STATUS_APPROVED:
                textColor = context.getColor(R.color.status_approved);
                bgColor = context.getColor(R.color.status_approved_bg);
                break;
            case TeamRequest.STATUS_REJECTED:
                textColor = context.getColor(R.color.status_rejected);
                bgColor = context.getColor(R.color.status_rejected_bg);
                break;
            case TeamRequest.STATUS_PUBLISHED:
                textColor = context.getColor(R.color.status_published);
                bgColor = context.getColor(R.color.status_published_bg);
                break;
            case TeamRequest.STATUS_CANCELLED:
                textColor = context.getColor(R.color.status_cancelled);
                bgColor = context.getColor(R.color.status_cancelled_bg);
                break;
            case TeamRequest.STATUS_COMPLETED:
                textColor = context.getColor(R.color.status_completed);
                bgColor = context.getColor(R.color.status_completed_bg);
                break;
            default:
                textColor = context.getColor(R.color.text_secondary);
                bgColor = context.getColor(R.color.surface_variant);
                break;
        }

        badge.setText(status.replace("_", " "));
        badge.setTextColor(textColor);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dpToPx(context, 8));
        badge.setBackground(bg);
        badge.setPadding(dpToPx(context, 12), dpToPx(context, 4),
                dpToPx(context, 12), dpToPx(context, 4));
    }

    /**
     * Apply status badge styling for volunteer assignments.
     */
    public static void applyAssignmentStatusBadge(TextView badge, String status) {
        if (status == null) return;

        Context context = badge.getContext();
        int textColor;
        int bgColor;

        switch (status) {
            case "APPROVED":
                textColor = context.getColor(R.color.status_approved);
                bgColor = context.getColor(R.color.status_approved_bg);
                break;
            case "REJECTED":
                textColor = context.getColor(R.color.status_rejected);
                bgColor = context.getColor(R.color.status_rejected_bg);
                break;
            case "NEEDS_CORRECTION":
                textColor = context.getColor(R.color.status_needs_correction);
                bgColor = context.getColor(R.color.status_needs_correction_bg);
                break;
            case "CANCELLED":
                textColor = context.getColor(R.color.status_cancelled);
                bgColor = context.getColor(R.color.status_cancelled_bg);
                break;
            case "PENDING":
            default:
                textColor = context.getColor(R.color.status_pending);
                bgColor = context.getColor(R.color.status_pending_bg);
                break;
        }

        badge.setText(status.replace("_", " "));
        badge.setTextColor(textColor);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dpToPx(context, 8));
        badge.setBackground(bg);
        badge.setPadding(dpToPx(context, 12), dpToPx(context, 4),
                dpToPx(context, 12), dpToPx(context, 4));
    }

    /**
     * Show Date Picker dialog for a target TextView / EditText.
     */
    public static void showDatePicker(Context context, TextView target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    target.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    /**
     * Show Time Picker dialog for a target TextView / EditText.
     */
    public static void showTimePicker(Context context, TextView target) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(
                context,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
                    target.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        ).show();
    }

    // ════════════════════════════════════════
    //  DATE / TIME FORMATTING
    // ════════════════════════════════════════

    /**
     * Format date string from storage format to display format.
     * Input:  "2025-02-16"
     * Output: "16 Feb 2025"
     */
    public static String formatDateForDisplay(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) return "";
        try {
            SimpleDateFormat input = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat(Constants.DATE_DISPLAY_FORMAT, Locale.getDefault());
            Date date = input.parse(dateStr);
            return date != null ? output.format(date) : dateStr;
        } catch (ParseException e) {
            return dateStr;
        }
    }

    /**
     * Format time string from storage format to display format.
     * Input:  "14:30"
     * Output: "02:30 PM"
     */
    public static String formatTimeForDisplay(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) return "";
        try {
            SimpleDateFormat input = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat(Constants.TIME_DISPLAY_FORMAT, Locale.getDefault());
            Date time = input.parse(timeStr);
            return time != null ? output.format(time) : timeStr;
        } catch (ParseException e) {
            return timeStr;
        }
    }

    // ════════════════════════════════════════
    //  DIALOGS & FEEDBACK
    // ════════════════════════════════════════

    /**
     * Show a simple toast message.
     */
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show a long toast message.
     */
    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Show a Snackbar message.
     */
    public static void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Show a Snackbar with an action button.
     */
    public static void showSnackbarWithAction(View view, String message,
                                               String actionText, View.OnClickListener action) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setAction(actionText, action)
                .show();
    }

    /**
     * Show a confirmation dialog.
     */
    public static void showConfirmDialog(Context context, String title, String message,
                                          Runnable onConfirm) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (d, w) -> onConfirm.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show an input dialog (e.g., for rejection reason).
     */
    public static void showInputDialog(Context context, String title, String hint,
                                        InputCallback callback) {
        TextInputEditText input = new TextInputEditText(context);
        input.setHint(hint);
        input.setPadding(dpToPx(context, 24), dpToPx(context, 16),
                dpToPx(context, 24), dpToPx(context, 8));

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Submit", (d, w) -> {
                    String text = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!TextUtils.isEmpty(text)) {
                        callback.onInput(text);
                    } else {
                        showToast(context, "Please enter a reason");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public interface InputCallback {
        void onInput(String text);
    }

    // ════════════════════════════════════════
    //  NETWORK
    // ════════════════════════════════════════

    /**
     * Check if the device has network connectivity.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    // ════════════════════════════════════════
    //  UTILITY
    // ════════════════════════════════════════

    /**
     * Convert dp to pixels.
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Show or hide a view.
     */
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
