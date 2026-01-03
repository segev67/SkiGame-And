package com.example.homework1

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.homework1.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set initial UI state according to the current switch value
        updateFastModeUi(binding.switchSpeed.isChecked)

        // Listen to switch changes (Fast mode ON / OFF)
        binding.switchSpeed.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Fast mode ON", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Slow mode ON", Toast.LENGTH_SHORT).show()
            }
            updateFastModeUi(isChecked)
        }

        // Play with buttons mode (speed depends on switch)
        binding.btnPlayButton.setOnClickListener {
            val mode = if (binding.switchSpeed.isChecked) {
                GameMode.BUTTON_FAST
            } else {
                GameMode.BUTTON_SLOW
            }
            startGame(mode)
        }

        // Play with sensor mode
        binding.btnPlaySensor.setOnClickListener {
            startGame(GameMode.SENSOR)
        }

        // Go to Top Scores screen
        binding.btnHighScores.setOnClickListener {
            startActivity(Intent(this, TopScoresActivity::class.java))
        }
    }

    // Helper to start the game with a given mode
    private fun startGame(mode: GameMode) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("GAME_MODE", mode.name)
        startActivity(intent)
    }

    // Update label color according to Fast/Slow mode
    private fun updateFastModeUi(isFast: Boolean) {
        if (isFast) {
            binding.txtFastModeLabel.setTextColor(Color.RED)
        } else {
            binding.txtFastModeLabel.setTextColor(Color.WHITE)
        }
    }
}
