package net.castang.esir.progm.terresasutralesactionfun

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class Game2Activity : ComponentActivity(),
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    lateinit var imageViewFlag: ImageView
    var topOfMast = 0
    var bottomOfMast = 0
    var timestampStart : Long = 0
    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2)



        //Place flag at the bootom of the mast
        val imageViewMast = findViewById<ImageView>(R.id.imageViewMast)
        imageViewMast.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = imageViewMast.width
                val height = imageViewMast.height

                //Calculate position of mast
                topOfMast = (height*(8.6/10.5)-height).toInt()
                bottomOfMast = (height*(11.9/10.5)-height).toInt()
                val x = width* (3.2/6).toFloat()
                val y = (height*(11.9/10.5)-height).toFloat()

                //draw flag
                val layout = findViewById<FrameLayout>(R.id.layoutGame2)
                imageViewFlag = ImageView(this@Game2Activity)
                imageViewFlag.x = x
                imageViewFlag.y = y
                imageViewFlag.setImageResource(R.drawable.flag)
                layout.addView(imageViewFlag)
                val params = imageViewFlag.layoutParams
                params.width= ((0.08*width).toInt())
                Log.d("POSITION", "y de imageViewFlag : $y, x de imageViewMast : $x")

                imageViewMast.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        // Initialize Firebase Auth and change flag if needed
        auth = Firebase.auth
        db = Firebase.firestore
        val user = Firebase.auth.currentUser
        if (user != null) {
            // User is signed in
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val varName = "Flag"
                    if (document != null && document.data?.contains(varName) == true) {
                        val flagName = document.data!![varName].toString()
                        val resId = resources.getIdentifier(flagName, "drawable", packageName)
                        val res = resources.getDrawable(resId)
                        imageViewFlag.setImageDrawable(res)
                    }
                }
        }

        //Show rules of the game to user
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title_game2))
            .setMessage(resources.getString(R.string.dialog_rules_game2))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Launch game when user click
                dialog.dismiss()
                gameProcess()
            }
            .show()
    }

    private fun gameProcess() {
        mDetector = GestureDetectorCompat(this, this)
        mDetector.setOnDoubleTapListener(this)
        timestampStart = System.currentTimeMillis()
    }


    private fun gameEnd() {
        val time = System.currentTimeMillis()-timestampStart
        runOnUiThread {
            MaterialAlertDialogBuilder(this@Game2Activity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.title_end_game))
                .setMessage(resources.getString(R.string.dialog_end_game2,time.toString()))
                .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                    //Go to next game
                    val intent = Intent()
                        .putExtra("activityName",this@Game2Activity.javaClass.simpleName)
                        .putExtra("score",(100000/time).toInt())
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .show()
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    override fun onDown(event: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        event1: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(event: MotionEvent) {
    }

    override fun onScroll(
        e1: MotionEvent?,
        event1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return true
    }

    override fun onShowPress(event: MotionEvent) {
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        runOnUiThread(){
            imageViewFlag.y += ((topOfMast-bottomOfMast)*0.05).toFloat()
            if (imageViewFlag.y < topOfMast){
                //Stop game because flag is at the top
                gameEnd()
            }
        }
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        return true
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        timestampStart -= 10000 //To prevent cheating
        gameEnd()
    }

}
