
package net.castang.esir.progm.terresasutralesactionfun

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import java.util.Random



class Game5Activity : AppCompatActivity() {

    private lateinit var layout: RelativeLayout
    private lateinit var selectedToolImageView: ImageView
    private var selectedTool: Int = R.drawable.bross // Par défaut, la brosse est sélectionnée
    private var dX: Float = 0f
    private var dY: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            generateDirt()
        }
    }

    private fun generateDirt() {
        val numDirt = 30
        val random = Random()

        for (i in 0 until numDirt) {
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            val x = it.rawX.toInt()
            val y = it.rawY.toInt()

            if (it.action == MotionEvent.ACTION_DOWN) {
                for (i in 0 until layout.childCount) {
                    val childView = layout.getChildAt(i)
                    Log.d("DEV",childView.toString())

                    if (childView is ImageView && childView.drawable != null) {
                        if (isViewOverlapping(x, y, childView, selectedToolImageView)) {
                            val childDrawable = childView.drawable
                            Log.d("DEV",childView.toString())
                            if ((selectedTool == R.drawable.bross && childDrawable.constantState == resources.getDrawable(R.drawable.stain)?.constantState) ||
                                (selectedTool == R.drawable.vacuum && childDrawable.constantState == resources.getDrawable(R.drawable.seeds)?.constantState)) {
                                layout.removeView(childView)
                            }
                            break
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }


    private fun isViewOverlapping(x: Int, y: Int, view1: View, view2: View): Boolean {
        val rect1 = Rect()
        view1.getGlobalVisibleRect(rect1)
        val rect2 = Rect()
        view2.getGlobalVisibleRect(rect2)
        return rect1.intersect(rect2)
    }

    private fun handleToolTouch(view: View, event: MotionEvent) {
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
            }
            MotionEvent.ACTION_UP -> {
                // Action lorsque l'utilisateur arrête de toucher l'outil
                // Vérifier s'il y a collision avec une saleté et supprimer si nécessaire
                for (i in 0 until layout.childCount) {
                    val childView = layout.getChildAt(i)
                    if (childView is ImageView && childView.drawable != null) {
                        if (isViewOverlapping(view.x.toInt(), view.y.toInt(), childView, selectedToolImageView)) {
                            val childDrawable = childView.drawable
                            if ((selectedTool == R.drawable.bross && childDrawable.constantState == resources.getDrawable(R.drawable.stain)?.constantState) ||
                                (selectedTool == R.drawable.vacuum && childDrawable.constantState == resources.getDrawable(R.drawable.seeds)?.constantState)) {
                                layout.removeView(childView)
                            }
                            break
                        }
                    }
                }
            }
        }
    }

}

