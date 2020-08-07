package com.example.viyer.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.viyer.LoginActivity;
import com.example.viyer.R;
import com.example.viyer.adapters.MeetupsAdapter;
import com.example.viyer.models.Offer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.viyer.MainActivity.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MeetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeetupFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseUser user;
    private RecyclerView rvMeetups;
    private List<Offer> offers;
    private MeetupsAdapter adapter;
    private Toolbar mToolbar;

    public MeetupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MeetupFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MeetupFragment newInstance(String param1, String param2) {
        MeetupFragment fragment = new MeetupFragment();
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
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_meetup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvMeetups = view.findViewById(R.id.rvMeetups);

        offers = new ArrayList<>();

        mToolbar = view.findViewById(R.id.browseMeetup);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setTitle("Meetup");

        adapter = new MeetupsAdapter(getContext(), offers);
        rvMeetups.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvMeetups.setLayoutManager(layoutManager);

        getBuyingOffers();
    }

    private void getBuyingOffers() {
        LoginActivity.db().collection("offers")
                .whereEqualTo("buyerUid", user.getUid())
                .whereEqualTo("response", true)
                .whereEqualTo("status", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Offer> result = task.getResult().toObjects(Offer.class);
                            offers.addAll(result);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}