package net.castang.esir.progm.terresasutralesactionfun

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.widget.ConstraintLayout
import net.castang.esir.progm.terresasutralesactionfun.ui.theme.TerresAsutralesActionFunTheme
import java.util.Random
import android.os.Looper
import android.util.Log
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Timer
import java.util.TimerTask

// ________________________________________________________
// /                                                        \
// |      ____        _           _                         |
// |     |  _ \ _   _| |__   __ _| |_ ___ _ __              |
// |     | | | | | | | '_ \ / _` | __/ _ \ '__|             |
// |     | |_| | |_| | |_) | (_| | ||  __/ |                |
// |     |____/ \__, |_.__/ \__,_|\__\___|_|                |
// |            |___/                                       |
// |                                                        |
// |    Congratulations! This game was coded with the help   |
// |    of ChatGPT. Thanks to ChatGPT for assisting in the  |
// |    development of this game.                           |
// |                                                        |
// |    Time elapsed since first prompt: [TimeElapsed]      |
// |                                                        |
// \________________________________________________________/
//      \
//       \
//           _,
//         .'.'`.
//        `      `
// The prompts can be found there : https://chat.openai.com/share/6c56819f-4fe3-4d3d-b3f6-58ce3bfa1bf9

class Game4Activity : ComponentActivity() {

    private lateinit var handler: Handler
    private val random = Random()
    private var score = 0
    private var gameTime = 30
    private var gameRunning = false
    private val characterTimer = Timer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game4)
        handler = Handler(Looper.getMainLooper())
        //Show rules of the game to user
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title_game4))
            .setMessage(resources.getString(R.string.dialog_rules_game4))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Launch game when user click
                dialog.dismiss()
                startGame()
            }
            .show()
    }

    private fun startGame() {
        gameRunning = true
        score = 0
        gameTime = 30
        updateScoreView()

        handler.postDelayed({
            endGame()
        }, 30000) // 30 secondes


        characterTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (gameRunning) {
                    runOnUiThread {
                        generateCharacter()
                    }
                } else {
                    characterTimer?.cancel()
                }
            }
        }, 0, 1000)

    }

    private fun generateCharacter() {
        if (gameRunning) {
            val character = ImageView(this)
            val isCat = random.nextBoolean()
            character.setImageResource(if (isCat) R.drawable.cat else R.drawable.petrel)

            // Réduire la taille des personnages
            character.layoutParams = ViewGroup.LayoutParams(100, 100) // Ajustez la taille selon vos besoins

            //val layoutParams = character.layoutParams as ConstraintLayout.LayoutParams
            val size = 100
            val layoutParams = ConstraintLayout.LayoutParams(size, size)

            // Définir la position initiale aléatoire
            layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.topMargin = random.nextInt(screenHeight - 100) // Pour éviter que les personnages sortent de l'écran
            layoutParams.marginStart = random.nextInt(screenWidth - 100)

            // Ajouter le personnage à la vue
            val constraintLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)
            constraintLayout.addView(character)

            character.setOnClickListener {
                if (isCat) {
                    score++
                } else {
                    score -= 3
                }
                updateScoreView()
                constraintLayout.removeView(character) // Supprimer le personnage lorsque cliqué
                generateCharacter()
            }

            // Déplacer le personnage de manière aléatoire à intervalles réguliers
            val moveRunnable = object : Runnable {
                override fun run() {
                    val deltaX = random.nextInt(201) - 100 // Déplacement horizontal aléatoire entre -100 et 100 pixels
                    val deltaY = random.nextInt(201) - 100 // Déplacement vertical aléatoire entre -100 et 100 pixels

                    layoutParams.topMargin += deltaY
                    layoutParams.marginStart += deltaX

                    // Vérifier si le personnage est toujours visible à l'écran
                    if (layoutParams.topMargin < 0) {
                        layoutParams.topMargin = 0
                    } else if (layoutParams.topMargin > screenHeight - 100) {
                        layoutParams.topMargin = screenHeight - 100
                    }
                    if (layoutParams.marginStart < 0) {
                        layoutParams.marginStart = 0
                    } else if (layoutParams.marginStart > screenWidth - 100) {
                        layoutParams.marginStart = screenWidth - 100
                    }

                    // Appliquer les nouvelles marges pour déplacer le personnage
                    character.layoutParams = layoutParams

                    // Planifier le prochain déplacement après un court délai
                    handler.postDelayed(this, 200) // Déplacer toutes les 200 millisecondes
                }
            }

            // Commencer le déplacement initial
            handler.post(moveRunnable)
        }
    }



    private fun updateScoreView() {
        val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
        scoreTextView.text = "Score: $score"

    }

    private fun endGame() {
        gameRunning = false
        characterTimer?.cancel() // Arrêter la génération de personnages

        // Nettoyer l'interface utilisateur
        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)
        constraintLayout.removeAllViews() // Supprimer tous les personnages restants de l'écran

        // Afficher un message de fin de jeu avec le score
        val message = "Game Over!\nYour score: $score"
        MaterialAlertDialogBuilder(this@Game4Activity)
            .setCancelable(false)
            .setTitle(resources.getString(R.string.title_end_game))
            .setMessage(resources.getString(R.string.dialog_end_game4,score.toString()))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Go to next game
                val intent = Intent()
                    .putExtra("activityName",this@Game4Activity.javaClass.simpleName)
                    .putExtra("score",score)
                setResult(RESULT_OK, intent)
                finish()
            }
            .show()

    }


    private val screenWidth: Int
        get() {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.widthPixels
        }

    private val screenHeight: Int
        get() {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            return displayMetrics.heightPixels
        }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        endGame()
        //super.onBackPressed()
    }
}

