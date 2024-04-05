package net.castang.esir.progm.terresasutralesactionfun

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import net.castang.esir.progm.terresasutralesactionfun.ui.theme.TerresAsutralesActionFunTheme

class Game3Activity : ComponentActivity(),SensorEventListener {
    private var light : Float = 1000F
    private var mLight: Sensor? = null
    private lateinit var sensorManager: SensorManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mLight= sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        setContentView(R.layout.activity_game3)

        //Show rules of the game to user
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title_game3))
            .setMessage(resources.getString(R.string.dialog_rules_game3))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Launch game when user click
                dialog.dismiss()
                gameProcess()
            }
            .show()
    }

    private fun gameProcess() {
        Thread.sleep(3000)  //Wait 3 s
        // Viibrate
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(1000)
        // Get light value
        mLight?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun gameEnd(lightFinal : Float) {
        sensorManager.unregisterListener(this)
        runOnUiThread {
            MaterialAlertDialogBuilder(this@Game3Activity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.title_end_game))
                .setMessage(resources.getString(R.string.dialog_end_game3,lightFinal.toString()))
                .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                    //Go to next game
                    setResult(RESULT_OK, Intent().putExtra("score", ((1000-lightFinal)/100).toInt()))
                    finish()
                }
                .show()
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type==Sensor.TYPE_LIGHT){
                light = event.values[0]
                gameEnd(light)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //Do nothing but do it good
    }
}
