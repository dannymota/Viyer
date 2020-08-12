package com.example.viyer;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.viyer.models.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;

public class AugmentedProductActivity extends AppCompatActivity {

    private ModelRenderable renderable;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_product);

        FirebaseStorage storage = FirebaseStorage.getInstance();

        product = (Product) Parcels.unwrap(getIntent().getParcelableExtra(Product.class.getSimpleName()));

        StorageReference storageReference = storage.getReference().child(product.getDocumentId());

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);

        findViewById(R.id.btnDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    File file = File.createTempFile("out", "glb");
                    storageReference.getFile((file)).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            buildModel(file);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        arFragment.setOnTapArPlaneListener(((hitResult, plane, motionEvent) -> {
            AnchorNode anchorNode = new AnchorNode(hitResult.createAnchor());
            anchorNode.setRenderable(renderable);

            TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
            node.getScaleController().setMaxScale(0.02f);
            node.getScaleController().setMinScale(0.01f);

            node.setParent(anchorNode);
            node.setRenderable(renderable);

            arFragment.getArSceneView().getScene().addChild(anchorNode);
            node.select();
        }));
    }

    private void buildModel(File file) {
        RenderableSource renderableSource = RenderableSource.builder()
                .setSource(this, Uri.parse(file.getPath()), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT).build();

        ModelRenderable.builder()
                .setSource(this, renderableSource)
                .setRegistryId(file.getPath()).build()
                .thenAccept(modelRenderable -> {
                    Toast.makeText(this, "AR model loaded from server", Toast.LENGTH_SHORT).show();
                    renderable = modelRenderable;
                });
    }
}