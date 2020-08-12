package com.example.viyer.rxfirebase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class FirestoreProductFileUploader {

    public interface FileUploadEvent {

    }

    public class FileUploadSuccessEvent implements FileUploadEvent {
        public final Uri fileUri;
        public final String postId;

        FileUploadSuccessEvent(Uri fileUri, String postId) {
            this.fileUri = fileUri;
            this.postId = postId;
        }
    }

    public class FileUploadFailureEvent implements FileUploadEvent {
        public final String errorMessage;

        FileUploadFailureEvent(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public class FileUploadProgressEvent implements FileUploadEvent {
        public final int uploadProgress;

        FileUploadProgressEvent(int uploadProgress) {
            this.uploadProgress = uploadProgress;
        }
    }

    public class FileUploadCompleteEvent implements FileUploadEvent {

    }

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference  storageReference = storage.getReference();
    private final PublishSubject<FileUploadEvent> uploadSubject = PublishSubject.create();

    public Observable<FileUploadEvent> observeUploadEvents() {
        return uploadSubject.hide();
    }

    public void uploadPostFile(String postId, Uri fileUri) {
        String fileId = UUID.randomUUID().toString();
        final StorageReference ref = storageReference.child("posts/" + postId + "/" +fileId);
        ref.putFile(fileUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        FileUploadEvent progressEvent = new FileUploadProgressEvent((int) progress);
                        uploadSubject.onNext(progressEvent);
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        FileUploadEvent completeEvent = new FileUploadCompleteEvent();
                        uploadSubject.onNext(completeEvent);
                    }
                })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return ref.getDownloadUrl();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FileUploadEvent event = new FileUploadSuccessEvent(uri, postId);
                        uploadSubject.onNext(event);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        uploadSubject.onNext(new FileUploadFailureEvent(e.getMessage()));
                    }
                });
    }
}
