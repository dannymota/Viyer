package com.example.viyer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.viyer.models.Offer;
import com.example.viyer.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
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
    private String buyerUid;
    private LinearLayoutManager linearLayoutManager;
    private Bundle bundle;
    private Product product;
    private TextView tvProductTitle;
    private TextView tvSellerUid;
    private ImageView ivProduct;
    private ImageButton btnSuggest;


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
        btnSuggest = findViewById(R.id.btnSuggest);

        user = FirebaseAuth.getInstance().getCurrentUser();

        messages = new ArrayList<>();
        adapter = new ChatsAdapter(this, user.getUid(), messages);
        rvMessages.setAdapter(adapter);

        linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        rvMessages.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true);

        bundle = getIntent().getExtras();
        chatId = bundle.getString("chatId");
        buyerUid = bundle.getString("buyerUid");
        product = (Product) Parcels.unwrap(getIntent().getParcelableExtra(Product.class.getSimpleName()));

        tvProductTitle.setText(product.getTitle());
        tvSellerUid.setText(product.getUid());
        Glide.with(this).load(product.getPhotoUrls().get(0)).into(ivProduct);

        getMessages(chatId);
        getOffers();

        btnSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChatActivity.this, SuggestLocationActivity.class);
                intent.putExtra(Product.class.getSimpleName(), Parcels.wrap(product));
                startActivity(intent);
            }
        });

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_out_right, android.R.anim.slide_in_left);
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

    public void getOffers() {
        LoginActivity.db().collection("offers")
                .whereEqualTo("productId", product.getDocumentId())
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
                                case MODIFIED:
                                    Offer added = dc.getDocument().toObject(Offer.class);
                                    if (!added.getBuyerUid().equals(user.getUid())
                                            && added.getLocation() != null
                                            && added.getBuyerUid().equals(buyerUid)
                                            && !product.getLocked() && !added.isResponse()) {
                                        showOffer(added);
                                    }  else if (added.getBuyerUid().equals(user.getUid()) && added.isResponse() && !added.isStatus()) {
                                        offerUpdate("rejected");
                                    } else if (added.getBuyerUid().equals(user.getUid()) && added.isResponse() && added.isStatus()) {
                                        offerUpdate("accepted");
                                    }
                                    break;
                                case REMOVED:
                                    Log.d(TAG, "Removed offer: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });
    }

    private void showOffer(Offer offer) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Offer Received")
                .setMessage("Congratulations, you have an offer for " + product.getTitle()
                        + ". " + buyerUid + " is willing to pay "
                        + offer.getOffer() + " and meetup at " + offer.getLocation() + ". Do you accept?");
        builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateOffer("offers", offer.getDocumentId(), "status", true);
                updateOffer("offers", offer.getDocumentId(), "response", true);
                updateOffer("posts", product.getDocumentId(), "locked", true);
            }
        });
        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateOffer("offers", offer.getDocumentId(), "response", true);
            }
        });
        builder.show();
    }

    private void offerUpdate(String status) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Offer Accepted")
                .setMessage("The seller has " + status + " your offer.");
        builder.show();
    }

    private void updateOffer(String collection, String documentId, String field, boolean status) {
        LoginActivity.db().collection(collection).document(documentId).update(field, status).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully updated!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "DocumentSnapshot failed!");
            }
        });
    }
}