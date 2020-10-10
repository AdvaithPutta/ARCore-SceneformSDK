package com.google.ar.sceneform.samples.augmentedimage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.concurrent.CompletableFuture;

public class Augmented3DObject extends AnchorNode {
    private static final String TAG = "Augmented3DObject";
    // The augmented image represented by this node.
    private AugmentedImage image;
    private ArFragment arFragment;
    private ModelRenderable natarajaRenderable;

    // 3d model is initiated here.  We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private static CompletableFuture<ModelRenderable> imageCenterObject;


    public Augmented3DObject(Context context) {
        // Upon construction, start loading the models for the corners of the frame.


        if (imageCenterObject == null) {
           imageCenterObject =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/assisi.sfb"))
                            .build();


        }
    }
    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    public void setImage(AugmentedImage image) {
        this.image = image;

        // If any of the models are not loaded, then recurse when all are loaded.
        if (!imageCenterObject.isDone()) {
            CompletableFuture.allOf(imageCenterObject)
                    .thenAccept((Void aVoid) -> setImage(image))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }

        // Set the anchor based on the center of the image.
        setAnchor(image.createAnchor(image.getCenterPose()));

        // Make the center node nodes.
        Vector3 localPosition = new Vector3();
        Node cornerNode;

        // center node.
        localPosition.set(0.0f * image.getExtentX(), 0.0f, 0.0f * image.getExtentZ());
       
        cornerNode = new Node();
        cornerNode.setParent(this);
        cornerNode.setLocalPosition(localPosition);
        cornerNode.setRenderable(imageCenterObject.getNow(null));


    }

    public AugmentedImage getImage() {
        return image;
    }
}

