package com.example.viyer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VerifyIdentityActivity extends AppCompatActivity {

    private final int PICK_IMAGE_REQUEST = 22;
    public final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 40;
    private Uri photoUri;
    private ImageView ivID;
    private ImageView ivPhoto;
    private Boolean photoID;
    private Boolean faceID;
    public FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_identity);

        photoID = false;
        faceID = false;

        ivID = findViewById(R.id.ivID);
        ivPhoto = findViewById(R.id.ivPhoto);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ivID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGovernment();
            }
        });

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFace();
            }
        });
    }

    private void selectGovernment() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void selectFace() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (intent.resolveActivity(this.getPackageManager()) != null) {
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
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.viyer.contentprovider",
                        photoFile);
                photoUri = photoURI;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


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
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                if (requestCode == PICK_IMAGE_REQUEST) {
                    ivID.setImageBitmap(bitmap);
                } else {
                    ivPhoto.setImageBitmap(bitmap);
                }

                FaceDetector faceDetector = new FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false).build();

                if(!faceDetector.isOperational()){
                    Toast.makeText(VerifyIdentityActivity.this,"Could not detect faces", Toast.LENGTH_SHORT).show();
                    return;
                }

                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<Face> faces = faceDetector.detect(frame);

                if (faces.size() == 1) {
                    if (requestCode == PICK_IMAGE_REQUEST) {
                        photoID = true;
                        Toast.makeText(VerifyIdentityActivity.this,"Verified Government ID", Toast.LENGTH_SHORT).show();
                        ivID.setEnabled(false);
                    } else {
                        faceID = true;
                        Toast.makeText(VerifyIdentityActivity.this,"Verified Face", Toast.LENGTH_SHORT).show();
                        ivPhoto.setEnabled(false);
                    }
                } else {
                    if (requestCode == PICK_IMAGE_REQUEST) {
                        Toast.makeText(VerifyIdentityActivity.this,"Please add a better photo of your ID", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(VerifyIdentityActivity.this,"Please take a better photo of your face", Toast.LENGTH_SHORT).show();
                    }
                }

                if (photoID && faceID) {
                    Toast.makeText(VerifyIdentityActivity.this,"Identity verified, please refresh for update", Toast.LENGTH_SHORT).show();
                    identityVerified();
                }

                Log.d("Verify", "Size: " + faces.size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void identityVerified() {
        LoginActivity.db().collection("users")
                .document(firebaseUser.getUid())
                .update("id", true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Verify", "Error updating document", e);
                    }
                });
    }
}