package com.witcode.light.light;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.witcode.light.light.activities.MainActivity;
import com.witcode.light.light.backend.UpdateLights;
import com.witcode.light.light.domain.EastNorth;
import com.witcode.light.light.fragments.ActivityFragment;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by rosety on 1/6/17.
 */

public class Utils {

    public static EastNorth Deg2UTM(double Lat, double Lon) {
        int Zone;
        char Letter;
        double Easting;
        double Northing;

        Zone = (int) Math.floor(Lon / 6 + 31);
        if (Lat < -72)
            Letter = 'C';
        else if (Lat < -64)
            Letter = 'D';
        else if (Lat < -56)
            Letter = 'E';
        else if (Lat < -48)
            Letter = 'F';
        else if (Lat < -40)
            Letter = 'G';
        else if (Lat < -32)
            Letter = 'H';
        else if (Lat < -24)
            Letter = 'J';
        else if (Lat < -16)
            Letter = 'K';
        else if (Lat < -8)
            Letter = 'L';
        else if (Lat < 0)
            Letter = 'M';
        else if (Lat < 8)
            Letter = 'N';
        else if (Lat < 16)
            Letter = 'P';
        else if (Lat < 24)
            Letter = 'Q';
        else if (Lat < 32)
            Letter = 'R';
        else if (Lat < 40)
            Letter = 'S';
        else if (Lat < 48)
            Letter = 'T';
        else if (Lat < 56)
            Letter = 'U';
        else if (Lat < 64)
            Letter = 'V';
        else if (Lat < 72)
            Letter = 'W';
        else
            Letter = 'X';
        Easting = 0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) * 0.9996 * 6399593.62 / Math.pow((1 + Math.pow(0.0820944379, 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)), 0.5) * (1 + Math.pow(0.0820944379, 2) / 2 * Math.pow((0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin(Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) / 3) + 500000;
        Easting = Math.round(Easting * 100) * 0.01;
        Northing = (Math.atan(Math.tan(Lat * Math.PI / 180) / Math.cos((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) - Lat * Math.PI / 180) * 0.9996 * 6399593.625 / Math.sqrt(1 + 0.006739496742 * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) * (1 + 0.006739496742 / 2 * Math.pow(0.5 * Math.log((1 + Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180))) / (1 - Math.cos(Lat * Math.PI / 180) * Math.sin((Lon * Math.PI / 180 - (6 * Zone - 183) * Math.PI / 180)))), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) + 0.9996 * 6399593.625 * (Lat * Math.PI / 180 - 0.005054622556 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + 4.258201531e-05 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 - 1.674057895e-07 * (5 * (3 * (Lat * Math.PI / 180 + Math.sin(2 * Lat * Math.PI / 180) / 2) + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 4 + Math.sin(2 * Lat * Math.PI / 180) * Math.pow(Math.cos(Lat * Math.PI / 180), 2) * Math.pow(Math.cos(Lat * Math.PI / 180), 2)) / 3);
        if (Letter < 'M')
            Northing = Northing + 10000000;
        Northing = Math.round(Northing * 100) * 0.01;

        return new EastNorth(Easting,Northing);
    }

    public static String getUpdateLightsActionTypeFromActivityActionType(int type){
        String actionType;
        switch (type) {
            case ActivityFragment.ACTIVITY_WALK:
                actionType = UpdateLights.WALK;
                break;
            case ActivityFragment.ACTIVITY_BIKE:
                actionType = UpdateLights.BIKE;
                break;

            case ActivityFragment.ACTIVITY_BUS:
                actionType = UpdateLights.BUS;
                break;

            case ActivityFragment.ACTIVITY_RAILROAD:
                actionType = UpdateLights.RAILROAD;
                break;

            case ActivityFragment.ACTIVITY_RECYCLE:
                actionType = UpdateLights.RECYCLE;
                break;

            case ActivityFragment.ACTIVITY_CARSHARE:
                actionType = UpdateLights.CAR_SHARE;
                break;

            default:
                actionType = UpdateLights.OTHER;
                break;

        }
        return actionType;
    }

    public static void sendNotification(Context c,String title, String content, String action) {

        Intent iContentPress = new Intent(c, MainActivity.class);

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


        PendingIntent piContent = PendingIntent.getActivity(c, 0,
                iContentPress, 0);

        Bitmap icon = BitmapFactory.decodeResource(c.getResources(),
                R.mipmap.ic_icono_app);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(c)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSmallIcon(R.drawable.ic_bulb)
                        .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                        .setContentIntent(piContent)
                        .setOngoing(false);
        NotificationManager mNotifyMgr =
                (NotificationManager) c.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(1, mBuilder.build());


    }
}
