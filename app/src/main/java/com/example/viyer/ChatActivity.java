package com.example.viyer;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viyer.adapters.ChatsAdapter;
import com.example.viyer.fragments.BrowseFragment;
import com.example.viyer.fragments.PostFragment;
import com.example.viyer.models.Message;
import com.example.viyer.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.viyer.MainActivity.TAG;

public class ChatActivity extends AppCompatActivity {

    private FirebaseUser user;
    private List<Message> messages;
    private RecyclerView rvMessages;
    private ChatsAdapter adapter;
    private Button btnSend;
    private EditText etMessage;
    private String chatId;
    private LinearLayoutManager linearLayoutManager;
    private Bundle bundle;
    private Product product;
    private TextView tvProductTitle;
    private TextView tvSellerUid;
    private ImageView ivProduct;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        rvMessages = findViewById(R.id.rvMessages);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        tvProductTitle = findViewById(R.id.tvProductTitle);
        tvSellerUid = findViewById(R.id.tvSellerUid);
        ivProduct = findViewById(R.id.ivProduct);

        user = FirebaseAuth.getInstance().getCurrentUser();

        messages = new ArrayList<>();
        adapter = new ChatsAdapter(this, user.getUid(), messages);
        rvMessages.setAdapter(adapter);

        linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        rvMessages.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);

        bundle = getIntent().getExtras();
        chatId = bundle.getString("chatId");
        product = (Product) Parcels.unwrap(getIntent().getParcelableExtra(Product.class.getSimpleName()));

        tvProductTitle.setText(product.getTitle());
        tvSellerUid.setText(product.getUid());
        Glide.with(this).load(product.getPhotoUrls().get(0)).into(ivProduct);

        getMessages(chatId);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!PostFragment.isEmpty(etMessage)) {
                    sendMessage(etMessage.getText().toString(), chatId);
                    etMessage.setText("");
                }
            }
        });
    }

    private void sendMessage(final String message, final String chatId) {
        Map<String, Object> data = new HashMap<>();
        data.put("content", message);
        data.put("createdAt", new Timestamp(new Date()));
        data.put("fromUid", user.getUid());

        LoginActivity.db().collection("chats")
                .document(chatId)
                .collection("messages")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        updateRecentChatTime(chatId);
                        updateRecentChatMessage(message, chatId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void updateRecentChatTime(String chatId) {
        LoginActivity.db().collection("chats")
                .document(chatId)
                .update("updatedAt", new Timestamp(new Date()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                        }
                    })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private void updateRecentChatMessage(String message, String chatId) {
        LoginActivity.db().collection("chats")
                .document(chatId)
                .update("recentMessage", message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    public void getMessages(String chatId) {
        LoginActivity.db().collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Message result = dc.getDocument().toObject(Message.class);
                                    messages.add(0, result);
                                    rvMessages.smoothScrollToPosition(0);
                                    adapter.notifyItemInserted(0);
                                    break;
                                case MODIFIED:
                                    Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });
    }
}