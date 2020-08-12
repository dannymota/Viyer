package com.example.viyer.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.viyer.LoginActivity;
import com.example.viyer.R;
import com.example.viyer.SignUpActivity;
import com.example.viyer.VerifyIdentityActivity;
import com.example.viyer.adapters.ProductsAdapter;
import com.example.viyer.layouts.SquareRelativeLayout;
import com.example.viyer.models.Product;
import com.example.viyer.models.ProductAdsData;
import com.example.viyer.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileActivity";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser firebaseUser;
    private User user;
    private TextView tvFullName;
    private TextView tvLocation;
    private TextView tvCreatedAt;
    private SimpleDateFormat simpleDateFormat;
    private TextView tvVerifyNow;
    private ImageView ivVerified;
    private RecyclerView rvView;
    private ProductsAdapter adapter;
    private List<ProductAdsData> products;
    private TabLayout tabLayout;
    private Toolbar mToolbar;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.profile_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.profileLogout);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profileLogout:
                FirebaseAuth.getInstance().signOut();
                getLoginActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getLoginActivity() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        getActivity().onBackPressed();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        tvVerifyNow = view.findViewById(R.id.tvVerifyNow);
        ivVerified = view.findViewById(R.id.ivVerified);
        rvView = view.findViewById(R.id.rvView);
        tabLayout = view.findViewById(R.id.tabLayout);

        mToolbar = view.findViewById(R.id.profileToolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setTitle("Profile");

        products = new ArrayList<>();
        adapter = new ProductsAdapter(getContext(), products);

        rvView.setAdapter(adapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        rvView.setLayoutManager(layoutManager);
        rvView.setItemAnimator(new DefaultItemAnimator());
        rvView.setItemAnimator(new DefaultItemAnimator());

        simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getSelling();
        getUser(firebaseUser.getUid());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    adapter.clear();
                    getSelling();
                    refreshAdapter();
                } else {
                    adapter.clear();
                    getLikes();
                    refreshAdapter();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        tvVerifyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    Intent i = new Intent(getContext(), VerifyIdentityActivity.class);
                    startActivity(i);
            }
        });
    }

    private void getUser(String uid) {
        DocumentReference userRef = LoginActivity.db().collection("users").document(uid);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        user = document.toObject(User.class);

                        tvFullName.setText(user.getFirstName() + " " + user.getLastName());
                        tvLocation.setText(user.getLocation());
                        tvCreatedAt.setText(simpleDateFormat.format(user.getUpdatedAt()));
                        if (user.getId()) {
                            tvVerifyNow.setVisibility(View.GONE);
                        } else {
                            ivVerified.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d(TAG, "User doesn't exist: ", task.getException());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void getLikes() {
        LoginActivity.db().collection("posts").whereArrayContains("likes", firebaseUser.getUid())
                .limit(50)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            List<ProductAdsData> resultsList = new ArrayList<>();
                            List<Product> result = task.getResult().toObjects(Product.class);
                            for (Product product : result) {
                                ProductAdsData data = new ProductAdsData();
                                data.type = 2;
                                data.product = product;
                                data.ads = null;
                                resultsList.add(data);
                            }
                            adapter.setProductAdsList(resultsList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void getSelling() {
        LoginActivity.db().collection("posts").whereEqualTo("uid", firebaseUser.getUid())
                .limit(50)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            List<ProductAdsData> resultsList = new ArrayList<>();
                            List<Product> result = task.getResult().toObjects(Product.class);
                            for (Product product : result) {
                                ProductAdsData data = new ProductAdsData();
                                data.type = 2;
                                data.product = product;
                                data.ads = null;
                                resultsList.add(data);
                            }
                            adapter.setProductAdsList(resultsList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void refreshAdapter() {
        rvView.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        rvView.setLayoutManager(layoutManager);
        rvView.setItemAnimator(new DefaultItemAnimator());
    }
}