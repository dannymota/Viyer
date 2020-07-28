package com.example.viyer.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.viyer.LoginActivity;
import com.example.viyer.R;
import com.example.viyer.models.Offer;
import com.example.viyer.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

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
        private ImageView map;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.etLocation);
            tvAgentType = itemView.findViewById(R.id.tvLocation);
            map = itemView.findViewById(R.id.map);
            itemView.setOnClickListener(this);
        }

        public void bind(Offer offer) {
            getProduct(offer.getProductId());
            tvLocation.setText(offer.getLocation().getLatitude() + ", " + offer.getLocation().getLongitude());
            tvAgentType.setText("Buying");

            String url ="https://maps.googleapis.com/maps/api/staticmap?";
            url+="&zoom=14";
            url+="&size=330x130";
            url+="&maptype=roadmap";
            url+="&markers=color:green%7Clabel:G%7C"+offer.getLocation().getLatitude()+", "+offer.getLocation().getLongitude();
            url+="&key=AIzaSyAOJDiBTvLbWl4kX80Dzs-eRE3YcFVlXlw";

            Glide.with(context).load(url).into(map);
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
