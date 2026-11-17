package hk.edu.thei.mti.wongyucheung_lab2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class ConfigurationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        val etPlayerName = findViewById<EditText>(R.id.etPlayerName)
        val etTargetScore = findViewById<EditText>(R.id.etTargetScore)
        val btnSaveConfig = findViewById<Button>(R.id.btnSaveConfig)
        val btnReturnConfig = findViewById<Button>(R.id.btnReturnConfig)

        val prefs: SharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Read current preferences
        val currentName = prefs.getString("player_name", "Player 1")
        val currentTarget = prefs.getInt("target_score", 3)

        etPlayerName.setText(currentName)
        etTargetScore.setText(currentTarget.toString())

        // Save new settings
        btnSaveConfig.setOnClickListener {
            val newName = etPlayerName.text.toString().trim()
            val newTarget = etTargetScore.text.toString().toIntOrNull() ?: 3

            prefs.edit()
                .putString("player_name", if (newName.isEmpty()) "Player 1" else newName)
                .putInt("target_score", newTarget)
                .apply()

            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show()
        }

        btnReturnConfig.setOnClickListener {
            finish()
        }
    }
}