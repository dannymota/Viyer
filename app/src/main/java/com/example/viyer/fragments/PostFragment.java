package com.example.viyer.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.viyer.LoginActivity;
import com.example.viyer.R;
import com.example.viyer.adapters.PreviewsAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private final int PICK_IMAGE_REQUEST = 22;
    public final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 40;
    private Button btnSelect, btnTake, btnPost;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private ImageView ivPreview;
    private FirebaseUser user;
    private PreviewsAdapter adapter;
    private List<Bitmap> photos;
    private EditText etTitle;
    private EditText etDesc;
    private EditText etPrice;
    private ScrollView scrollView;
    private Uri photoUri;

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
        etTitle = view.findViewById(R.id.etTitle);
        etDesc = view.findViewById(R.id.etDesc);
        etPrice = view.findViewById(R.id.etPrice);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        filePaths = new ArrayList<>();
        photos = new ArrayList<>();

        adapter = new PreviewsAdapter(photos, filePaths);

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvPreviews.setLayoutManager(horizontalLayoutManagaer);
        rvPreviews.setAdapter(adapter);

//        ActionBar actionBar;
//        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
//        actionBar.setTitle("Post");

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
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
                btnPost.setClickable(false);
                if (filePaths.size() == 0) {
                    Toast.makeText(getContext(),"Please upload at least one image", Toast.LENGTH_SHORT).show();
                    return;
                } else if (isEmpty(etTitle)) {
                    etTitle.setError("Please set a title");
                    etTitle.requestFocus();
                    return;
                }
                else if (isEmpty(etDesc)) {
                    etDesc.setError("Please set a description");
                    etDesc.requestFocus();
                    return;
                }
                else if (isEmpty(etPrice)) {
                    etPrice.setError("Please set a price");
                    etPrice.requestFocus();
                    return;
                } else {
                    uploadImage();
                }
                btnPost.setClickable(true);
            }
        });
    }

    public static boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
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

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                return;
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.viyer.contentprovider",
                        photoFile);
                photoUri = photoURI;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    public Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bitmap, null, null);
        return Uri.parse(path);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap;
        Uri uri;

        if ((requestCode == PICK_IMAGE_REQUEST || requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) && resultCode == RESULT_OK && data != null) {
            try {
                uri = data.getData() == null ? photoUri : data.getData();
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                filePaths.add(uri);
                ivPreview.setImageBitmap(bitmap);
                photos.add(bitmap);
                adapter.notifyItemInserted(photos.size() - 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePaths != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            List<String> photoUrls = new ArrayList<>();

            final String postId = UUID.randomUUID().toString().replaceAll("-", "");

            addPostToCollection(postId);

            for (Uri uri : filePaths) {
                final StorageReference ref = storageReference.child("posts/" + postId + "/" + UUID.randomUUID().toString());
                ref.putFile(uri)
                        .addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        progressDialog.dismiss();

                                        // TODO: 7/20/20 Add chain pattern to optimize callbacks
                                        Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                addImageToPost(postId, uri.toString());
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
                                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        Toast.makeText(getContext(),"Post sent", Toast.LENGTH_SHORT).show();
                                        photos.clear();
                                        adapter.notifyDataSetChanged();
                                        filePaths.clear();
                                        ivPreview.setImageResource(R.drawable.ic_launcher_background);
                                        etTitle.setText("");
                                        etDesc.setText("");
                                        etPrice.setText("");
                                    }
                                });
            }
        }
    }

    public void addImageToPost(String postUID, String photoUrl) {
        LoginActivity.db().collection("posts").document(postUID).update("photoUrls", FieldValue.arrayUnion(photoUrl))
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

    public void addPostToCollection(String postId) {
        Map<String, Object> post = new HashMap<>();
        post.put("uid", user.getUid());
        post.put("photoUrls", new ArrayList<>());
        post.put("title", etTitle.getText().toString());
        post.put("description", etDesc.getText().toString());
        post.put("price", Integer.parseInt(etPrice.getText().toString()));
        post.put("locked", false);
        post.put("likes", Arrays.asList());

        LoginActivity.db().collection("posts").document(postId)
                .set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written");
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