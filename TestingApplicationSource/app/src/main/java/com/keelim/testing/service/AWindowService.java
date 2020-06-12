package com.keelim.testing.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.core.app.NotificationCompat;

import com.keelim.testing.R;

public class AWindowService extends Service {
    private WindowManager wm;
    private View mView;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Android O 이상일 경우 Foreground 서비스를 실행
        // Notification channel 설정.
        final String strId = "0098765";
        final String strTitle = getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = notificationManager.getNotificationChannel(strId);
        if (channel == null) {
            channel = new NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, strId).build();
        startForeground(1, notification);

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflater 를 사용하여 layout 을 가져오자
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        // 윈도우매니저 설정

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                // Android O 이상인 경우 TYPE_APPLICATION_OVERLAY 로 설정
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);


        params.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        // 위치 지정

        mView = inflate.inflate(R.layout.view_in_service, null);
        // view_in_service.xml layout 불러오기
        // mView.setOnTouchListener(onTouchListener);
        // Android O 이상의 버전에서는 터치리스너가 동작하지 않는다. ( TYPE_APPLICATION_OVERLAY 터치 미지원)


        final ImageButton btn_img = mView.findViewById(R.id.btn_img);
        btn_img.setOnClickListener(v -> {
            Log.d("test", "onClick ");
            // do something!
        });

        // btn_img 에 android:filterTouchesWhenObscured="true" 속성 추가하면 터치리스너가 동작한다.
        btn_img.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.d("test", "touch DOWN ");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.d("test", "touch UP");
                    break;
                case MotionEvent.ACTION_MOVE:
                    Log.d("test", "touch move ");
                    break;
            }
            return false;
        });
        wm.addView(mView, params); // 윈도우에 layout 을 추가 한다.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true); // Foreground service 종료

        if (wm != null) {
            if (mView != null) {
                wm.removeView(mView); // View 초기화
                mView = null;
            }
            wm = null;
        }
    }

}