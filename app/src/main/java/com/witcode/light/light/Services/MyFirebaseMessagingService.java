package com.witcode.light.light.Services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.witcode.light.light.R;
import com.witcode.light.light.activities.MainActivity;

/**
 * Created by rosety on 2/6/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.v("mytag", "recieved" + remoteMessage.getData().toString());

        if(remoteMessage.getData().get("is_notif").equals("true")){
            sendNotification(remoteMessage.getData().get("notif_title"),
                    remoteMessage.getData().get("notif_content"),
                    remoteMessage.getData().get("notif_action"));
        }

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }

    private void sendNotification(String title, String content, String action) {

        Intent iContentPress = new Intent(this, MainActivity.class);

        iContentPress.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if(action!=null){
            switch (action){
                case "start_activity":
                    iContentPress.setAction("start_activity");
                    break;
                case "ranking":
                    iContentPress.setAction("ranking");
                    break;
                case "market":
                    iContentPress.setAction("market");
                    break;
                default:
                    iContentPress.setAction("normal");
                    break;
            }
        }else{
            iContentPress.setAction("normal");
        }


        PendingIntent piContent = PendingIntent.getActivity(this, 0,
                iContentPress, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_icono_app);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSmallIcon(R.drawable.ic_bulb)
                        .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(piContent)
                        .setOngoing(false);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(1, mBuilder.build());


    }
}
