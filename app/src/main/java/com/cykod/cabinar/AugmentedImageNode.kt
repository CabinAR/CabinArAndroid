package com.cykod.cabinar

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.util.concurrent.CompletableFuture

class AugmentedImageNode : AnchorNode {

    private val TAG = "AugmentedImageNode"

    // The augmented image represented by this node.
    private var augmentedImage: AugmentedImage? = null

    constructor(context: Context) {
    }

    var image: AugmentedImage?
        get() {
            return augmentedImage
        }
        set(value) {
            this.augmentedImage = value

            if(value != null) {
                // Set the anchor based on the center of the image.
                anchor = value!!.createAnchor(value!!.centerPose)
            }
        }

}