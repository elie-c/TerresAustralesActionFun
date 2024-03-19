package net.castang.esir.progm.terresasutralesactionfun

import android.os.Bundle
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import kotlin.random.Random


class Game1Activity : ComponentActivity() {
    var answer = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game1)

        //Show rules of the game to user
        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.title_game1))
            .setMessage(resources.getString(R.string.dialog_rules_game1))
            .setNeutralButton(resources.getString(R.string.button_go)) { dialog, which ->
                //Show first question
                showQuestion()
            }
            .show()

        //Configure date picker
        val number_picker1 = findViewById<NumberPicker>(R.id.number_picker1);
        number_picker1.minValue=0
        number_picker1.maxValue=2;
        val number_picker2 = findViewById<NumberPicker>(R.id.number_picker2);
        number_picker2.minValue=0
        number_picker2.maxValue=9;
        val number_picker3 = findViewById<NumberPicker>(R.id.number_picker3);
        number_picker3.minValue=0
        number_picker3.maxValue=9;
        val number_picker4 = findViewById<NumberPicker>(R.id.number_picker4);
        number_picker4.minValue=0
        number_picker4.maxValue=9;


    }

    fun validate_date(view: View) {
        val number_1 = findViewById<NumberPicker>(R.id.number_picker1).value;
        val number_2 = findViewById<NumberPicker>(R.id.number_picker2).value;
        val number_3 = findViewById<NumberPicker>(R.id.number_picker3).value;
        val number_4 = findViewById<NumberPicker>(R.id.number_picker4).value;
        val year = (number_1.toString()+number_2.toString()+number_3.toString()+number_4.toString()).toInt()
        if (year == answer){
            Toast.makeText(this,"Bravo",Toast.LENGTH_LONG).show()
        }else{
            Toast.makeText(this,"T nul",Toast.LENGTH_LONG).show()
        }
        showQuestion();
    }

    fun showQuestion(){
        //Getting questions
        val file = this.resources.openRawResource(R.raw.game1_questions)
        val inputStream = this.resources.openRawResource(R.raw.game1_questions)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        val jsonString = String(buffer)
        val jsonArray = JSONArray(jsonString)
        //Log.d("JSON", jsonArray.getJSONObject(1).getString("question").toString())

        //Choose one question and printIt
        val randomIndex = Random.nextInt(jsonArray.length())
        val question = jsonArray.getJSONObject(randomIndex).getString("question").toString()
        val textView = findViewById<TextView>(R.id.textViewQuestion)
        textView.text=question

        //chnage answer var
        answer = jsonArray.getJSONObject(randomIndex).getString("answer").toInt()
    }
}
