package com.example.viyer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.viyer.adapters.SlidesAdapter;
import com.example.viyer.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.parceler.Parcels;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.viyer.MainActivity.TAG;

public class ProductDetailsActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvLocation;
    private TextView tvPrice;
    private ViewPager vpImages;
    private Button btnOffer;
    private Button btnBuy;
    public Product product;
    private SlidesAdapter adapter;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        tvTitle = findViewById(R.id.tvTitle);
        tvLocation = findViewById(R.id.tvLocation);
        tvPrice = findViewById(R.id.tvPrice);
        vpImages = findViewById(R.id.vpImages);
        btnOffer = findViewById(R.id.btnOffer);
        btnBuy = findViewById(R.id.btnBuy);

        user = FirebaseAuth.getInstance().getCurrentUser();
        product = (Product) Parcels.unwrap(getIntent().getParcelableExtra(Product.class.getSimpleName()));

        tvTitle.setText(product.getTitle());
        tvPrice.setText("$" + product.getPrice());
        tvLocation.setText(product.getDocumentId());

        adapter = new SlidesAdapter(product.getPhotoUrls(), this);
        vpImages.setAdapter(adapter);

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkChat();
            }
        });
    }

    private void getChatActivity(String chatId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra(Product.class.getSimpleName(), Parcels.wrap(product));
        startActivity(intent);
        finish();
    }

    private void createChat() {
        Map<String, Object> chat = new HashMap<>();
        chat.put("productId", product.getDocumentId());
        chat.put("uids", Arrays.asList(product.getUid(), user.getUid()));
        chat.put("updatedAt", new Timestamp(new Date()));
        chat.put("recentMessage", "");

        LoginActivity.db().collection("chats")
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        getChatActivity(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void checkChat() {
        LoginActivity.db().collection("chats")
                .whereEqualTo("productId", product.getDocumentId())
                .whereArrayContains("uids", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                getChatActivity(document.getId());
                                Log.d(TAG, "Found chat with documentId: " + document.getId());
                                break;
                            }
                            if (task.getResult().getDocuments().size() == 0) {
                                Log.d(TAG, "Could not find productId: " + product.getDocumentId() + "with user:" + user.getUid());
                                createChat();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}