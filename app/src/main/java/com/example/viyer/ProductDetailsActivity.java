package com.example.viyer;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.viyer.adapters.SlidesAdapter;
import com.example.viyer.helpers.CameraPermissionHelper;
import com.example.viyer.models.Chatroom;
import com.example.viyer.models.Product;
import com.example.viyer.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.parceler.Parcels;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private FirebaseUser firebaseUser;
    private ImageView ivLiked;
    private ImageView ivAR;
    private Boolean liked;
    private boolean mUserRequestedInstall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        product = (Product) Parcels.unwrap(getIntent().getParcelableExtra(Product.class.getSimpleName()));

        tvTitle = findViewById(R.id.tvTitle);
        tvLocation = findViewById(R.id.tvLocation);
        tvPrice = findViewById(R.id.tvDate);
        vpImages = findViewById(R.id.vpImages);
        btnOffer = findViewById(R.id.btnOffer);
        btnBuy = findViewById(R.id.btnBuy);
        ivLiked = findViewById(R.id.ivLiked);
        ivAR = findViewById(R.id.ivAR);

        getUpdatedLike();
        maybeEnableArButton();

        tvTitle.setText(product.getTitle());
        tvPrice.setText("$" + String.valueOf(product.getPrice()));
        tvLocation.setText(product.getDocumentId());

        adapter = new SlidesAdapter(product.getPhotoUrls(), this);
        vpImages.setAdapter(adapter);

        vpImages.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }

            private GestureDetector gestureDetector = new GestureDetector(ProductDetailsActivity.this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (liked) {
                        dislikeProduct();
                        liked = false;
                        ivLiked.setImageDrawable(ProductDetailsActivity.this.getResources().getDrawable(R.drawable.ufi_heart));
                    } else {
                        likeProduct();
                        liked = true;
                        ivLiked.setImageDrawable(ProductDetailsActivity.this.getResources().getDrawable(R.drawable.ufi_heart_active));
                    }
                    return super.onDoubleTap(e);
                }
            });

        });

        ivLiked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (liked) {
                    dislikeProduct();
                    liked = false;
                    ivLiked.setImageDrawable(ProductDetailsActivity.this.getResources().getDrawable(R.drawable.ufi_heart));
                } else {
                    likeProduct();
                    liked = true;
                    ivLiked.setImageDrawable(ProductDetailsActivity.this.getResources().getDrawable(R.drawable.ufi_heart_active));
                }
            }
        });

        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkChat();
            }
        });

        ivAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailsActivity.this, AugmentedProductActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    maybeEnableArButton();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            ivAR.setVisibility(View.VISIBLE);
            ivAR.setEnabled(true);
        } else {
            ivAR.setVisibility(View.INVISIBLE);
            ivAR.setEnabled(false);
        }
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
        chat.put("uids", Arrays.asList(product.getUid(), firebaseUser.getUid()));
        chat.put("updatedAt", new Timestamp(new Date()));
        chat.put("recentMessage", "");
        chat.put("buyerUid", firebaseUser.getUid());

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

    private void createOffer(int offer) {
        Map<String, Object> chat = new HashMap<>();
        chat.put("productId", product.getDocumentId());
        chat.put("buyerUid", firebaseUser.getUid());
        chat.put("sellerUid", product.getUid());
        chat.put("location", null);
        chat.put("offer", offer);
        chat.put("response", false);
        chat.put("status", false);
        chat.put("date", null);
        chat.put("address", null);
        chat.put("view", false);

        LoginActivity.db().collection("offers")
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
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
                .whereArrayContains("uids", firebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                getChatActivity(document.getId());
                                break;
                            }
                            if (task.getResult().getDocuments().size() == 0) {
                                createChat();
                                createOffer(product.getPrice());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void likeProduct() {
        LoginActivity.db().collection("posts").document(product.getDocumentId()).update("likes", FieldValue.arrayUnion(firebaseUser.getUid()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Image successfully written");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing image", e);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void dislikeProduct() {
        LoginActivity.db().collection("posts").document(product.getDocumentId()).update("likes", FieldValue.arrayRemove(firebaseUser.getUid()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Image successfully written");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing image", e);
                    }
                });
    }

    private void getUpdatedLike() {
        DocumentReference productRef = LoginActivity.db().collection("posts").document(product.getDocumentId());
        productRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Product product = document.toObject(Product.class);
                        if (product.getLikes().contains(firebaseUser.getUid())) {
                            liked = true;
                            ivLiked.setImageDrawable(ProductDetailsActivity.this.getResources().getDrawable(R.drawable.ufi_heart_active));
                        } else {
                            liked = false;
                            ivLiked.setImageDrawable(ProductDetailsActivity.this.getResources().getDrawable(R.drawable.ufi_heart));
                        }
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