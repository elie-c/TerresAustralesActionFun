package net.castang.esir.progm.terresasutralesactionfun

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import net.castang.esir.progm.terresasutralesactionfun.ui.theme.TerresAsutralesActionFunTheme

class TraininingActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    //Store highScore
    private var ScoreGame1Activity = 0
    private var ScoreGame2Activity = 0
    private var ScoreGame3Activity = 0
    private var ScoreGame4Activity = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
    }

    override fun onResume() {
        auth = Firebase.auth
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        if (user != null) {
            // User is signed in
            for (i in 1..4){
                val activityName = "Game"+i.toString()+"Activity"
                readScore(activityName) { score ->
                    val viewName = "textViewScore" + activityName
                    val resId: Long = resources.getIdentifier(viewName, "id", packageName).toLong()
                    val view = findViewById<TextView>(resId.toInt())
                    view.text = resources.getString(R.string.score, score.toString())
                    view.visibility = View.VISIBLE
                }
            }

        }
        super.onResume()
    }
    fun go_game1(view: View) {
        val intent = Intent(this, Game1Activity::class.java)
        startActivityForResult(intent,1)
    }

    fun go_game2(view: View) {
        val intent = Intent(this, Game2Activity::class.java)
        startActivityForResult(intent,1)
    }

    fun go_game3(view: View) {
        val intent = Intent(this, Game3Activity::class.java)
        startActivityForResult(intent,1)
    }

    fun go_game4(view: View) {
        val intent = Intent(this, Game4Activity::class.java)
        startActivityForResult(intent,1)
    }

    fun go_game5(view: View) {
        val intent = Intent(this, Game5Activity::class.java)
        startActivityForResult(intent,1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val score = data?.getIntExtra("score", -1)
            val activityName = data?.getStringExtra("activityName")
            if (score != null && activityName != null) {
                readScore(activityName) { highScore ->
                    if (score > highScore) {
                        writeScore(score, activityName)
                    }
                }
            }
        }
    }

    private fun writeScore(score: Int,activity : String){
        val user = Firebase.auth.currentUser
        val db = Firebase.firestore
        var varName = "Score"+activity
        if (user != null) {
            // User is signed in
            user?.uid?.let { userId ->
                val user = hashMapOf(
                    "user" to userId,
                    varName to score
                )
                db.collection("users")
                    .document(userId)
                    .set(user, SetOptions.merge())
                    .addOnSuccessListener { documentReference ->
                    }
            }
        }
    }

    //Function readScore code with ChatGPT
    private fun readScore(activity: String, callback: (Int) -> Unit) {
        val auth = Firebase.auth
        val user = auth.currentUser
        val db = Firebase.firestore
        if (user != null) {
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val varName = "Score" + activity
                    if (document != null && document.data?.contains(varName) == true) {
                        val score = document.data!![varName].toString().toInt()
                        callback(score)
                    } else {
                        callback(0) // Fallback to 0 if data not found
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                    callback(0) // Fallback to 0 if an error occurs
                }
        } else {
            callback(0) // Fallback to 0 if no user is logged in
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent,)
        super.onBackPressed()
    }

}
