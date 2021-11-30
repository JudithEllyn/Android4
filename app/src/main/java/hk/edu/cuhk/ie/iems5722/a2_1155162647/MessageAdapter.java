package hk.edu.cuhk.ie.iems5722.a2_1155162647;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<MessageEntity> {
    public MessageAdapter(Context context, ArrayList<MessageEntity> messages){
        super(context,0,messages);
    }


    @Override
    public MessageEntity getItem(int i){
        return super.getItem(super.getCount()-i-1);
    }


    @Override
    public View getView(int i, View convertview, ViewGroup viewGroup){
        MessageEntity msg1 = getItem(i);

        if(msg1.getUserid()==1155162647){
            convertview = LayoutInflater.from(getContext()).inflate(R.layout.item_dialog,null,false);
        }
        else{
            convertview = LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_left,null,false);
        }


        TextView usertxt = (TextView) convertview.findViewById(R.id.usertxt);
        TextView timetxt = (TextView) convertview.findViewById(R.id.timetxt);
        TextView msgtxt = (TextView) convertview.findViewById(R.id.msgtxt);

        usertxt.setText(msg1.getUsername());
        timetxt.setText(msg1.getTime());
        msgtxt.setText(msg1.getMessage());

        return convertview;

    }
}
