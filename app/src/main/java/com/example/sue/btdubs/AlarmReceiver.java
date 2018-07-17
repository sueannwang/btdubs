package com.example.sue.btdubs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.example.sue.btdubs.R;

import java.util.Calendar;
import java.util.Objects;

public class AlarmReceiver extends WakefulBroadcastReceiver {

  private static final int HOURLY = 1, DAILY = 2, WEEKLY = 3, MONTHLY = 4, YEARLY = 5;

  @Override
  public void onReceive(Context context, Intent intent) {
    int id = intent.getIntExtra(ReminderParams.ID, -1);
    String title = intent.getStringExtra(ReminderParams.TITLE);
    String msg = intent.getStringExtra(ReminderParams.CONTENT);

    if (context == null) {
      return;
    }

    ContentResolver contentResolver = context.getContentResolver();
    if (contentResolver == null) {
      return;
    }

    Uri uri = ContentUris.withAppendedId(ReminderContract.All.CONTENT_URI, id);
    Cursor cursor = contentResolver.query(uri,
            null, null, null, null);

    if (cursor == null || !cursor.moveToFirst()) {
      return;
    }

    int frequency = cursor.getInt(cursor.getColumnIndex(ReminderParams.FREQUENCY));
    Calendar time = Calendar.getInstance();
    time.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(ReminderParams.TIME)));
    cursor.close();

    if (frequency > 0) {
      switch (frequency) {
        case HOURLY:
          time.add(Calendar.HOUR, 1);

          break;
        case DAILY:
          time.add(Calendar.DATE, 1);

          break;
        case WEEKLY:
          time.add(Calendar.DATE, 7);
          break;
        case MONTHLY:
          time.add(Calendar.MONTH, 1);

          break;
        case YEARLY:
          time.add(Calendar.YEAR, 1);

          break;
      }

      ContentValues values = new ContentValues();
      values.put(ReminderContract.Alerts.TIME, time.getTimeInMillis());
      uri = ContentUris.withAppendedId(ReminderContract.Alerts.CONTENT_URI, id);
      context.getContentResolver().update(uri, values, null, null);

      Intent setAlarm = new Intent(context, AlarmService.class);
      setAlarm.putExtra(ReminderParams.ID, id);
      setAlarm.setAction(AlarmService.CREATE);
      context.startService(setAlarm);
    }

    Intent result = new Intent(context, CreateOrEditAlert.class);
    result.putExtra(ReminderParams.ID, id);
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    stackBuilder.addParentStack(CreateOrEditAlert.class);
    stackBuilder.addNextIntent(result);
    PendingIntent clicked = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
    bigStyle.setBigContentTitle(title);
    bigStyle.bigText(msg);
    Notification n = new NotificationCompat.Builder(context,"7")
            .setSmallIcon(R.drawable.ic_calendar_check_black_48dp)
            .setContentTitle(title)
            .setContentText(msg)
            .setContentIntent(clicked)
            .setPriority(Notification.PRIORITY_MAX)
            .setWhen(0)
            .setStyle(bigStyle)
            .setContentIntent(clicked)
            .setAutoCancel(true)

            .build();
    int importance = NotificationManager.IMPORTANCE_HIGH;
    //installing on oreo


    if (Build.VERSION.SDK_INT >= 26) {
      NotificationChannel channel = new NotificationChannel("7", "Vipul", importance);
      channel.setDescription("Push Message");
// Register the channel with the notifications manager same result

    }



    n.defaults |= Notification.DEFAULT_VIBRATE;
    n.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    n.defaults |= Notification.DEFAULT_SOUND;

    NotificationManager notificationManager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
    Objects.requireNonNull(notificationManager).notify(id, n);


  }

}
