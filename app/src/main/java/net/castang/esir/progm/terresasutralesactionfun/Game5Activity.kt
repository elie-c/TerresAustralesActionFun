/*
  _______  _______ .______       __  ___      _______.
 /  _____||   ____||   _  \     |  |/  /     /       |
|  |  __  |  |__   |  |_)  |    |  '  /     |   (----`
|  | |_ | |   __|  |      /     |    <       \   \
|  |__| | |  |____ |  |\  \----.|  .  \  .----)   |
 \______| |_______|| _| `._____||__|\__\ |_______/

// This Java program was created with the assistance of ChatGPT.
// Stay tuned for the prompt link :
// Note : le lien du prompt n'a pas pu être mis car il est bloqué par la modération : This shared link has been disabled by moderation.
*/

package net.castang.esir.progm.terresasutralesactionfun

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Random

class Game5Activity : AppCompatActivity() {
    private lateinit var layout: RelativeLayout
    private lateinit var selectedToolImageView: ImageView
    private var selectedTool: Int = R.drawable.bross // By defualt, use bross
    private var timestampStart : Long = 0
    private var dX: Float = 0f
    private var dY: Float = 0f
    private var numDirts = 20 //Can be change later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_game5)

        layout = findViewById(R.id.layout)
        selectedToolImageView = findViewById(R.id.selectedToolImageView)

        val aspirateurButton: Button = findViewById(R.id.aspirateurButton)
        val brosseButton: Button = findViewById(R.id.brosseButton)

        aspirateurButton.setOnClickListener {
            selectedTool = R.drawable.vacuum
            selectedToolImageView.setImageResource(selectedTool)

        }
        brosseButton.setOnClickListener {
            selectedTool = R.drawable.bross
            selectedToolImageView.setImageResource(selectedTool)
        }
        selectedToolImageView.setOnTouchListener { view, event ->
            handleToolTouch(view, event)
            true
        }

        //Show rules of the game to user
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title_game5))
            .setMessage(resources.getString(R.string.dialog_rules_game5))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Launch game when user click
                timestampStart = System.currentTimeMillis()
                dialog.dismiss()
                generateDirt()
            }
            .show()
    }

    /*override fun onWindowFocusChanged(hasFocus: Boolean) {
        //Just after onCreate(), needed to be sure screen is OK
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            generateDirt()
        }
    }
     */

    private fun generateDirt() {
        //Generate a random quantity of each type of dirt
        val random = Random()
        for (i in 0 until numDirts) {
            val dirtImageView = ImageView(this)
            dirtImageView.setImageResource(if (random.nextBoolean()) R.drawable.seeds else R.drawable.stain)
            val layoutParams = RelativeLayout.LayoutParams(
                dpToPx(24),
                dpToPx(24)
            )
            layoutParams.leftMargin = random.nextInt(layout.width)
            layoutParams.topMargin = random.nextInt(layout.height)
            dirtImageView.layoutParams = layoutParams
            layout.addView(dirtImageView)
        }
    }
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun isViewOverlapping(x: Int, y: Int, view1: View, view2: View): Boolean {
        //Used to detect the tool tocuh the dirt
        val rect1 = Rect()
        view1.getGlobalVisibleRect(rect1)
        val rect2 = Rect()
        view2.getGlobalVisibleRect(rect2)
        return rect1.intersect(rect2)
    }

    private fun handleToolTouch(view: View, event: MotionEvent) {
        //The main game function
        onTouchEvent(event)
        val layoutParams = view.layoutParams as RelativeLayout.LayoutParams
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Action lorsque l'utilisateur commence à toucher l'outil
                dX = view.x - event.rawX
                dY = view.y - event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                // Action lorsque l'utilisateur déplace l'outil
                layoutParams.leftMargin = (event.rawX + dX).toInt()
                layoutParams.topMargin = (event.rawY + dY).toInt()
                view.layoutParams = layoutParams

                // Vérifier s'il y a collision avec une saleté et supprimer si nécessaire
                for (i in 0 until layout.childCount) {
                    val childView = layout.getChildAt(i)
                    if (!childView.toString().contains("selectedTool")){
                        Log.d("DEV",childView.toString())
                        if (childView is ImageView && childView.drawable != null) {
                            if (isViewOverlapping(view.x.toInt(), view.y.toInt(), childView, selectedToolImageView)) {
                                val childDrawable = childView.drawable
                                if ((selectedTool == R.drawable.bross && childDrawable.constantState == resources.getDrawable(R.drawable.stain)?.constantState) ||
                                    (selectedTool == R.drawable.vacuum && childDrawable.constantState == resources.getDrawable(R.drawable.seeds)?.constantState)) {
                                    layout.removeView(childView)
                                    numDirts --
                                    if (numDirts < 1){
                                        gameEnd()
                                    }
                                }
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    private fun gameEnd() {
        val time = System.currentTimeMillis()-timestampStart
        runOnUiThread {
            MaterialAlertDialogBuilder(this@Game5Activity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.title_end_game))
                .setMessage(resources.getString(R.string.dialog_end_game5,time.toString()))
                .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                    //Go to next game
                    val intent = Intent()
                        .putExtra("activityName",this@Game5Activity.javaClass.simpleName)
                        .putExtra("score",(100000/time).toInt())
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .show()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        timestampStart -= 10000 //To prevent cheating
        gameEnd()
    }

}

