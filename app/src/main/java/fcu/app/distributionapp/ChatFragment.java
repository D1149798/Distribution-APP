package fcu.app.distributionapp;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fcu.app.distributionapp.adapter.ChatAdapter;
import fcu.app.distributionapp.model.ChatItem;
import fcu.app.distributionapp.model.Message;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private String chatId = "C0GbAovQ5HRA14xrz9mI";
    private static final String CHAT_ID = "chat_id";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String groupId;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private Button sendButton;
    private EditText inputBox;
    private FirebaseFirestore db;
    private List<Message> messageList = new ArrayList<>();
    private static final String ARG_GROUP_ID = "group_id";
    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String groupId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView_messages); // 改這裡：要用 view.findViewById
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // 改 this → getContext()
        chatAdapter = new ChatAdapter(); // 確保 ChatAdapter 有無參建構子
        recyclerView.setAdapter(chatAdapter);
        sendButton = view.findViewById(R.id.button_send);
        inputBox = view.findViewById(R.id.editText_message);

        sendButton.setOnClickListener(v -> {
            String text = inputBox.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                inputBox.setText("");  // 清空輸入框
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference messagesRef = db.collection("newGroups")
                .document(groupId)
                .collection("messages");

        messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("ChatFragment", "Listen failed.", error);
                            return;
                        }

                        List<ChatItem> chatItemList = new ArrayList<>();
                        Date lastDate = null;

                        for (QueryDocumentSnapshot doc : value) {
                            Message msg = doc.toObject(Message.class);
                            Date msgDate = msg.getTimestamp().toDate();
                            //messageList.add(msg);
                            if (lastDate == null || !isSameDay(msgDate, lastDate)) {
                                String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(msgDate);
                                chatItemList.add(new ChatItem(ChatItem.Type.DATE, dateString));

                                lastDate = msgDate;
                            }

                            chatItemList.add(new ChatItem(ChatItem.Type.MESSAGE, msg));
                        }

                        chatAdapter.setMessages(chatItemList);
                        recyclerView.post(() -> recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1));

                    }
                });
        if (groupId != null) {
            loadMessages();
        }
        return view;
    }

    private void loadMessages() {
        db.collection("newGroups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    messageList.clear();
                    for (var doc : querySnapshot) {
                        Message message = doc.toObject(Message.class);
                        messageList.add(message);
                    }
                    chatAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "載入訊息失敗：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ChatFragment", "讀取 messages 失敗", e);
                });
    }
    private void sendMessage(String text) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) return;

        String senderEmail = currentUser.getEmail();

        // 查找 users 裡符合該 email 的文件
        db.collection("users")
                .whereEqualTo("Email", senderEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    String senderName = "匿名"; // 預設值

                    if (!queryDocumentSnapshots.isEmpty()) {
                        // 假設 email 是唯一的，只會找到一個文件
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        senderName = doc.getId(); // 這就是你要的文件 ID（如 "123456"）
                    }

                    // 建立訊息物件
                    Message message = new Message(
                            senderEmail,
                            senderName,
                            text,
                            Timestamp.now()
                    );

                    // 寫入 Firestore：chats/test_chat/messages 子集合
                    db.collection("newGroups")
                            .document(groupId)
                            .collection("messages")
                            .add(message)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("Chat", "訊息已送出");
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Chat", "送出訊息失敗", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w("Chat", "查詢使用者失敗", e);
                });
    }

    private boolean isSameDay(Date date1, Date date2) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }



//    private void sendMessage(String text) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        // 假設這些是目前登入的使用者資訊
//        String senderId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();  // 如果你有設定 displayName
//
//        // 建立訊息物件
//        Message message = new Message(
//                senderId,
//                senderName != null ? senderName : "匿名",
//                text,
//                Timestamp.now()
//        );
//
//        // 寫入 Firestore：你這裡應該是 chats/test_chat/messages 子集合
//        db.collection("chats")
//                .document("test_chat")
//                .collection("messages")
//                .add(message)
//                .addOnSuccessListener(documentReference -> {
//                    Log.d("Chat", "訊息已送出");
//                })
//                .addOnFailureListener(e -> {
//                    Log.w("Chat", "送出訊息失敗", e);
//                });
//    }

}