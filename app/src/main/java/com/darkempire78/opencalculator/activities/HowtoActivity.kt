package com.darkempire78.opencalculator.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.darkempire78.opencalculator.R
import com.darkempire78.opencalculator.databinding.ActivityHowtoBinding

class HowtoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHowtoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHowtoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // back button
        findViewById<ImageView>(R.id.howto_back_button).setOnClickListener {
            finish()
        }
    }
}
