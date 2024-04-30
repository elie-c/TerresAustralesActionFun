package net.castang.esir.progm.terresasutralesactionfun

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity: ComponentActivity() {
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        auth = Firebase.auth
        // [END initialize_auth]
        val user = Firebase.auth.currentUser
        if (user != null){
            user?.let {
                for (profile in it.providerData) {
                    val name = profile.displayName
                    findViewById<TextView>(R.id.textViewWelcome).text = resources.getString(R.string.titile_welcome_user,name)
                }
            }
        }else{
            findViewById<TextView>(R.id.textViewWelcome).text = resources.getString(R.string.titile_welcome_guest)

        }

    }


    fun go_multi(view: View) {
        val intent = Intent(this, MultiActivity::class.java)
        startActivity(intent)
    }
    fun go_training(view: View) {
        val intent = Intent(this, TraininingActivity::class.java)
        startActivity(intent)
    }
    fun go_solo(view: View) {
        val intent = Intent(this, SoloActivity::class.java)
        startActivity(intent)
    }
    fun goToSettings(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun goToFaq(view: View) {
        val intent = Intent(this, FaqActivity::class.java)
        startActivity(intent)
    }

}