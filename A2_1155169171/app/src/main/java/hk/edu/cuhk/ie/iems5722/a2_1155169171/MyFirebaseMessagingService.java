package hk.edu.cuhk.ie.iems5722.a2_1155169171;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";
    @Override
    // This callback function will be called when an FCM message is received (except for a notification message is received when app is in background)
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String msg = remoteMessage.getData().get("Msg");
            String title = remoteMessage.getData().get("Title");
            String id = remoteMessage.getData().get("Id");
            System.out.println("1111111111111: "+id);
            sendNotification(id,title, msg);
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }
        // Check if message contains a notification payload.
//        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message notification payload: " + remoteMessage.getNotification().getBody());
//            Log.d(TAG, "Message Notification title: " + remoteMessage.getNotification().getTitle());
//            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
//        }

        //Once the data is received, your app can:
        //???Generation a notification
        //???Jump to a new page
        //???Send request to server to fetch new data
    }
    // Other overridden methods
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }
    private void sendRegistrationToServer(String token) {
        // Implement your own logic to submit token to server
        FormBody formBody = new FormBody.Builder()
                .add("token", token)
                .add("user_id", "1155169171")
                .build();
        Request postMessages = new Request.Builder()
                .url("http://47.250.43.42/api/a4/submit_push_token")
                .post(formBody)
                .build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(postMessages).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();
                    try {
                        //setResponse(new JSONObject(data));
                        System.out.println(new JSONObject(data));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
    public void sendNotification(String id,String title, String context) {

        String CHANNEL_ID = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(title)
                .setContentText(context)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true);

//        Intent indent = new Intent(this, MainActivity.class);
        Intent indent = new Intent(this, ChatActivity.class);
        indent.putExtra("room_title",title);
        indent.putExtra("room_id",id);
        System.out.println("33333333333: "+id);
//        indent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        indent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        indent.addCategory(Intent.CATEGORY_LAUNCHER);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(indent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, indent, PendingIntent.FLAG_IMMUTABLE);

        builder.setContentIntent(pendingIntent);
        builder.setFullScreenIntent(pendingIntent, true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, builder.build());

    }
    private void scheduleJob() { }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }
}
