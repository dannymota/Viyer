package com.example.viyer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viyer.LoginActivity;
import com.example.viyer.MainActivity;
import com.example.viyer.R;
import com.example.viyer.fragments.MeetupFragment;
import com.example.viyer.models.Offer;
import com.example.viyer.models.Product;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.example.viyer.MainActivity.TAG;

public class MeetupsAdapter extends RecyclerView.Adapter<MeetupsAdapter.ViewHolder> {

    private List<Offer> offers;
    private Context context;
    private Product product;

    public MeetupsAdapter(Context context, List<Offer> offers) {
        this.offers = offers;
        this.context = context;
    }

    @NonNull
    @Override
    public MeetupsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meetup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Offer offer = offers.get(position);

        holder.bind(offer);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView tvTitle;
        private TextView tvDate;
        private TextView tvLocation;
        private TextView tvAgentType;
        private SupportMapFragment mapFragment;
        private GoogleMap map;
        private FirebaseUser user;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.etLocation);
            tvAgentType = itemView.findViewById(R.id.tvLocation);
            user = FirebaseAuth.getInstance().getCurrentUser();
            itemView.setOnClickListener(this);
        }

        public void bind(Offer offer) {
            DateFormat df = new SimpleDateFormat("dd MMMM yyyy");
            getProduct(offer.getProductId());
            tvLocation.setText(offer.getAddress());

            if (offer.getBuyerUid().equals(user.getUid())) {
                tvAgentType.setText("Buying");
            } else {
                tvAgentType.setText("Selling");
            }

            tvDate.setText(df.format(offer.getDate()));

            mapFragment = (SupportMapFragment) ((MainActivity) context).getSupportFragmentManager().findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        loadMap(map, offer);
                    }
                });
            }
        }

        protected void loadMap(GoogleMap googleMap, Offer offer) {
            map = googleMap;
            if (map != null) {
                focusLocation(offer);
            }
        }

        protected void focusLocation(Offer offer) {
            GeoPoint geoPoint = offer.getLocation();

            LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

            map.clear();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            map.addMarker(markerOptions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            map.moveCamera(cameraUpdate);
        }

        @Override
        public void onClick(View view) {

        }

        private void getProduct(String productId) {
            DocumentReference productRef = LoginActivity.db().collection("posts").document(productId);
            productRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            product = document.toObject(Product.class);
                            tvTitle.setText(product.getTitle());

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
