package com.mizou.fieldplanner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class CarBtReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent i) {
        try {
            String a = i.getAction();
            BluetoothDevice d = (BluetoothDevice) i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (d == null || a == null) return;
            SharedPreferences p = c.getSharedPreferences("wt_car", Context.MODE_PRIVATE);
            String want = p.getString("addr", "");
            if (want.length() == 0 || !want.equalsIgnoreCase(d.getAddress())) return;
            boolean conn = BluetoothDevice.ACTION_ACL_CONNECTED.equals(a);
            long now = System.currentTimeMillis();
            String[] parts = p.getString("events", "").split(";");
            StringBuilder sb = new StringBuilder();
            int from = Math.max(0, parts.length - 39);
            for (int k = from; k < parts.length; k++) {
                if (parts[k].length() > 0) sb.append(parts[k]).append(';');
            }
            sb.append(now).append('|').append(conn ? 1 : 0).append(';');
            p.edit().putString("events", sb.toString()).apply();
            if (conn) notifyStart(c);
        } catch (Exception e) {
            // ignore: never crash a system broadcast
        }
    }

    private void notifyStart(Context c) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(new NotificationChannel("car", "Car and start of day", NotificationManager.IMPORTANCE_HIGH));
        }
        Intent li = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        PendingIntent pi = PendingIntent.getActivity(c, 0, li, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        int ic = c.getResources().getIdentifier("ic_stat_icon", "drawable", c.getPackageName());
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, "car") : new Notification.Builder(c);
        b.setContentTitle("Good morning")
                .setContentText("Your car connected. Tap to start your day.")
                .setSmallIcon(ic == 0 ? android.R.drawable.ic_dialog_info : ic)
                .setContentIntent(pi)
                .setAutoCancel(true);
        nm.notify(4242, b.build());
    }
}
