package com.boredomdenied.capstone;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import butterknife.BindString;


public class GpsUpdateService extends Service {

    @BindString(R.string.client_unbound)
    String clientUnbound;
    @BindString(R.string.request_location_updates)
    String requestLocationUpdates;
    @BindString(R.string.remove_location_updates)
    String removeLocationUpdates;
    @BindString(R.string.starting_foreground_service)
    String foregroundService;
    @BindString(R.string.location_updated)
    String locationUpdated;
    @BindString(R.string.service_started)
    String serviceStarted;
    @BindString(R.string.no_request_updates)
    String noRequestUpdates;
    @BindString(R.string.lost_location_permission)
    String lostLocationPermission;
    @BindString(R.string.location_failed)
    String locationFailed;
    @BindString(R.string.no_remove_updates)
    String noRemoveUpdates;


    private static final String PACKAGE_NAME = "com.boredomdenied.capstone";
    private static final String TAG = GpsUpdateService.class.getSimpleName();
    private static final String CHANNEL_ID = "channel_01";
    private static final String STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";

    private static final long UPDATE_SPEED = 6000;
    private static final long QUICK_UPDATE_SPEED = UPDATE_SPEED / 2;
    private final IBinder Binder = new LocalBinder();

    static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    static final String EXTRA_LOCATION = PACKAGE_NAME + ".defaultLocation";

    private static final int NOTIFICATION_ID = 12345678;

    private boolean ChangingConfiguration = false;

    private NotificationManager NotificationManager;
    private LocationRequest LocationRequest;
    private FusedLocationProviderClient FusedLocationClient;
    private LocationCallback LocationCallback;
    private Handler ServiceHandler;
    private Location Location;

    public GpsUpdateService() {
    }

    @Override
    public void onCreate() {
        FusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        ServiceHandler = new Handler(handlerThread.getLooper());
        NotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager.createNotificationChannel(mChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, String.valueOf(serviceStarted));
        boolean startedFromNotification = intent.getBooleanExtra(STARTED_FROM_NOTIFICATION,
                false);

        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {

        stopForeground(true);
        ChangingConfiguration = false;
        return Binder;
    }

    @Override
    public void onRebind(Intent intent) {

        stopForeground(true);
        ChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, String.valueOf(clientUnbound));

        if (!ChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, String.valueOf(foregroundService));
            startForeground(NOTIFICATION_ID, getNotification());
        }
        return true;
    }

    @Override
    public void onDestroy() {
        ServiceHandler.removeCallbacksAndMessages(null);
    }


    public void requestLocationUpdates() {
        Log.i(TAG, String.valueOf(requestLocationUpdates));
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), GpsUpdateService.class));
        try {
            FusedLocationClient.requestLocationUpdates(LocationRequest,
                    LocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, String.valueOf(noRequestUpdates) + unlikely);
        }

    }


    public void removeLocationUpdates() {
        Log.i(TAG, String.valueOf(removeLocationUpdates));
        try {
            FusedLocationClient.removeLocationUpdates(LocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, String.valueOf(noRemoveUpdates) + unlikely);
        }
    }


    private Notification getNotification() {
        Intent intent = new Intent(this, GpsUpdateService.class);

        CharSequence text = Utils.getLocationText(Location);

        intent.putExtra(STARTED_FROM_NOTIFICATION, true);

        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }

    private void getLastLocation() {
        try {
            FusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location = task.getResult();
                            } else {
                                Log.w(TAG, String.valueOf(locationFailed));
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, String.valueOf(lostLocationPermission) + unlikely);
        }
    }

    private void onNewLocation(Location location) {
        Log.i(TAG, String.valueOf(locationUpdated) + location);

        Location = location;

        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        if (serviceIsRunningInForeground(this)) {
            NotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }


    private void createLocationRequest() {
        LocationRequest = new LocationRequest();
        LocationRequest.setInterval(UPDATE_SPEED);
        LocationRequest.setFastestInterval(QUICK_UPDATE_SPEED);
        LocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    public class LocalBinder extends Binder {
        GpsUpdateService getService() {
            return GpsUpdateService.this;
        }
    }


    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
