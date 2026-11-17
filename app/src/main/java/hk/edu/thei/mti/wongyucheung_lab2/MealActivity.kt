package hk.edu.thei.mti.wongyucheung_lab2

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.ComponentActivity

class MealActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal)

        // View References
        val cbCustomer1 = findViewById<CheckBox>(R.id.cbCustomer1)
        val cbCustomer2 = findViewById<CheckBox>(R.id.cbCustomer2)
        val rgMeals = findViewById<RadioGroup>(R.id.rgMeals)
        val btnConfirm = findViewById<Button>(R.id.btnConfirm)
        val tvResult = findViewById<TextView>(R.id.tvResult)
        val btnReturn = findViewById<Button>(R.id.btnReturn)

        // Confirm Button Logic
        btnConfirm.setOnClickListener {
            val selectedCustomers = mutableListOf<String>()
            if (cbCustomer1.isChecked) selectedCustomers.add("Customer 1")
            if (cbCustomer2.isChecked) selectedCustomers.add("Customer 2")

            val checkedRadioButtonId = rgMeals.checkedRadioButtonId

            if (selectedCustomers.isEmpty() && checkedRadioButtonId == -1) {
                tvResult.text = "Please select customer and meal item:"
            } else if (selectedCustomers.isEmpty()) {
                tvResult.text = "Please select at least one customer:"
            } else if (checkedRadioButtonId == -1) {
                tvResult.text = "Please select item:"
            } else {
                val selectedRadio = findViewById<RadioButton>(checkedRadioButtonId)
                val mealName = selectedRadio.text.toString()
                val customerNames = selectedCustomers.joinToString(" and ")

                tvResult.text = "$customerNames select $mealName."
            }
        }

        // Return Button Logic (closes this activity and goes back to Game UI)
        btnReturn.setOnClickListener {
            finish()
        }
    }
}