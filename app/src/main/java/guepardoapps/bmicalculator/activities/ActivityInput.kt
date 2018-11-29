package guepardoapps.bmicalculator.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import es.dmoral.toasty.Toasty
import guepardoapps.bmicalculator.R
import guepardoapps.bmicalculator.controller.NavigationController
import guepardoapps.bmicalculator.enums.BmiLevel
import guepardoapps.bmicalculator.extensions.doubleFormat
import guepardoapps.bmicalculator.models.BmiContainer
import guepardoapps.bmicalculator.services.BmiService
import kotlinx.android.synthetic.main.side_input.*
import java.lang.Exception

class ActivityInput : Activity() {

    private lateinit var bmiService: BmiService
    private var lastResult: Pair<BmiContainer, BmiLevel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.side_input)

        bmiService = BmiService(this)
        val lastSaveResult = bmiService.getList().lastOrNull()
        if (lastSaveResult != null) {
            lastResult = Pair(lastSaveResult, BmiLevel.getByLevel(lastSaveResult.value))

            bmiResult.text = lastResult!!.first.value.doubleFormat(2)
            bmiResult.setBackgroundColor(lastResult!!.second.backgroundColor.toInt())
            bmiResultDescription.text = lastResult!!.second.description
        }

        setupButtons()
    }

    private fun setupButtons() {
        buttonCalculate.setOnClickListener {
            calculateBmi()
        }

        imageButtonGraph.setOnClickListener {
            NavigationController(this).navigate(ActivityGraph::class.java, true)
        }

        imageButtonShare.setOnClickListener {
            if (lastResult != null) {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, "My last BMI: ${lastResult!!.first.value.doubleFormat(2)}\n")
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            }
        }

        imageButtonAbout.setOnClickListener {
            NavigationController(this).navigate(ActivityAbout::class.java, false)
        }
    }

    private fun calculateBmi() {
        val weightString = inputFieldWeight.text.toString()
        val heightString = inputFieldHeight.text.toString()
        if (weightString.isEmpty() || heightString.isEmpty()) {
            Toasty.error(this, "Please provide all needed fields!", Toast.LENGTH_LONG).show()
            return
        }

        val weight: Double
        val height: Double

        try {
            weight = weightString.toDouble()
            height = heightString.toDouble() / 100
        } catch (exception: Exception) {
            Toasty.error(this, "Please provide valid numbers!", Toast.LENGTH_LONG).show()
            return
        }

        if (weight !in 0..1000) {
            Toasty.error(this, "Please enter a valid weight!", Toast.LENGTH_LONG).show()
            return
        }

        if (height <= 0 || height >= 3) {
            Toasty.error(this, "Please enter a valid weight!", Toast.LENGTH_LONG).show()
            return
        }

        lastResult = bmiService.calculateBmi(weight, height)

        bmiResult.text = lastResult!!.first.value.doubleFormat(2)
        bmiResult.setBackgroundColor(lastResult!!.second.backgroundColor.toInt())
        bmiResultDescription.text = lastResult!!.second.description
    }
}