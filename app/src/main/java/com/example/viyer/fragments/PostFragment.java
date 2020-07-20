package com.example.viyer.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viyer.LoginActivity;
import com.example.viyer.R;
import com.example.viyer.adapters.ProductsAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.example.viyer.MainActivity.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView rvPreviews;
    private List<Uri> filePaths;
    private Uri takePhoto;
    private final int PICK_IMAGE_REQUEST = 22;
    public final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 40;
    private Button btnSelect, btnTake, btnPost;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView ivPreview;
    private FirebaseUser user;
    private ProductsAdapter adapter;
    private List<Bitmap> photos;

    private String imageFilePath;

    public PostFragment() {}

    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
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
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPreviews = view.findViewById(R.id.rvPreviews);
        btnSelect = view.findViewById(R.id.btnSelect);
        btnTake = view.findViewById(R.id.btnTake);
        btnPost = view.findViewById(R.id.btnPost);
        ivPreview = view.findViewById(R.id.ivPreview);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        filePaths = new ArrayList<>();
        photos = new ArrayList<>();

        adapter = new ProductsAdapter(photos);

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvPreviews.setLayoutManager(horizontalLayoutManagaer);
        rvPreviews.setAdapter(adapter);

//        ActionBar actionBar;
//        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
//        actionBar.setTitle("Post");

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCamera();
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    public Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, null, null);
        return Uri.parse(path);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        Boolean isMainPhoto;
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                isMainPhoto = filePaths.size() == 0 ? true : false;
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), data.getData());
                filePaths.add(data.getData());
                ivPreview.setImageBitmap(bitmap);
                photos.add(bitmap);
                adapter.notifyItemInserted(photos.size() - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data.getData() == null) {
            isMainPhoto = filePaths.size() == 0 ? true : false;
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            filePaths.add(getImageUri(bitmap));
            photos.add(bitmap);
            adapter.notifyItemInserted(photos.size() - 1);
        }
    }

    private void uploadImage() {
        if (filePaths != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            List<String> photoUrls = new ArrayList<>();

            final String postID = UUID.randomUUID().toString().replaceAll("-", "");

            addPostToCollection(postID);

            for (Uri uri : filePaths) {
                final StorageReference ref = storageReference.child("posts/" + postID + "/" + UUID.randomUUID().toString());
                ref.putFile(uri)
                        .addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(),"Post sent", Toast.LENGTH_SHORT).show();

                                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                addImageToPost(postID, uri.toString());
                                            }
                                        });
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error, Image not uploaded
                                progressDialog.dismiss();
                                Toast.makeText(getContext(),"Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnProgressListener(
                                new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    // Progress Listener for loading
                                    // percentage on the dialog box
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                        progressDialog.setMessage("Uploaded " + (int)progress + "%");
                                    }
                                });
            }

            Log.d("PostFragment", photoUrls.toString());
        }
    }

    public void addImageToPost(String postUID, String photoUrl) {
        LoginActivity.db().collection("posts").document(postUID).update("photoUrls", FieldValue.arrayUnion(photoUrl));
    }

    public void addPostToCollection(String postID) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        user.put("photoUrls", new ArrayList<>());

        LoginActivity.db().collection("posts").document(postID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}