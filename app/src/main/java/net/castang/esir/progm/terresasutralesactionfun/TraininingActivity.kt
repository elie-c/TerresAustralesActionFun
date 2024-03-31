package net.castang.esir.progm.terresasutralesactionfun

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import net.castang.esir.progm.terresasutralesactionfun.ui.theme.TerresAsutralesActionFunTheme

class TraininingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)
    }

    fun go_game1(view: View) {
        val intent = Intent(this, Game1Activity::class.java)
        startActivity(intent)
    }

    fun go_game2(view: View) {
        val intent = Intent(this, Game2Activity::class.java)
        startActivity(intent)
    }

    fun go_game3(view: View) {}


}