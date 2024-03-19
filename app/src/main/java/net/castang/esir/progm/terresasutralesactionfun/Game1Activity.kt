package net.castang.esir.progm.terresasutralesactionfun

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random


class Game1Activity : ComponentActivity() {
    //variables
    var answer = 0
    lateinit var blockingDialog: AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1)

        //Create blocking dialog for later
        blockingDialog =
            MaterialAlertDialogBuilder(this).setTitle(resources.getString(R.string.title_wait))
                .setMessage(resources.getString(R.string.dialog_wait_question)).create();

        //Show rules of the game to user
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title_game1))
            .setMessage(resources.getString(R.string.dialog_rules_game1))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Launch game when user click
                dialog.dismiss()
                gameProcess()
            }
            .show()

        //Configure date picker
        val number_picker1 = findViewById<NumberPicker>(R.id.number_picker1);
        number_picker1.minValue = 0
        number_picker1.maxValue = 2
        val number_picker2 = findViewById<NumberPicker>(R.id.number_picker2);
        number_picker2.minValue = 0
        number_picker2.maxValue = 9
        val number_picker3 = findViewById<NumberPicker>(R.id.number_picker3);
        number_picker3.minValue = 0
        number_picker3.maxValue = 9
        val number_picker4 = findViewById<NumberPicker>(R.id.number_picker4);
        number_picker4.minValue = 0
        number_picker4.maxValue = 9
    }

    fun validate_date(view: View) {
        val number_1 = findViewById<NumberPicker>(R.id.number_picker1).value;
        val number_2 = findViewById<NumberPicker>(R.id.number_picker2).value;
        val number_3 = findViewById<NumberPicker>(R.id.number_picker3).value;
        val number_4 = findViewById<NumberPicker>(R.id.number_picker4).value;
        val year =
            (number_1.toString() + number_2.toString() + number_3.toString() + number_4.toString()).toInt()
        if (year == answer) {
            Toast.makeText(this, "Bravo", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "T nul", Toast.LENGTH_LONG).show()
        }
        //Block user so he have to wait for timer end
        blockingDialog.show()
        blockingDialog.setCancelable(false)
    }

    fun showQuestion() {
        runOnUiThread {
            //Getting questions
            val file = this.resources.openRawResource(R.raw.game1_questions)
            val inputStream = this.resources.openRawResource(R.raw.game1_questions)
            val buffer = ByteArray(inputStream.available())
            inputStream.read(buffer)
            inputStream.close()
            val jsonString = String(buffer)
            val jsonArray = JSONArray(jsonString)

            //Choose one question and print it
            val randomIndex = Random.nextInt(jsonArray.length())
            val question = jsonArray.getJSONObject(randomIndex).getString("question").toString()
            val textView = findViewById<TextView>(R.id.textViewQuestion)
            textView.text = question

            //Change answer var
            answer = jsonArray.getJSONObject(randomIndex).getString("answer").toInt()
        }

    }


    fun gameProcess() {
        val numberOfQuestion = 5 //Can be change later
        val timePerQuestion = 10 //Can be change later
        val score = 0
        var i = 0
        //show first question
        showQuestion()
        //Wait timer
        val timer = Timer()
        val task = object : TimerTask() {
            override fun run() {
                i++
                println("Exécution de la fonction fct - Compteur: $i")
                if (i >= numberOfQuestion) {
                    timer.cancel()
                    println("Arrêt du minuteur")
                }
                // Appeler la fonction fct ici
                blockingDialog.dismiss()
                showQuestion()
            }
        }

        // Démarrer le minuteur avec une période de 10 secondes
        timer.schedule(task, 0L, 10000L)

    }
}
    /*
        //for (i in 1..numberOfQuestion){


            //show first question
            showQuestion()
            //Wait timer
            var handler = Handler()
            val r = Runnable {
                //what ever you do here will be done after 3 seconds delay.
                //If timer exceed go to next question
                blockingDialog.dismiss()
                Log.d("INDICE",i.toString())
            }
            if (i++ < 5) {
                handler.postDelayed(r, 5000);
            }
            //If timer exceed go to next question
            //blockingDialog.dismiss()
        //}
        //At the end of the loop show message


    }
}
*/