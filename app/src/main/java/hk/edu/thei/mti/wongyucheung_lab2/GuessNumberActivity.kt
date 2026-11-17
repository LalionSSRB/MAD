package hk.edu.thei.mti.wongyucheung_lab2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.random.Random

class GuessNumberActivity : ComponentActivity() {

    private var targetNumber: Int = 0
    private var minRange: Int = 0
    private var maxRange: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess_number)

        val tvPrompt = findViewById<TextView>(R.id.tvPrompt)
        val etInputNumber = findViewById<EditText>(R.id.etInputNumber)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val btnBackToMenu = findViewById<Button>(R.id.btnBackToMenu)

        startNewGame()
        updatePromptWithRange(tvPrompt, "Guess a number:")

        btnSubmit.setOnClickListener {
            val inputText = etInputNumber.text.toString().trim()
            checkPlayerInput(inputText, tvPrompt)
            etInputNumber.text.clear()
        }

        btnBackToMenu.setOnClickListener {
            finish()
        }
    }

    private fun checkPlayerInput(inputText: String, tvPrompt: TextView) {
        val userGuess = inputText.toIntOrNull()

        if (userGuess == null) {
            updatePromptWithRange(tvPrompt, "Your data is incorrect. Enter again!")
            return
        }

        if (userGuess < minRange || userGuess > maxRange) {
            updatePromptWithRange(tvPrompt, "Out of bounds! Guess within $minRange - $maxRange:")
            return
        }

        if (userGuess == targetNumber) {
            tvPrompt.text = "You win! The number was $targetNumber."
        } else if (userGuess > targetNumber) {
            maxRange = userGuess - 1
            updatePromptWithRange(tvPrompt, "You are wrong! Too high.")
        } else {
            minRange = userGuess + 1
            updatePromptWithRange(tvPrompt, "You are wrong! Too low.")
        }
    }

    private fun updatePromptWithRange(tvPrompt: TextView, message: String) {
        tvPrompt.text = "$message\nRange: ($minRange - $maxRange)"
    }

    private fun startNewGame() {
        minRange = 0
        maxRange = 100
        targetNumber = Random.nextInt(minRange, maxRange + 1)
    }
}