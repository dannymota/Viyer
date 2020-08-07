package com.example.viyer.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viyer.ChatActivity;
import com.example.viyer.LoginActivity;
import com.example.viyer.MainActivity;
import com.example.viyer.R;
import com.example.viyer.models.Chatroom;
import com.example.viyer.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.parceler.Parcels;

import java.util.List;

import static com.example.viyer.MainActivity.TAG;

public class ChatroomsAdapter extends RecyclerView.Adapter<ChatroomsAdapter.ViewHolder> {

    private Context context;
    private List<Chatroom> chatrooms;
    private FirebaseUser user;

    public ChatroomsAdapter(Context context, List<Chatroom> chatrooms, FirebaseUser user) {
        this.context = context;
        this.chatrooms = chatrooms;
        this.user = user;
    }

    @NonNull
    @Override
    public ChatroomsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chatroom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatroomsAdapter.ViewHolder holder, int position) {
        Chatroom chatroom = chatrooms.get(position);
        holder.bind(chatroom);
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Product product;
        private TextView tvName;
        private TextView tvMessage;
        private TextView tvUpdatedAt;
        private TextView tvAgentType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFullName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvUpdatedAt = itemView.findViewById(R.id.tvUpdatedAt);
            tvAgentType = itemView.findViewById(R.id.tvLocation);
            itemView.setOnClickListener(this);
        }

        public void bind(Chatroom chatroom) {
            String uidOther = chatroom.getUids().get(0).equals(user.getUid()) ? chatroom.getUids().get(1) : user.getUid();
            Boolean isBuyer = chatroom.getBuyerUid().equals(user.getUid()) ? true : false;
            tvName.setText(uidOther);
            tvMessage.setText(chatroom.getRecentMessage());
            tvUpdatedAt.setText(MainActivity.getRelativeTimeAgo(String.valueOf(chatroom.getUpdatedAt())));

            if (isBuyer) {
                tvAgentType.setText("Buying");
                tvAgentType.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                tvAgentType.setText("Selling");
                tvAgentType.setTextColor(Color.parseColor("#F44336"));
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Chatroom chatroom = chatrooms.get(position);
                getProduct(chatroom, chatroom.getProductId());
            }
        }

        private void getProduct(final Chatroom chatroom, String productId) {
            DocumentReference productRef = LoginActivity.db().collection("posts").document(productId);
            productRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            product = document.toObject(Product.class);
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("chatId", chatroom.getDocumentId());
                            intent.putExtra("buyerUid", chatroom.getBuyerUid());
                            intent.putExtra(Product.class.getSimpleName(), Parcels.wrap(product));
                            context.startActivity(intent);
                        } else {
                            Log.d(TAG, "Document doesn't exist: ", task.getException());
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }


    }
}
