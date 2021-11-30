package hk.edu.cuhk.ie.iems5722.a2_1155162647;

import static java.lang.System.out;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import hk.edu.cuhk.ie.iems5722.a2_1155162647.constant.URLConstant;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ListView chatrlistview;
    private ArrayList<ChatroomEntity>myChatRooms = new ArrayList<>();
    private ArrayAdapter myChatRoomsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        isGooglePlayServicesAvailable(this);
        setContentView(R.layout.activity_main);
        getToken();
        //设置ListView显示
        this.chatrlistview = (ListView)findViewById(R.id.chatroomlv);
        myChatRoomsAdapter = new ArrayAdapter(this,R.layout.item_chatroom,myChatRooms);
        this.chatrlistview.setAdapter(myChatRoomsAdapter);
        new GetChatroom().execute(URLConstant.GET_CHATROOMS);

        //点击每个ChatRoom事件
        this.chatrlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatroomEntity room = (ChatroomEntity)chatrlistview.getItemAtPosition(i);
                int rmid = room.getId();
                String rmname = room.getName();
                Intent intent = new Intent(MainActivity.this,ChatActivity.class);
                intent.putExtra("id",rmid);
                intent.putExtra("name",rmname);
                startActivity(intent);
            }
        });

    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                        new SubmitUserPostTokenTask().execute(token);
                    }
                });
    }

    @Override
    public void onResume(){
        super.onResume();
        isGooglePlayServicesAvailable(this);


    }
    private void isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 9000).show();
            }
        }
    }

    private class GetChatroom extends AsyncTask<String, Void, String> {

        InputStream is = null;
        private Exception exception;

        @Override
        protected String doInBackground(String... urls) {

            String results = "";
            try {
//                out.println(urls[0]);
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                //进行连接
                conn.connect();
                int response = conn.getResponseCode();
                if(response!=200){
                    out.println("Error");
                }
//                out.println(response);
                is = conn.getInputStream();

                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((line=br.readLine()) != null) {
                    results += line;
                }

            } catch (Exception e) {
                this.exception = e;
                return null;
            }
//            out.println(results);
            return results;
        }


        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject json = new JSONObject(result);
                    String status = json.getString("status");
                    if (!status.equals("OK")) {

                    } else {
                        JSONArray array = json.getJSONArray("data");
                        for (int i = 0; i < array.length(); i++) {
                            int id = array.getJSONObject(i).getInt("id");
                            String name = array.getJSONObject(i).getString("name");
//                            out.println(id);
//                            out.println(name);
                            ChatroomEntity chatroom = new ChatroomEntity(id,name);
//                            out.println(chatroom);
                            myChatRooms.add(chatroom);
                            myChatRoomsAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class SubmitUserPostTokenTask extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            MyFirebaseMessagingService.sendRegistrationToServer(strings[0]);
            return null;
        }
    }
}






