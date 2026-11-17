package hk.edu.thei.mti.wongyucheung_lab2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import kotlin.random.Random

class RPSGameActivity : ComponentActivity() {

    private var playerScore = 0
    private var cpuScore = 0
    private var targetScore = 3
    private var playerName = "Player"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rps_game)

        val tvScoreBoard = findViewById<TextView>(R.id.tvScoreBoard)
        val tvGamePrompt = findViewById<TextView>(R.id.tvGamePrompt)
        val btnRock = findViewById<Button>(R.id.btnRock)
        val btnPaper = findViewById<Button>(R.id.btnPaper)
        val btnScissors = findViewById<Button>(R.id.btnScissors)
        val btnReset = findViewById<Button>(R.id.btnResetRps)
        val btnBack = findViewById<Button>(R.id.btnBackToHub)

        // Load configuration from SharedPreferences
        val prefs: SharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        playerName = prefs.getString("player_name", "Player") ?: "Player"
        targetScore = prefs.getInt("target_score", 3)

        updateScoreBoard(tvScoreBoard)

        fun playRound(playerChoice: String) {
            if (playerScore >= targetScore || cpuScore >= targetScore) return

            val choices = listOf("Rock", "Paper", "Scissors")
            val cpuChoice = choices[Random.nextInt(choices.size)]

            if (playerChoice == cpuChoice) {
                tvGamePrompt.text = "CPU picked $cpuChoice.\nIt's a tie!"
            } else if (
                (playerChoice == "Rock" && cpuChoice == "Scissors") ||
                (playerChoice == "Paper" && cpuChoice == "Rock") ||
                (playerChoice == "Scissors" && cpuChoice == "Paper")
            ) {
                playerScore++
                tvGamePrompt.text = "CPU picked $cpuChoice.\n$playerChoice beats $cpuChoice! Point to you!"
            } else {
                cpuScore++
                tvGamePrompt.text = "CPU picked $cpuChoice.\n$cpuChoice beats $playerChoice! Point to CPU!"
            }

            updateScoreBoard(tvScoreBoard)

            if (playerScore >= targetScore) {
                tvGamePrompt.text = "Match Won! $playerName reached $targetScore points!"
            } else if (cpuScore >= targetScore) {
                tvGamePrompt.text = "Game Over! CPU reached $targetScore points."
            }
        }

        btnRock.setOnClickListener { playRound("Rock") }
        btnPaper.setOnClickListener { playRound("Paper") }
        btnScissors.setOnClickListener { playRound("Scissors") }

        btnReset.setOnClickListener {
            playerScore = 0
            cpuScore = 0
            updateScoreBoard(tvScoreBoard)
            tvGamePrompt.text = "Match reset. Choose your move:"
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun updateScoreBoard(tvScoreBoard: TextView) {
        tvScoreBoard.text = "$playerName: $playerScore | CPU: $cpuScore (First to $targetScore)"
    }
}