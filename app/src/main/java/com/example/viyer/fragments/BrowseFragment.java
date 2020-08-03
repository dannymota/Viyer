package com.example.viyer.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.viyer.LoginActivity;
import com.example.viyer.R;
import com.example.viyer.adapters.ProductsAdapter;
import com.example.viyer.models.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.viyer.MainActivity.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BrowseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BrowseFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Button btnLogout;
    private RecyclerView rvProducts;
    private ProductsAdapter adapter;
    private List<Product> products;
    private FirebaseUser user;
    private TextView tvNoMessage;
    private ImageView ivDog;
    private TextView tvSortBy;
    private TextView tvDetail;
    private int checkItem;
    private ImageView ivSort;

    public BrowseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BrowseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BrowseFragment newInstance(String param1, String param2) {
        BrowseFragment fragment = new BrowseFragment();
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
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        checkItem = 0;

        btnLogout = view.findViewById(R.id.btnLogout);
        rvProducts = view.findViewById(R.id.rvProducts);
        tvNoMessage = view.findViewById(R.id.tvNoMessage);
        tvSortBy = view.findViewById(R.id.tvSortBy);
        tvDetail = view.findViewById(R.id.tvDetail);
        ivDog = view.findViewById(R.id.ivDog);
        ivSort = view.findViewById(R.id.ivSort);

        products = new ArrayList<>();
        adapter = new ProductsAdapter(getContext(), products);
        refreshAdapter();

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                getLoginActivity();
            }
        });

        getProducts();

        tvSortBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSorting();
            }
        });

        ivSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void getLoginActivity() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        getActivity().onBackPressed();
    }

    private void getProducts() {
        LoginActivity.db().collection("posts")
                .limit(200)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            List<Product> result = task.getResult().toObjects(Product.class);
                            result.removeIf(p -> p.getUid().equals(user.getUid()));

                            if (result.isEmpty()) {
                                tvNoMessage.setVisibility(View.VISIBLE);
                                ivDog.setVisibility(View.VISIBLE);
                            } else {
                                tvNoMessage.setVisibility(View.INVISIBLE);
                                ivDog.setVisibility(View.INVISIBLE);
                                tvDetail.setVisibility(View.VISIBLE);
                                tvSortBy.setVisibility(View.VISIBLE);
                                ivSort.setVisibility(View.VISIBLE);
                            }
                            products.addAll(result);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void changeSorting() {
        final CharSequence[] MAP_TYPE_ITEMS =
                {"Recent", "Price", "Location"};

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Sort by");
        builder.setSingleChoiceItems(
                MAP_TYPE_ITEMS,
                checkItem,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                tvSortBy.setText("Sort by Recent");
                                checkItem = 0;
                                adapter.clear();
                                getProducts();
                                adapter.notifyDataSetChanged();
                                refreshAdapter();
                                break;
                            case 1:
                                tvSortBy.setText("Sort by Price");
                                checkItem = 1;
                                mergeSort(products);
                                adapter.notifyDataSetChanged();
                                refreshAdapter();
                                break;
                            case 2:
                                tvSortBy.setText("Sort by Location");
                                checkItem = 2;
                                break;
                        }
                        dialog.dismiss();
                    }
                }
        );
        builder.show();
    }

    private static List<Product> merge(final List<Product> left, final List<Product> right) {
        final List<Product> merged = new ArrayList<>();
        while (!left.isEmpty() && !right.isEmpty()) {
            if(left.get(0).getPrice() - right.get(0).getPrice() <= 0) {
                merged.add(left.remove(0));
            } else {
                merged.add(right.remove(0));
            }
        }
        merged.addAll(left);
        merged.addAll(right);
        return merged;
    }

    public static void mergeSort(final List<Product> products) {
        boolean addSwitch = true;
        if (products.size() >= 2) {
            final List<Product> left = new ArrayList<Product>();
            final List<Product> right = new ArrayList<Product>();

            while (!products.isEmpty()) {
                if (addSwitch) {
                    left.add(products.remove(0));
                } else {
                    right.add(products.remove(products.size() / 2));
                }
                addSwitch = !addSwitch;
            }
            mergeSort(left);
            mergeSort(right);
            products.addAll(merge(left, right));
        }
    }

    public void refreshAdapter() {
        rvProducts.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        rvProducts.setLayoutManager(layoutManager);
        rvProducts.setItemAnimator(new DefaultItemAnimator());
    }
}