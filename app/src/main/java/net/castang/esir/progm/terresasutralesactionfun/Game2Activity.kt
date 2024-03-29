package net.castang.esir.progm.terresasutralesactionfun

import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginLeft
import kotlin.properties.Delegates


class Game2Activity : ComponentActivity() {
    lateinit var imageViewFlag: ImageView
    //private var x = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2)

        //Place flag at the bootom of the mast
        val imageViewMast = findViewById<ImageView>(R.id.imageViewMast)
        //val y = imageViewMast.drawable.intrinsicHeight * (4/10.5);
        //val x = imageViewMast.drawable.intrinsicWidth * (3.4/6);
        imageViewMast.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // C'est appelé lorsque les dimensions de l'imageViewMast changent
                val width = imageViewMast.width
                val height = imageViewMast.height

                // Faites quelque chose avec les nouvelles dimensions
                Log.d("POSITION", "Nouvelle taille - Largeur: $width, Hauteur: $height")

                val layout = findViewById<FrameLayout>(R.id.layoutGame2)
                imageViewFlag = ImageView(this@Game2Activity)

                val x = width* (3.4/6).toFloat()
                val y = (height*(11.6/10.5)-height).toFloat()
                imageViewFlag.x = x
                imageViewFlag.y = y
                imageViewFlag.setImageResource(R.drawable.flag)
                layout.addView(imageViewFlag)
                val params = imageViewFlag.layoutParams
                params.width=200
                Log.d("POSITION", "y de imageViewFlag : $y, x de imageViewMast : $x")

                // Assurez-vous de retirer le listener après utilisation pour éviter les fuites de mémoire
                imageViewMast.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        val x = imageViewMast.width
        val y =  imageViewMast.width






        //imageViewFlag = findViewById<ImageView>(R.id.imageViewFlag)
    }

}
