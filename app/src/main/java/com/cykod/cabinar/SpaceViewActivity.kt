package com.cykod.cabinar

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.ux.ArFragment

import kotlinx.android.synthetic.main.activity_space_view.*
import java.io.IOException
import java.util.HashMap
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import com.google.ar.core.exceptions.ImageInsufficientQualityException
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.HttpURLConnection
import java.net.URL


class SpaceViewActivity : AppCompatActivity() {


    private lateinit var arFragment: AugmentedImageFragment
    private lateinit var arWebview: WebView

    private var spaceId: Int = 0
    private var cabinSpace:CabinSpace? = null

    private var activePieces:HashMap<String,CabinPiece> = HashMap<String,CabinPiece>()

    private lateinit var apiClient: ApiClient

    private var showScan = true

    private val augmentedImageMap: MutableMap<AugmentedImage, AnchorNode> = mutableMapOf()

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_ID = "id"

        fun newIntent(context: Context, space: CabinSpace): Intent {
            val detailIntent = Intent(context, SpaceViewActivity::class.java)

            detailIntent.putExtra(EXTRA_TITLE, space.name)
            detailIntent.putExtra(EXTRA_ID, space.id)

            return detailIntent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_space_view)
        //setSupportActionBar(toolbar)

        /*
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }*/
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as AugmentedImageFragment
        arFragment.arSceneView.scene.addOnUpdateListener(Scene.OnUpdateListener { this.onUpdateFrame(it) })


        // Turn off the plane discovery since we're only looking for images
        arFragment.getPlaneDiscoveryController().hide()
        arFragment.getPlaneDiscoveryController().setInstructionView(null)
        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false)


        val action: String? = intent?.action
        val intentData: Uri? = intent?.data

        var apiToken:String? = null;

        // Query params from the intent link
        if(intentData != null && intentData.host == "space") {
            spaceId = intentData!!.getQueryParameter("space_id")!!.toInt()
            apiToken = intentData!!.getQueryParameter("cabin_key")
        } else {
            spaceId = intent.extras!!.getInt(EXTRA_ID)
            var sharedPref = this.getSharedPreferences("cabinar",Context.MODE_PRIVATE)
            apiToken = sharedPref.getString("api_token",null)
        }


        apiClient = ApiClient(applicationContext,apiToken)

        arWebview = findViewById(R.id.webview) as WebView
        arWebview.setBackgroundColor(0)
        arWebview.setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
        val webSettings = arWebview.settings
        webSettings.allowUniversalAccessFromFileURLs = true
        webSettings.javaScriptEnabled = true

        arWebview.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                getSpace(spaceId)
            }
        })
        arWebview.loadUrl("file:///android_asset/WebAssets/index.html")

        refresh_button.setOnClickListener {
            resetTracking()
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    fun errorOut(title:String, message:String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("ok") { _, _ ->
                this.finish()
            }.show();
    }

    fun getSpace(spaceId: Int) {
        apiClient.getSpace(spaceId) { space, message ->
            if(space != null) {
                cabinSpace = space
                setupSpace()
            } else {
                errorOut("Could not load space", "Check your network connection and retry")
            }
        }
    }

    fun setupSpace() {
        setAssets()

        val future = doAsync {
            // do your background thread task

            val markerPieces = cabinSpace!!.pieces.filter { it.markerUrl != null  && it.markerUrl != "" }

            val context = applicationContext

            var bitmaps: MutableList<Bitmap?> = mutableListOf()
            var piecesWithBitmaps: MutableList<CabinPiece> = mutableListOf()

            markerPieces.map { piece ->
                val url = URL(piece.markerUrl)
                val connection = url.openConnection() as HttpURLConnection

                val s = connection.getInputStream()
                val img = BitmapFactory.decodeStream(s )

                piecesWithBitmaps.add(piece)
                bitmaps.add(img)
            }

            uiThread {
                // use result here if you want to update ui
                loadBitmaps(piecesWithBitmaps, bitmaps)
            }
        }

        // asfd
        // get all the images
    }

    fun resetTracking() {
        // This is stupid but currently seems to be the best option
        // for sceneForm
        // https://github.com/google-ar/sceneform-android-sdk/issues/253
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);

        /*
        arWebview.evaluateJavascript("resetTracking()", { })

        augmentedImageMap.forEach { (key, value) ->
            arFragment.arSceneView.scene.removeChild(value)
            value.anchor?.detach()
            value.setParent(null)
        }

        augmentedImageMap.clear()

        fit_to_scan.visibility = View.VISIBLE
        showScan = true
        */
    }

    fun loadBitmaps(pieces: MutableList<CabinPiece>, bitmaps: MutableList<Bitmap?>) {

        if(arFragment.imageDatabase != null) {

            var config = arFragment.arSceneView.session!!.config
            var imageDatabase = AugmentedImageDatabase(arFragment.arSceneView.session!!)

            for ((index, bitmap) in bitmaps.withIndex()) {
                if(bitmap != null) {
                    try {
                        var pos = imageDatabase!!.addImage(
                            pieces[index].id.toString(),
                            bitmap
                        )
                        activePieces[pieces[index].id.toString()] = pieces[index]
                        Log.e("CabinAR", "Added images $pos")
                    } catch(e: ImageInsufficientQualityException) {
                        errorOut("Space has bad Markers", "The markers are of insufficient quality. Please upload better markers.")

                    }
                }
            }
            config.augmentedImageDatabase = imageDatabase

            arFragment.arSceneView.session!!.configure(config)

            Log.e("CabinAR", "Added images")
            Log.e("CabinAR", arFragment.arSceneView.session!!.config.augmentedImageDatabase.numImages.toString())
        }
    }

    fun setAssets() {
        arWebview.evaluateJavascript(assetString(), { })
    }

    fun assetString() : String {
        var allAssets = ""
        for (piece in cabinSpace!!.pieces) {
            if (piece.assets != null) {
                val assets = (piece.assets ?: "").replace("\"/ar-file", "\"https://www.cabin-ar.com/ar-file")
                allAssets = allAssets + assets + "\n"
            }
        }
        return "addAssets(`$allAssets`);"
    }

    fun addPieceToWebview(piece:CabinPiece) {
        arWebview.evaluateJavascript(pieceString(piece), { })
    }

    fun addPiecesToWebview() {
        var allStr = "";
        for (piece in cabinSpace!!.pieces) {
            allStr = allStr + pieceString(piece)
        }
        //allStr += ";hideAllPieces();"
        arWebview.evaluateJavascript(allStr, {});
    }


    fun pieceString(piece: CabinPiece) : String {
        return  "setPieceEntity(${piece.id}, ${piece.markerMeterWidth ?: 1.0}, `${piece.scene}`);"
    }

    fun printMatrix(mat:FloatArray) : String {
        return "[${mat[0]}, ${mat[4]}, ${mat[8]}, ${mat[12]}," +
                "${mat[1]}, ${mat[5]}, ${mat[9]}, ${mat[13]}," +
                "${mat[2]}, ${mat[6]}, ${mat[10]}, ${mat[14]}," +
                "${mat[3]}, ${mat[7]}, ${mat[11]}, ${mat[15]} ]"

    }


    private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment.arSceneView.arFrame

        // If there is no frame or ARCore is not tracking yet, just return.
        if (frame == null || frame.camera.trackingState != TrackingState.TRACKING) {
            return
        }

        var objectTransforms = "{"

        var camMat:FloatArray = FloatArray(16)
        frame.camera.getDisplayOrientedPose().toMatrix(camMat,0)

        var projMat:FloatArray = FloatArray(16)
        frame.camera.getProjectionMatrix(projMat,0,0.005f,10000.0f)

        var cameraTransform = printMatrix(camMat)
        var projectionTransform = printMatrix(projMat)


        val updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        for (augmentedImage in updatedAugmentedImages) {

            when (augmentedImage.trackingState) {
                TrackingState.PAUSED -> {
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    val text = "Detected Image " + augmentedImage.index
                    //SnackbarHelper.getInstance().showMessage(this, text)
                    Log.e("CabinAR", text)
                }

                TrackingState.TRACKING -> {
                    if(showScan) {
                        fit_to_scan.visibility = View.GONE
                        showScan = false
                    }
                    // Have to switch to UI Thread to update View.
                    //fitToScanView.setVisibility(View.GONE)
                    // Create a new anchor for newly found images.
                    var node:AugmentedImageNode?

                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        node = AugmentedImageNode(this)
                        node.image = augmentedImage
                        augmentedImageMap[augmentedImage] = node
                        arFragment.arSceneView.scene.addChild(node)

                        val piece = activePieces[augmentedImage.name]
                        if(piece != null) {
                            addPieceToWebview(piece)
                        }
                    } else {
                        node = augmentedImageMap[augmentedImage]!! as AugmentedImageNode
                    }
                    var mat:FloatArray = FloatArray(16)
                    if(node.anchor != null) {
                        node.anchor!!.pose.toMatrix(mat, 0)
                        objectTransforms += "\n${augmentedImage.name}: ${printMatrix(mat)},"
                    }

                }

                TrackingState.STOPPED -> augmentedImageMap.remove(augmentedImage)
            }
        }
        objectTransforms += "\n}"
        val str = "setScenePostParams(${objectTransforms},${cameraTransform},${projectionTransform});"
        arWebview.evaluateJavascript(str, { })
    }



}


