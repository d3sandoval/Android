package demo.android.app.hu.servicedemo.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import demo.android.app.hu.servicedemo.MainActivity;
import demo.android.app.hu.servicedemo.R;
import demo.android.app.hu.servicedemo.SettingsActivity;
import demo.android.app.hu.servicedemo.location.LDLocationManager;

/**
 * Created by Peter on 2014.10.26..
 */
public class ServiceLocation extends Service implements LocationListener {

    private final int NOTIF_FOREGROUND_ID = 101;

    public static final String BR_NEW_LOCATION = "BR_NEW_LOCATION";
    public static final String KEY_LOCATION = "KEY_LOCATION";

    private LDLocationManager ldLocationManager = null;
    private boolean locationMonitorRunning = false;

    private Location firstLocation = null;
    private Location lastLocation = null;

    private IBinder binderServiceLocation = new BinderServiceLocation();

    private WindowManager windowManager;
    private View floatingView;
    private TextView tvFloatLat;
    private TextView tvFloatLng;

    @Override
    public IBinder onBind(Intent intent) {
        return binderServiceLocation;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_FOREGROUND_ID, getMyNotification("starting..."));

        firstLocation = null;

        if (!locationMonitorRunning) {
            locationMonitorRunning = true;
            ldLocationManager = new LDLocationManager(getApplicationContext(), this);
            ldLocationManager.startLocationMonitoring();
        }

        if (intent.getBooleanExtra(SettingsActivity.KEY_WITH_FLOATING,false)) {
            showFloatingWindow();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ldLocationManager != null) {
            ldLocationManager.stopLocationMonitoring();
        }

        hideFloatingWindow();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (firstLocation == null) {
            firstLocation = location;
        }
        lastLocation = location;

        //--- update notification
        updateNotification("Lat: "+location.getLatitude()+"\n"+
                "Lng: "+location.getLongitude());
        //--- end update notification code

        //--- floating update
        if (floatingView != null) {
            tvFloatLat.setText("Lat: "+location.getLatitude());
            tvFloatLng.setText("Lng: "+location.getLongitude());
        }
        //--- end floating update

        Intent intent = new Intent(BR_NEW_LOCATION);
        intent.putExtra(KEY_LOCATION, location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        updateNotification("Status changed: " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        updateNotification("Provider enabled: "+provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        updateNotification("Provider disabled: "+provider);
    }

    private Notification getMyNotification(String text) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                NOTIF_FOREGROUND_ID,
                notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Service Location Demo")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(new long[]{1000,2000,1000})
                .setContentIntent(contentIntent).build();
        return  notification;
    }

    private void updateNotification(String text) {
        Notification notification = getMyNotification(text);
        NotificationManager notifMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notifMan.notify(NOTIF_FOREGROUND_ID,notification);
    }

    private void showFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatingView = ((LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.float_layout, null);
        tvFloatLat = (TextView) floatingView.findViewById(R.id.tvFloatLat);
        tvFloatLng = (TextView) floatingView.findViewById(R.id.tvFloatLng);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        windowManager.addView(floatingView, params);

        try {
            floatingView.setOnTouchListener(new View.OnTouchListener() {
                private WindowManager.LayoutParams paramsF = params;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = paramsF.x;
                            initialY = paramsF.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            break;
                        case MotionEvent.ACTION_MOVE:
                            paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                            paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                            if (floatingView != null) {
                                windowManager.updateViewLayout(floatingView, paramsF);
                            }
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideFloatingWindow() {
        if (floatingView != null) {
            windowManager.removeView(floatingView);
            floatingView = null;
        }
    }

    public class BinderServiceLocation extends Binder {
        public ServiceLocation getSerivce() {
            return ServiceLocation.this;
        }
    }

    public boolean isLocationMonitorRunning() {
        return locationMonitorRunning;
    }

    public Location getLastLocation() {
        return lastLocation;
    }
}
