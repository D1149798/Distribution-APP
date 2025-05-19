package fcu.app.distributionapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fcu.app.distributionapp.R;
import fcu.app.distributionapp.model.Message;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // 假設目前使用者的 ID（之後可以改成從 Firebase auth 取得）

    private static final int VIEW_TYPE_DATE = 0;
    private static final int VIEW_TYPE_ME = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);
        return msg.getSenderId().equals(currentUserId) ? VIEW_TYPE_ME : VIEW_TYPE_OTHER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ME) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_self, parent, false);
            return new MyMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message, parent, false);
            return new OtherMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = messages.get(position);
        if (holder instanceof MyMessageViewHolder) {
            ((MyMessageViewHolder) holder).bind(msg);
        } else if (holder instanceof OtherMessageViewHolder) {
            ((OtherMessageViewHolder) holder).bind(msg);
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeTextView;

        public MyMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message);
            timeTextView = itemView.findViewById(R.id.textView_time);
        }

        public void bind(Message msg) {
            messageText.setText(msg.getContent());

            Timestamp ts = msg.getTimestamp();
            if (ts != null) {
                Date date = ts.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeText = sdf.format(date);
                timeTextView.setText(timeText); // <-- 指定時間顯示位置
            } else {
                timeTextView.setText(""); // 還沒同步好
            }
        }
    }

    static class OtherMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView senderNameText;
        TextView timeTextView;

        public OtherMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tv_message);
            senderNameText = itemView.findViewById(R.id.textView_sender);
            timeTextView = itemView.findViewById(R.id.textView_time);
        }

        public void bind(Message msg) {
            messageText.setText(msg.getContent());
            senderNameText.setText(msg.getSenderName());
            Timestamp ts = msg.getTimestamp();
            if (ts != null) {
                Date date = ts.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeText = sdf.format(date);
                timeTextView.setText(timeText); // <-- 指定時間顯示位置
            } else {
                timeTextView.setText(""); // 還沒同步好
            }
        }
    }


}
