package hk.edu.cuhk.ie.iems5722.a2_1155162647;

import static java.lang.System.out;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import hk.edu.cuhk.ie.iems5722.a2_1155162647.constant.URLConstant;

public class ChatActivity extends AppCompatActivity {

    private ImageButton sendbtn;
    private EditText edittext1;
    private MessageAdapter MessageAdapter1;
    private int ChatroomId;
    private String ChatroomName;
    public static final String myuserId = "1155162647";
    String myuserName = "Ellyn";

    int mynextpage;
    int mytotalpage;

    private ListView msglistview;
    private ArrayList<MessageEntity> message1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //设置导航栏返回键
        ActionBar actionbar = this.getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);


        //获取一些组件
        this.edittext1 = (EditText) findViewById(R.id.edittext1);
        this.sendbtn = (ImageButton) findViewById(R.id.sendbtn);
        this.msglistview = (ListView) findViewById(R.id.messagelv);

        //获取ChatRoom的id进行标识
        Bundle extras = getIntent().getExtras();
        this.ChatroomId = extras.getInt("id");
        this.ChatroomName = extras.getString("name");

//        out.println("name:" + ChatroomName);

        //标题栏标识
        setTitle(this.ChatroomName);

        //设置请求到的消息ListView及其适配器
        this.message1 = new ArrayList<MessageEntity>();
        this.MessageAdapter1 = new MessageAdapter(this, this.message1);
        this.msglistview.setAdapter(this.MessageAdapter1);

        //根据id请求服务器中对应ChatRoom中的消息
        String getmsgApi1 = URLConstant.GET_MESSAGES + '?' + "chatroom_id" + '=' + this.ChatroomId
                + '&' + "page" + '=' + 1;

        new getMessagesTask().execute(getmsgApi1);

        //监听发送消息事件，将自己的消息传给服务器然后再获取到页面
        sendbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
//                out.println("print button");
                sendMessage(view);
            }
        });

        ScrollPage();


    }

    public boolean onSupportNavigateUp()
    {
        finish();
        return super.onSupportNavigateUp();
    }

    class getMessagesTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected String doInBackground(String... urls) {
            String getMessage = "";
            String line;

            try {
                InputStream is = null;

                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");

                conn.setDoInput(true);
                conn.connect();

                int response = conn.getResponseCode();
//                out.println(response);

                is = conn.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    getMessage += line;
                }
//                out.println(getMessage);


            } catch (Exception e) {
                exception = e;
                return null;
            }
            return getMessage;
        }

        @Override
        protected void onPostExecute(String msg) {
            if (msg != null) {
                try {
                    JSONObject json = new JSONObject(msg);
                    String status = json.getString("status");
                    if (!status.equals("OK")) {

                    }
                    else {
                        JSONObject data = json.getJSONObject("data");
                        JSONArray array = data.getJSONArray("messages");

                        int current_page = data.getInt("current_page");
                        int total_pages = data.getInt("total_pages");

                        mynextpage = current_page + 1;
                        mytotalpage = total_pages;


//                        out.println(current_page);
//                        out.println(total_pages);
//                        out.println(data);
//                        out.println(array);

                        for (int i = 0; i < array.length(); i++) {

                            String username = array.getJSONObject(i).getString("name");
                            String time = array.getJSONObject(i).getString("message_time");
                            String message = array.getJSONObject(i).getString("message");
                            int userid = array.getJSONObject(i).getInt("user_id");

                            MessageEntity mmsg = new MessageEntity(username, userid, time, message);
                            message1.add(mmsg);
                            MessageAdapter1.notifyDataSetChanged();
                        }
//                        out.println(message1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    class sendMessagesTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        @Override
        protected String doInBackground(String... urls) {
            String sendMessage = "";
            String line;

            try {
                //连接服务器，传送数据方法用post
                out.println(urls[0]);

                URL url = new URL(urls[0]);
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
                builder.appendQueryParameter("chatroom_id",urls[1]);
                builder.appendQueryParameter("user_id",urls[2]);
                builder.appendQueryParameter("name",urls[3]);
                builder.appendQueryParameter("message",urls[4]);

                //把新建的uri写给服务器
                String query = builder.build().getEncodedQuery();
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                //读取服务器的返回结果
                int response = conn.getResponseCode();
//                out.println(response);
                if(response!=200){
                    out.println("Error");
                }

                InputStream is = conn.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine())!=null){
                    sendMessage += line;
                }
//                out.println(sendMessage);
            }catch (Exception e) {
                this.exception = e;
                return null;
            }
            return sendMessage;
        }
    }

    //获取时间戳
    public String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-DD hh:mm");
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        return format.format(new Date());
    }

    public void refreshMsg(){
        out.println("go into refresh");
        String getmsgApi3 = URLConstant.GET_MESSAGES + '?' + "chatroom_id" + '=' + this.ChatroomId
                + '&' + "page" + '=' + 1;
        message1.clear();
        new getMessagesTask().execute(getmsgApi3);
    }

    public void sendMessage(View view){
        String message = edittext1.getText().toString();
        String time = getTime();
        new sendMessagesTask().execute(URLConstant.POST_MESSAGES, ""+ChatroomId, myuserId,myuserName, message);

        //获取发送消息后的新msg页面
        String getmsgApi2 = URLConstant.GET_MESSAGES + '?' + "chatroom_id" + '=' + this.ChatroomId
                + '&' + "page" + '=' + 1;
        new getMessagesTask().execute(getmsgApi2);
        edittext1.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_chat_actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.refreshbtn:
                refreshMsg();
//                out.println("refreshhhh");
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //实现滑动功能
    public void ScrollPage() {
        msglistview.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int statusCode;
            private int firstItem;

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItem, int totalItem) {
                //FirtItem是当前能看到的第一个列表项的ID，如果是0的话就说明是列表第一项
                firstItem = firstVisibleItem;
            }

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
//                out.println("firstItem:" + msglistview.getFirstVisiblePosition());
                statusCode = i;
                //statusCode=0说明停止滑动，如果没有停止滑动并且当前显示的第一个元素是ID=0即列表第一个元素，那就加载下一页
                if (statusCode != 0 && firstItem == 0) {
                    if (mynextpage <= mytotalpage) {
                        String getmsgApi2 = URLConstant.GET_MESSAGES + '?' + "chatroom_id" + '=' + ChatroomId
                                + '&' + "page" + '=' + mynextpage;
                        new getMessagesTask().execute(getmsgApi2);
                    }

                }
            }
        });
    }

}
