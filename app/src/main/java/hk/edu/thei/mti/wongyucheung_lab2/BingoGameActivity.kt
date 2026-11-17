package hk.edu.thei.mti.wongyucheung_lab2

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main Activity handling the 5x5 Player vs CPU Bingo game logic, Best-of-3 series management,
 * automated 10-second board regeneration timers, and SQLite persistence.
 */
class BingoGameActivity : ComponentActivity() {

    // Board datasets containing 25 unique numbers selected from 1..70
    private var playerNumbers = listOf<Int>()
    private var cpuNumbers = listOf<Int>()

    // Boolean tracking arrays marking cell selections (indices 0 to 24)
    private val playerMatched = BooleanArray(25) { false }
    private val cpuMatched = BooleanArray(25) { false }

    // UI references mapping to individual dynamic grid cells
    private val playerCellViews = mutableListOf<TextView>()
    private val cpuCellViews = mutableListOf<TextView>()

    // Shuffled pool preventing duplicate draws during a match
    private val drawPool = mutableListOf<Int>()

    // Round performance metrics and Best-of-3 trackers
    private var roundCount = 0
    private var currentGameNumber = 1
    private var currentMatchNumber = 1
    private var playerSeriesWins = 0
    private var cpuSeriesWins = 0

    // Match state flags and winning data
    private var isGameOver = false
    private var winningFiveNumbers = listOf<Int>()
    private var currentUsername = "Player"

    // Async countdown manager for automated board reset
    private var resetTimer: CountDownTimer? = null

    // Database helper reference
    private lateinit var dbHelper: BingoDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bingo_game)

        dbHelper = BingoDatabaseHelper(this)

        // UI Widget Bindings
        val tvSeriesScore = findViewById<TextView>(R.id.tvSeriesScore)
        val tvBingoInfo = findViewById<TextView>(R.id.tvBingoInfo)
        val tvDrawnNumber = findViewById<TextView>(R.id.tvDrawnNumber)
        val tvPlayerBoardLabel = findViewById<TextView>(R.id.tvPlayerBoardLabel)
        val gridPlayerBoard = findViewById<GridLayout>(R.id.gridPlayerBoard)
        val gridCpuBoard = findViewById<GridLayout>(R.id.gridCpuBoard)
        val btnDrawNumber = findViewById<Button>(R.id.btnDrawNumber)
        val btnNewBingo = findViewById<Button>(R.id.btnNewBingo)
        val btnViewHistory = findViewById<Button>(R.id.btnViewHistory)
        val btnBackFromBingo = findViewById<Button>(R.id.btnBackFromBingo)

        // Retrieve active session username from SharedPreferences
        val prefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        currentUsername = prefs.getString("current_logged_user", "admin") ?: "admin"
        tvPlayerBoardLabel.text = "$currentUsername's Board"

        // Dynamically initialize 5x5 grid layouts
        buildGridCells(gridPlayerBoard, playerCellViews)
        buildGridCells(gridCpuBoard, cpuCellViews)

        updateSeriesText(tvSeriesScore)
        setupNewBoard(tvBingoInfo, tvDrawnNumber, btnDrawNumber)

        // Click listener for drawing the next random number from the pool
        btnDrawNumber.setOnClickListener {
            if (isGameOver || drawPool.isEmpty()) return@setOnClickListener

            roundCount++
            val drawn = drawPool.removeAt(0)
            tvBingoInfo.text = "Player: $currentUsername | Round: $roundCount"

            // Evaluate Player board hit
            val playerIdx = playerNumbers.indexOf(drawn)
            val playerHit = playerIdx != -1
            if (playerHit) {
                playerMatched[playerIdx] = true
                playerCellViews[playerIdx].setBackgroundColor(Color.parseColor("#66BB6A"))
                playerCellViews[playerIdx].setTextColor(Color.WHITE)
            }

            // Evaluate CPU opponent board hit
            val cpuIdx = cpuNumbers.indexOf(drawn)
            val cpuHit = cpuIdx != -1
            if (cpuHit) {
                cpuMatched[cpuIdx] = true
                cpuCellViews[cpuIdx].setBackgroundColor(Color.parseColor("#EF5350"))
                cpuCellViews[cpuIdx].setTextColor(Color.WHITE)
            }

            tvDrawnNumber.text = when {
                playerHit && cpuHit -> "Drawn: $drawn — BOTH hit!"
                playerHit -> "Drawn: $drawn — You hit!"
                cpuHit -> "Drawn: $drawn — Opponent hit!"
                else -> "Drawn: $drawn — No hit"
            }

            // Check for completed 5-in-a-row lines on both boards
            val playerWinningLine = checkBingoLine(playerNumbers, playerMatched)
            val cpuWinningLine = checkBingoLine(cpuNumbers, cpuMatched)

            if (playerWinningLine != null || cpuWinningLine != null) {
                isGameOver = true
                btnDrawNumber.isEnabled = false

                val resultHeader: String
                val recordedUser: String
                if (playerWinningLine != null && cpuWinningLine != null) {
                    winningFiveNumbers = playerWinningLine
                    recordedUser = "$currentUsername (Tie)"
                    resultHeader = "Game $currentGameNumber - Match $currentMatchNumber: DOUBLE BINGO (TIE)!"
                } else if (playerWinningLine != null) {
                    playerSeriesWins++
                    winningFiveNumbers = playerWinningLine
                    recordedUser = currentUsername
                    resultHeader = "Game $currentGameNumber - Match $currentMatchNumber: $currentUsername wins!"
                } else {
                    cpuSeriesWins++
                    winningFiveNumbers = cpuWinningLine!!
                    recordedUser = "$currentUsername (CPU Won)"
                    resultHeader = "Game $currentGameNumber - Match $currentMatchNumber: CPU wins!"
                }

                // Persist match details to SQLite database
                saveMatchToDatabase(currentGameNumber, currentMatchNumber, recordedUser, roundCount, winningFiveNumbers)
                updateSeriesText(tvSeriesScore)

                val isSeriesFinished = (playerSeriesWins >= 2 || cpuSeriesWins >= 2)
                startAutoRegenerateCountdown(resultHeader, isSeriesFinished, tvSeriesScore, tvBingoInfo, tvDrawnNumber, btnDrawNumber)
            }
        }

        // Manual series reset button
        btnNewBingo.setOnClickListener {
            resetTimer?.cancel()
            currentGameNumber++
            currentMatchNumber = 1
            playerSeriesWins = 0
            cpuSeriesWins = 0
            updateSeriesText(tvSeriesScore)
            setupNewBoard(tvBingoInfo, tvDrawnNumber, btnDrawNumber)
        }

        // Display historical records dialog
        btnViewHistory.setOnClickListener { showHistoryDialog() }

        // Return to Hub menu
        btnBackFromBingo.setOnClickListener {
            resetTimer?.cancel()
            finish()
        }
    }

    /**
     * Formats current system time and records match statistics into the database.
     */
    private fun saveMatchToDatabase(gameNumber: Int, matchNumber: Int, username: String, roundNumber: Int, fiveNumbers: List<Int>) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        dbHelper.insertBingoRecord(gameNumber, matchNumber, username, roundNumber, fiveNumbers, timestamp)
        Toast.makeText(this, "Saved Game $gameNumber - Match $matchNumber to SQLite!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Renders an AlertDialog containing all SQLite match history rows.
     */
    private fun showHistoryDialog() {
        val records = dbHelper.getAllRecords()
        val message = if (records.isEmpty()) {
            "No Bingo matches recorded in SQLite yet."
        } else {
            records.joinToString("\n\n") {
                "Game ${it.gameNumber} : Match ${it.matchNumber}\n• User: ${it.username}\n• Rounds: ${it.roundNumber}\n• Numbers: [${it.bingoNumbers}]\n• Time: ${it.systemTime}"
            }
        }

        AlertDialog.Builder(this)
            .setTitle("SQLite Match History (${records.size})")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .setNeutralButton("Clear History") { _, _ ->
                dbHelper.clearAllRecords()
                currentGameNumber = 1
                currentMatchNumber = 1
                Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * Manages a 10-second countdown timer before automatically starting the next board.
     */
    private fun startAutoRegenerateCountdown(
        baseMessage: String,
        resetWholeSeries: Boolean,
        tvSeriesScore: TextView,
        tvBingoInfo: TextView,
        tvDrawnNumber: TextView,
        btnDrawNumber: Button
    ) {
        resetTimer?.cancel()
        resetTimer = object : CountDownTimer(10_000L, 1_000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secs = (millisUntilFinished / 1000L) + 1
                tvDrawnNumber.text = "$baseMessage\nNext board in ${secs}s..."
            }

            override fun onFinish() {
                if (resetWholeSeries) {
                    currentGameNumber++
                    currentMatchNumber = 1
                    playerSeriesWins = 0
                    cpuSeriesWins = 0
                } else {
                    currentMatchNumber++
                }
                updateSeriesText(tvSeriesScore)
                setupNewBoard(tvBingoInfo, tvDrawnNumber, btnDrawNumber)
            }
        }.start()
    }

    private fun updateSeriesText(tvSeriesScore: TextView) {
        tvSeriesScore.text = "Game $currentGameNumber (Match $currentMatchNumber) | $currentUsername $playerSeriesWins - $cpuSeriesWins CPU"
    }

    /**
     * Programmatically constructs 25 TextView cells inside a 5x5 GridLayout.
     */
    private fun buildGridCells(gridLayout: GridLayout, cellList: MutableList<TextView>) {
        gridLayout.removeAllViews()
        cellList.clear()
        for (row in 0 until 5) {
            for (col in 0 until 5) {
                val cell = TextView(this).apply {
                    gravity = Gravity.CENTER
                    textSize = 12f
                    setBackgroundColor(Color.parseColor("#E0E0E0"))
                    setTextColor(Color.BLACK)
                }
                val params = GridLayout.LayoutParams(
                    GridLayout.spec(row, 1f),
                    GridLayout.spec(col, 1f)
                ).apply {
                    width = 0
                    height = dpToPx(32)
                    setMargins(2, 2, 2, 2)
                }
                gridLayout.addView(cell, params)
                cellList.add(cell)
            }
        }
    }

    /**
     * Refills the 1..70 draw pool and populates fresh 5x5 boards for both player and CPU.
     */
    private fun setupNewBoard(tvBingoInfo: TextView, tvDrawnNumber: TextView, btnDrawNumber: Button) {
        roundCount = 0
        isGameOver = false
        btnDrawNumber.isEnabled = true
        winningFiveNumbers = emptyList()
        playerMatched.fill(false)
        cpuMatched.fill(false)

        drawPool.clear()
        drawPool.addAll((1..70).shuffled())

        playerNumbers = (1..70).shuffled().take(25)
        cpuNumbers = (1..70).shuffled().take(25)

        for (i in 0 until 25) {
            playerCellViews[i].text = playerNumbers[i].toString()
            playerCellViews[i].setBackgroundColor(Color.parseColor("#E0E0E0"))
            playerCellViews[i].setTextColor(Color.BLACK)

            cpuCellViews[i].text = cpuNumbers[i].toString()
            cpuCellViews[i].setBackgroundColor(Color.parseColor("#E0E0E0"))
            cpuCellViews[i].setTextColor(Color.BLACK)
        }

        tvBingoInfo.text = "Player: $currentUsername | Round: 0"
        tvDrawnNumber.text = "Tap 'Draw Next Number' to start!"
    }

    /**
     * Validates all 12 potential Bingo lines (5 rows, 5 columns, 2 diagonals).
     */
    private fun checkBingoLine(boardNumbers: List<Int>, matched: BooleanArray): List<Int>? {
        val winningLines = listOf(
            listOf(0,1,2,3,4), listOf(5,6,7,8,9), listOf(10,11,12,13,14), listOf(15,16,17,18,19), listOf(20,21,22,23,24),
            listOf(0,5,10,15,20), listOf(1,6,11,16,21), listOf(2,7,12,17,22), listOf(3,8,13,18,23), listOf(4,9,14,19,24),
            listOf(0,6,12,18,24), listOf(4,8,12,16,20)
        )
        for (line in winningLines) {
            if (line.all { matched[it] }) {
                return line.map { boardNumbers[it] }
            }
        }
        return null
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        resetTimer?.cancel()
        dbHelper.close()
    }
}