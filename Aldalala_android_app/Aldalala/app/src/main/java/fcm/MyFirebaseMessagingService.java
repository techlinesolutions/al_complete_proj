package fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import Config.BaseURL;
import techline.carsapp.ChatActivity;
import techline.carsapp.MainActivity;
import techline.carsapp.R;

/**
 * Created by Rajesh on 2017-08-07.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String broadCastReceiver = "Chat_Data";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {

            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            JSONObject object = new JSONObject(remoteMessage.getData());
            try {

                String getimage = "";

                if (object.has("image")) {
                    getimage = object.getString("image");
                }

                if (object.has("obj")) {
                    JSONObject chatObj = new JSONObject(object.getString("obj"));

                    if (ChatActivity.isActivityopen && chatObj.getString("join_id").equals(ChatActivity.getJoinId)) {

                        Intent data = new Intent("Chat_Data");
                        data.putExtra("jsonobject", object.getString("obj"));
                        sendBroadcast(data);
                    } else {
                        if (object.has("obj") && !object.getString("obj").isEmpty()) {
                            sendNotification(object.getString("message"), object.getString("title"), getimage, object.getString("created_at"), true, chatObj.getString("join_id"), "");
                        } else {
                            sendNotification(object.getString("message"), object.getString("title"), getimage, object.getString("created_at"), false, "", "");
                        }
                    }
                } else {
                    sendNotification(object.getString("message"), object.getString("title"), getimage, object.getString("created_at"), false, "", "");
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }

        Log.d(TAG, "is open" + ChatActivity.isActivityopen + ",joinid" + ChatActivity.getJoinId);

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            //sendNotification
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    private void sendNotification(String message, String title, String imageUrl, String created_at, boolean isChat, String join_id, String username) {

        Intent intent;

        if (isChat) {
            intent = new Intent(this, ChatActivity.class);
            intent.putExtra("chat_id", join_id);
            intent.putExtra("title", username);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 126, intent,
                PendingIntent.FLAG_ONE_SHOT);

        if (imageUrl != null && imageUrl.length() > 4) {
            Bitmap bitmap = getBitmapFromURL(BaseURL.IMG_POST_URL + imageUrl);

            showBigNotification(bitmap, title, message, created_at, pendingIntent);
        } else {
            simpleteNotification(title, message, created_at, pendingIntent);
        }

    }

    private void simpleteNotification(String title, String message, String timeStamp, PendingIntent pendingIntent) {

        String CHANNEL_ID = "68531";// The id of the channel.
        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        }

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bm)
                .setContentTitle(title)
                .setContentText(Html.fromHtml(message))
                .setAutoCancel(true)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setSound(defaultSoundUri)
                .setChannelId(CHANNEL_ID)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(123 /* ID of notification */, notificationBuilder.build());
    }

    private void showBigNotification(Bitmap bitmap, String title, String message, String timeStamp, PendingIntent resultPendingIntent) {
        NotificationCompat.BigPictureStyle bigPictureStyle = new NotificationCompat.BigPictureStyle();
        bigPictureStyle.setBigContentTitle(title);
        bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        bigPictureStyle.bigPicture(bitmap);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        String CHANNEL_ID = "68531";// The id of the channel.
        NotificationChannel mChannel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentText(Html.fromHtml(message));
        builder.setContentTitle(title);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
        builder.setAutoCancel(true);
        builder.setChannelId(CHANNEL_ID);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(354, builder.build());
    }

    /**
     * Downloading push notification image before displaying it in
     * the notification tray
     */
    public Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getTimeMilliSec(String timeStamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = format.parse(timeStamp);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

}