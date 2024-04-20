/*
  _______  __   __  _______  __    _  _______  _______  ______
 |       ||  | |  ||       ||  |  | ||       ||       ||    _ |
 |    ___||  |_|  ||    ___||   |_| ||    ___||    ___||   | ||
 |   |___ |       ||   |___ |       ||   |___ |   |___ |   |_||_
 |    ___||_     _||    ___||  _    ||    ___||    ___||    __  |
 |   |      |   |  |   |___ | | |   ||   |___ |   |___ |   |  | |
 |___|      |___|  |_______||_|  |__||_______||_______||___|  |_|

 Thank you for your invaluable assistance in coding this application! 🚀
 Your contributions have been instrumental in bringing this project to life.
 For more details, check out the prompt link: https://chat.openai.com/share/232e7d56-73bf-4b48-93f1-a24e60578e55

  Sincerely,
  ChatGPT 🤖
*/

package net.castang.esir.progm.terresasutralesactionfun

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.concurrent.timerTask

class Game6Activity : AppCompatActivity(), SensorEventListener {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var characterImageView: ImageView
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var characterWidth: Int = 300 // Character width in pixels
    private var characterHeight: Int = 400 // Character height in pixels
    private var lives: Int = 5 // Starting lives
    private val obstacleSpawnInterval = 800L // Interval in milliseconds
    private val obstacleSize = 200 // Obstacle size in pixels
    private val random = Random()
    private lateinit var timer: Timer
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var accelerometerValues: FloatArray? = null
    private val accelerationThreshold = 3.5f // Adjust this threshold as needed
    private var timestampStart : Long = 0
    private val obstaclesEncountered = HashSet<Int>()
    private lateinit var lifeTextView: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_game_6)

        lifeTextView = findViewById(R.id.lifeTextView)
        lifeTextView.text = "Vies: $lives"

        // Get root layout and character ImageView
        rootLayout = findViewById(R.id.rootLayout)
        characterImageView = ImageView(this)

        // Get screen dimensions
        val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        screenWidth = display.width
        screenHeight = display.height

        // Initialize sensor manager and accelerometer sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        //Show rules of the game to user
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title_game6))
            .setMessage(resources.getString(R.string.dialog_rules_game6))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Launch game when user click
                timestampStart = System.currentTimeMillis()
                dialog.dismiss()
                startGame()
            }
            .show()

    }

    override fun onResume() {
        super.onResume()
        // Register accelerometer sensor listener
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister accelerometer sensor listener
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Update accelerometer values
            accelerometerValues = event.values.clone()
            moveCharacter()
        }
    }

    private fun startGame() {
        // Set character image resource and size
        characterImageView.setImageResource(R.drawable.character)
        val layoutParams = RelativeLayout.LayoutParams(characterWidth, characterHeight)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        characterImageView.layoutParams = layoutParams
        rootLayout.addView(characterImageView)

        // Start spawning obstacles
        timer = Timer()
        timer.schedule(timerTask {
            runOnUiThread {
                spawnObstacle()
            }
        }, 0, obstacleSpawnInterval)
    }

    private fun spawnObstacle() {
        // Create a new ImageView for the obstacle
        val obstacleImageView = ImageView(this)
        // Ajouter un identifiant unique à l'obstacle (par exemple, son hashCode)
        val obstacleId = obstacleImageView.hashCode()
        // Choose obstacle image randomly
        val obstacleDrawable = if (random.nextBoolean()) R.drawable.obstacle_1 else R.drawable.obstacle_2
        obstacleImageView.setImageResource(obstacleDrawable)
        // Set obstacle size
        val layoutParams = RelativeLayout.LayoutParams(obstacleSize, obstacleSize)
        // Set random x-coordinate for obstacle
        layoutParams.leftMargin = random.nextInt(screenWidth - obstacleSize)
        obstacleImageView.layoutParams = layoutParams
        // Add obstacle to the layout
        rootLayout.addView(obstacleImageView)
        // Move obstacle downwards
        moveObstacle(obstacleImageView)
    }

    private fun moveObstacle(obstacle: ImageView) {
        // Créer un ValueAnimator pour animer la position verticale de l'obstacle
        val animator = ValueAnimator.ofFloat(obstacle.y, screenHeight.toFloat())

        // Définir la durée de l'animation (ajuster selon les besoins)
        animator.duration = 3000L

        // Ajouter un écouteur de mise à jour de l'animation
        animator.addUpdateListener { valueAnimator ->
            // Mettre à jour la position verticale de l'obstacle
            val value = valueAnimator.animatedValue as Float
            obstacle.y = value

            // Vérifier la collision à chaque mise à jour de l'animation
            if (checkCollision(obstacle)) {
            }
        }

        // Ajouter un écouteur de fin d'animation
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Supprimer l'obstacle du layout lorsque l'animation est terminée
                rootLayout.removeView(obstacle)
            }
        })

        // Démarrer l'animation
        animator.start()
    }





    private fun moveCharacter() {
        // Move character horizontally based on accelerometer values
        accelerometerValues?.let {
            val accelerationX = it[0]
            val newX = characterImageView.x - accelerationX * accelerationThreshold
            if (newX >= 0 && newX + characterWidth <= screenWidth) {
                characterImageView.x = newX
            }
        }
    }

    private fun checkCollision(obstacle: ImageView): Boolean {
        // Check collision between character and obstacle
        val characterRect = Rect(
            characterImageView.x.toInt(),
            characterImageView.y.toInt(),
            characterImageView.x.toInt() + characterWidth,
            characterImageView.y.toInt() + characterHeight
        )
        val obstacleRect = Rect(
            obstacle.x.toInt(),
            obstacle.y.toInt(),
            obstacle.x.toInt() + obstacle.width,
            obstacle.y.toInt() + obstacle.height
        )
        // Vérifier si l'obstacle a déjà été rencontré
        val collision = characterRect.intersect(obstacleRect)
        val obstacleId = obstacle.hashCode()
        val alreadyEncountered = obstaclesEncountered.contains(obstacleId)
        // Marquer l'obstacle comme rencontré s'il y a collision
        if (collision && !alreadyEncountered) {
            obstaclesEncountered.add(obstacleId)
            decrementLife()
        }
        // Retourner le résultat de la collision
        return collision

        return characterRect.intersect(obstacleRect)
    }





    private fun decrementLife() {
        lives--
        lifeTextView.text = "Vies: $lives"
        // Update UI to reflect lives remaining
        // For example, update hearts or display a message
        if (lives == 0) {
            gameEnd()
        }
    }

    private fun gameEnd() {
        val time = System.currentTimeMillis()-timestampStart
        runOnUiThread {
            MaterialAlertDialogBuilder(this@Game6Activity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.title_end_game))
                .setMessage(resources.getString(R.string.dialog_end_game6,time.toString()))
                .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                    //Go to next game
                    val intent = Intent()
                        .putExtra("activityName",this@Game6Activity.javaClass.simpleName)
                        .putExtra("score",(time/1000).toInt())
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .show()
        }
    }
}
