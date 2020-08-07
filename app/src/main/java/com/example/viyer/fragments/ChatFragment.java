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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.viyer.LoginActivity;
import com.example.viyer.R;
import com.example.viyer.adapters.ChatroomsAdapter;
import com.example.viyer.models.Chatroom;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.viyer.MainActivity.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView rvMessages;
    private ChatroomsAdapter adapter;
    private FirebaseUser user;
    private List<Chatroom> chatrooms;
    private LinearLayout linearLayoutManager;
    private ImageView ivDog;
    private TextView tvNoMessage;
    private Toolbar mToolbar;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
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
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.chatroom_fragment, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbar = view.findViewById(R.id.browseChatroom);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setTitle("Chatroom");

        chatrooms = new ArrayList<>();

        rvMessages = view.findViewById(R.id.rvMessages);
        adapter = new ChatroomsAdapter(getContext(), chatrooms, user);
        ivDog = view.findViewById(R.id.ivDog);
        tvNoMessage = view.findViewById(R.id.tvNoMessage);

        rvMessages.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvMessages.setLayoutManager(layoutManager);

        getLiveChatrooms();
    }

    private void getChatrooms() {
        LoginActivity.db().collection("chats")
                .whereArrayContains("uids", user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<Chatroom> result = task.getResult().toObjects(Chatroom.class);
                            chatrooms.addAll(result);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void getLiveChatrooms() {
        LoginActivity.db().collection("chats")
                .whereArrayContains("uids", user.getUid())
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (snapshots.isEmpty()) {
                            tvNoMessage.setVisibility(View.VISIBLE);
                            ivDog.setVisibility(View.VISIBLE);
                        } else {
                            tvNoMessage.setVisibility(View.INVISIBLE);
                            ivDog.setVisibility(View.INVISIBLE);
                        }

                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                Chatroom result = dc.getDocument().toObject(Chatroom.class);
                                chatrooms.add(result);
                                adapter.notifyItemInserted(chatrooms.size() - 1);
                            } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                                Chatroom result = dc.getDocument().toObject(Chatroom.class);
                                int position = getChatroomIndex(result.getDocumentId());
                                if (position != -1) {
                                    chatrooms.set(position, result);
                                    adapter.notifyItemChanged(position);
                                    swapeItem(position, 0);
                                }
                            }
                        }

                    }
                });
    }

    private int getChatroomIndex(final String documentId) {
        for (Chatroom chatroom : chatrooms) {
            if (chatroom.getDocumentId().equals(documentId)) {
                return chatrooms.indexOf(chatroom);
            }
        }
        return -1;
    }

    public void swapeItem(int fromPosition, int toPosition){
        Collections.swap(chatrooms, fromPosition, toPosition);
        adapter.notifyItemMoved(fromPosition, toPosition);
    }
}