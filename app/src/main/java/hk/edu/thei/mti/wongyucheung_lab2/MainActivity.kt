package hk.edu.thei.mti.wongyucheung_lab2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGoToGuessGame = findViewById<Button>(R.id.btnGoToGuessGame)
        val btnGoToRps = findViewById<Button>(R.id.btnGoToRps)
        val btnGoToMeal = findViewById<Button>(R.id.btnGoToMeal)
        val btnGoToConfig = findViewById<Button>(R.id.btnGoToConfig)
        val btnGoToBingo = findViewById<Button>(R.id.btnGoToBingo)

        btnGoToGuessGame.setOnClickListener {
            startActivity(Intent(this, GuessNumberActivity::class.java))
        }

        btnGoToRps.setOnClickListener {
            startActivity(Intent(this, RPSGameActivity::class.java))
        }

        btnGoToMeal.setOnClickListener {
            startActivity(Intent(this, MealActivity::class.java))
        }

        btnGoToConfig.setOnClickListener {
            startActivity(Intent(this, ConfigurationActivity::class.java))
        }

        btnGoToBingo.setOnClickListener {
            startActivity(Intent(this, BingoGameActivity::class.java))
        }
    }
}