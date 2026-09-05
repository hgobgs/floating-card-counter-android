package com.example.pokertracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {

    private WindowManager wm;
    private View overlayView;
    private WindowManager.LayoutParams params;

    private TextView[] countTexts = new TextView[4];
    private int[] counts = new int[4];

    private static final String CHANNEL_ID = "poker_tracker_channel";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
        createOverlay();
    }

    private void startForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Poker Tracker", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Card Counter Running")
                .setSmallIcon(android.R.drawable.ic_menu_info_details);
        startForeground(1, builder.build());
    }

    private void createOverlay() {
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 200;

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);

        setupCounts();
        setupButtons();
        makeDraggable();

        wm.addView(overlayView, params);
    }

    private void setupCounts() {
        countTexts[0] = overlayView.findViewById(R.id.count_p0);
        countTexts[1] = overlayView.findViewById(R.id.count_p1);
        countTexts[2] = overlayView.findViewById(R.id.count_p2);
        countTexts[3] = overlayView.findViewById(R.id.count_p3);
        updateCountUI();
    }

    private void setupButtons() {
        Button btnReset = overlayView.findViewById(R.id.btn_reset);
        Button btnUndo = overlayView.findViewById(R.id.btn_undo);
        Button btnAdd = overlayView.findViewById(R.id.btn_add);

        btnReset.setOnClickListener(v -> {
            for (int i = 0; i < 4; i++) counts[i] = 0;
            UndoStack.clear();
            CardStore.clear();
            updateCountUI();
        });

        btnUndo.setOnClickListener(v -> {
            UndoStack.Move m = UndoStack.pop();
            if (m != null && m.player >= 0 && m.player < 4) {
                counts[m.player] = Math.max(0, counts[m.player] - 1);
                CardStore.removeLast(m.rank, m.suit);
                updateCountUI();
            }
        });

        btnAdd.setOnClickListener(v -> {
            // simple demo: add one "A♠" to player 0 each tap
            // real version would open a card picker dialog
            counts[0]++;
            CardStore.add("A", "♠");
            UndoStack.push("A", "♠", 0);
            updateCountUI();
        });
    }

    private void updateCountUI() {
        for (int i = 0; i < 4; i++) {
            countTexts[i].setText("P" + (i + 1) + ": " + counts[i]);
        }
    }

    private void makeDraggable() {
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private float lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x += (int) (event.getRawX() - lastX);
                        params.y += (int) (event.getRawY() - lastY);
                        lastX = event.getRawX();
                        lastY = event.getRawY();
                        wm.updateViewLayout(overlayView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null && wm != null) {
            wm.removeView(overlayView);
        }
    }
}
