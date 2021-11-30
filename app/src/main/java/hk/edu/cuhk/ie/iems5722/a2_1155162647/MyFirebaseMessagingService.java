package hk.edu.cuhk.ie.iems5722.a2_1155162647;

import static java.lang.System.out;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import hk.edu.cuhk.ie.iems5722.a2_1155162647.constant.URLConstant;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            String chatroomId = remoteMessage.getData().get("chatroom_id");
            String chatroomName = "IEMS5722";
            String message = remoteMessage.getData().get("new_message");
            sendNotification(chatroomId, chatroomName, message);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        sendRegistrationToServer(token);
    }

    public static void sendRegistrationToServer(String token) {
        try {
            URL url = new URL(URLConstant.POST_USER_TOKEN);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //获取字节输出流对象
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

            //创建uri
            Uri.Builder builder = new Uri.Builder();
            builder.appendQueryParameter("user_id",ChatActivity.myuserId);
            builder.appendQueryParameter("token",token);
            //把新建的uri写给服务器
            String query = builder.build().getEncodedQuery();
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            //读取服务器的返回结果
            int response = conn.getResponseCode();
            if(response!=200){
                out.println("Error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String id, String name, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("id",id);
        intent.putExtra("name",name);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = MyApplication.CHANNEL_ID;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle(name)
                        .setContentText(message)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setColor(getResources().getColor((R.color.black)))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
