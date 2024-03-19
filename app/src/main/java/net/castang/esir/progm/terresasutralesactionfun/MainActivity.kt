package net.castang.esir.progm.terresasutralesactionfun

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity


class MainActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun go_multi(view: View) {
        Toast.makeText(this, getString(R.string.toast_comming_soon),Toast.LENGTH_LONG).show()
    }
    fun go_training(view: View) {
        val intent = Intent(this, TraininingActivity::class.java)
        startActivity(intent)
    }
    fun go_solo(view: View) {
        Toast.makeText(this, getString(R.string.toast_comming_soon),Toast.LENGTH_LONG).show()
    }
    fun goToSettings(view: View) {
        Toast.makeText(this, getString(R.string.toast_comming_soon),Toast.LENGTH_LONG).show()
    }

}