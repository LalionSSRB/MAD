package hk.edu.thei.mti.wongyucheung_lab2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvLoginMessage = findViewById<TextView>(R.id.tvLoginMessage)

        // Using SharedPreferences for account verification
        val prefs: SharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Seed default login credentials if first run
        if (!prefs.contains("saved_username")) {
            prefs.edit()
                .putString("saved_username", "admin")
                .putString("saved_password", "1234")
                .apply()
        }

        btnLogin.setOnClickListener {
            val user = etUsername.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            val registeredUser = prefs.getString("saved_username", "admin")
            val registeredPass = prefs.getString("saved_password", "1234")

            if (user == registeredUser && pass == registeredPass) {
                // Save current logged in user session
                prefs.edit().putString("current_logged_user", user).apply()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                tvLoginMessage.text = "Invalid username or password. Try admin / 1234"
            }
        }
    }
}