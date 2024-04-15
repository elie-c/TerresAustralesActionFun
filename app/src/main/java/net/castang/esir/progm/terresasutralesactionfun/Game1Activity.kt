package net.castang.esir.progm.terresasutralesactionfun

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.w3c.dom.Text
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random



class Game1Activity : ComponentActivity() {
    //variables
    var answer = 0
    var score = 0
    var message = R.string.dialog_wait_question //Using in function valdateDate()
    lateinit var blockingDialog: AlertDialog
    var questionAnswered = false //Used for detected timeout exceed for a question


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1)

        findViewById<TextView>(R.id.textViewScore).text = "Score : $score"

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
            message = R.string.dialog_good_answer_wait
            //Change and refresh score
            score ++
            findViewById<TextView>(R.id.textViewScore).text = "Score : $score"
        } else {
            message = R.string.dialog_bad_answer_wait
        }
        //Block user so he have to wait for timer end
        blockingDialog.setMessage(resources.getString(message));
        blockingDialog.show()
        blockingDialog.setCancelable(false)
        questionAnswered = true
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
        val numberOfQuestion = 2 //Can be change later
        val timePerQuestion : Long = 15 //Can be change later
        var i = 0
        //show first question
        showQuestion()
        //Wait timer
        val timer = Timer()
        //Function timer, called bellow
        val task = object : TimerTask() {
            override fun run() {
                i++
                if (i >= numberOfQuestion) {
                    //If there is no questions left, END GAME
                    timer.cancel()
                    //Show end of the game to user
                    blockingDialog.dismiss()
                    runOnUiThread {
                        MaterialAlertDialogBuilder(this@Game1Activity)
                            .setTitle(resources.getString(R.string.title_end_game))
                            .setMessage(resources.getString(R.string.dialog_end_question_game))
                            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                                //Go to next game
                                val intent = Intent()
                                    .putExtra("activityName",this@Game1Activity.javaClass.simpleName)
                                    .putExtra("score",score)
                                setResult(RESULT_OK, intent)
                                finish()
                            }
                            .show()
                    }
                }else{
                    //Else, if there is at least one question remaining
                    if ((questionAnswered)||(i==1)){
                        //If user answered the last question
                        blockingDialog.dismiss()
                    }else{
                        //If user hasn't answered
                        message = R.string.dialog_bad_answer_wait
                        runOnUiThread{
                            Toast.makeText(this@Game1Activity,R.string.toast_timeout,Toast.LENGTH_SHORT).show()

                        }
                        Thread.sleep(1000) //Wait for 1 s
                    }
                    questionAnswered = false; //Reinitialization before next question
                    showQuestion()
                }
            }
        }
        // Start timer
        timer.schedule(task, 0L, timePerQuestion*1000)
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        //super.onBackPressed()
    }
}