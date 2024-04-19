package net.castang.esir.progm.terresasutralesactionfun

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.RelativeLayout
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
    private var lives: Int = 2 // Starting lives
    private val obstacleSpawnInterval = 800L // Interval in milliseconds
    private val obstacleSize = 200 // Obstacle size in pixels
    private val random = Random()
    private lateinit var timer: Timer
    private lateinit var sensorManager: SensorManager
    private var accelerometerSensor: Sensor? = null
    private var accelerometerValues: FloatArray? = null
    private val accelerationThreshold = 3.5f // Adjust this threshold as needed
    private var timestampStart : Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_6)

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
        val moveAnimation = TranslateAnimation(0f, 0f, 0f, screenHeight.toFloat())
        moveAnimation.duration = 3000 // Adjust duration as needed
        moveAnimation.fillAfter = true
        obstacle.startAnimation(moveAnimation)
        moveAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Remove obstacle from layout when animation ends
                rootLayout.removeView(obstacle)
                // Check for collision after obstacle animation ends
                if (checkCollision(obstacle)){
                    decrementLife()
                }
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
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
        // Get bounds of character and obstacle on the horizontal axis
        val characterLeft = characterImageView.x
        val characterRight = characterLeft + characterWidth
        val obstacleLeft = obstacle.x
        val obstacleRight = obstacleLeft + obstacle.width

        // Check for collision on the horizontal axis
        return characterRight >= obstacleLeft && characterLeft <= obstacleRight
    }




    private fun decrementLife() {
        lives--
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
                        .putExtra("score",(time/100000).toInt())
                    setResult(RESULT_OK, intent)
                    finish()
                }
                .show()
        }
    }
}
