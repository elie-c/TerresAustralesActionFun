package net.castang.esir.progm.terresasutralesactionfun

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.userProfileChangeRequest

class LoginActivity : Activity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        val user = Firebase.auth.currentUser
        if (user != null) {
            // User is signed in
            findViewById<ConstraintLayout>(R.id.constraintLayoutForSettings).visibility = View.VISIBLE
            updateUI(user)
        } else {
            // No user is signed in
            findViewById<ConstraintLayout>(R.id.constraintLayoutForLogin).visibility = View.VISIBLE
        }
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }
    // [END on_start_check_user]

    private fun createAccount(email: CharSequence, password: CharSequence) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email.toString(), password.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
        // [END create_user_with_email]
    }

    private fun signIn(email: CharSequence, password: CharSequence) {
        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email.toString(), password.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
        // [END sign_in_with_email]
    }

    private fun sendEmailVerification() {
        // [START send_email_verification]
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                // Email Verification sent
            }
        // [END send_email_verification]
    }

    private fun updateUI(user: FirebaseUser?) {
        findViewById<ConstraintLayout>(R.id.constraintLayoutForLogin).visibility = View.GONE
        findViewById<ConstraintLayout>(R.id.constraintLayoutForSettings).visibility = View.VISIBLE
        user?.let {
            // Name, email address, and profile photo Url
            val name = it.displayName
            val email = it.email

            findViewById<EditText>(R.id.editTextName).setText(name)
            findViewById<TextView>(R.id.textViewEmail).text = email
        }

    }

    private fun reload() {
    }

    companion object {
        private const val TAG = "EmailPassword"
    }

    fun signInButton(view: View) {
        val email = findViewById<TextView>(R.id.editTextLogin).text
        val password = findViewById<TextView>(R.id.editTextTextPassword).text
        signIn(email,password)
    }

    fun signOutButton(view: View) {
        val email = findViewById<TextView>(R.id.editTextLogin).text.toString()
        val password = findViewById<TextView>(R.id.editTextTextPassword).text
        Log.d("DEV-email",email.toString())
        Log.d("DEV-pass",password.toString())

        createAccount(email,password)
    }

    fun validateSettings(view: View) {
        val user = Firebase.auth.currentUser
        val profileUpdates = userProfileChangeRequest {
            displayName = findViewById<EditText>(R.id.editTextName).text.toString()
        }
        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,R.string.toast_settings_saved,Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    Toast.makeText(this,R.string.toast_settings_error,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun logOut(view: View) {
        auth.signOut()
        finish()
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent,)
        super.onBackPressed()
    }
}