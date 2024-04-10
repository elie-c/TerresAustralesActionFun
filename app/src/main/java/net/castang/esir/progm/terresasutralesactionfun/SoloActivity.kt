package net.castang.esir.progm.terresasutralesactionfun

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Random

class SoloActivity : ComponentActivity() {
    val numberOfGames = 3
    val scoresList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solo)
        //Pickup 3 games
        val activities = listOf(
            Game1Activity::class.java,
            Game2Activity::class.java,
            Game3Activity::class.java,
            Game4Activity::class.java
        )
        val activitiesToLaunch : MutableList<Class<out ComponentActivity>> = mutableListOf()

        for (i in 1..numberOfGames){
            var activity : Class<out ComponentActivity>
            do {
                val index = Random().nextInt(activities.size)
                activity = activities[index]
            }while (activitiesToLaunch.contains(activity)) //To have only one time each activity
            activitiesToLaunch.add(activity)
        }
        for (i in 1..numberOfGames){
            val intent = Intent(this, activitiesToLaunch.get(i-1))
            startActivityForResult(intent,i)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val score = data?.getIntExtra("score", 0)
            if (score != null) {
                scoresList.add(score)
            }
        }
        if (scoresList.size == numberOfGames) {
            //If all the results are here
            endsSoloMode()
        }
    }

    private fun endsSoloMode() {
        //End of the games
        val scoreTotal = scoresList.sum()
        runOnUiThread(){
            MaterialAlertDialogBuilder(this@SoloActivity)
                .setCancelable(false)
                .setTitle(resources.getString(R.string.title_end_game))
                .setMessage(resources.getString(R.string.dialog_end_solo,scoreTotal.toString()))
                .setNeutralButton(resources.getString(R.string.button_seeyou)) { dialog, which ->
                    //Go to menu
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent,)
                }
                .show()
        }
        val mediaPlayer = MediaPlayer.create(this, R.raw.music_end_solo)
        mediaPlayer.start()



    }


}

