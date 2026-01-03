package com.example.homework1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.widget.Toast
import android.widget.ImageView
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.homework1.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        private var gameTickMillis: Long = 1000L  // 1 second per tick
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: SkiGameManager

    private lateinit var treeViews: Array<Array<ImageView>>
    private lateinit var skierViews: Array<ImageView>
    private lateinit var heartViews: Array<ImageView>

    private lateinit var handler: Handler
    private lateinit var gameMode: GameMode
    private val gameLoop = object : Runnable {
        override fun run() {
            val result = gameManager.tick()
            updateUi()

            when (result) {
                TickResult.NONE -> {
                    //No special event, just continue
                }

                TickResult.CRASH -> {
                    //Player crashed but still has lives
                    Toast.makeText(this@MainActivity, "Crash!", Toast.LENGTH_SHORT).show()
                }

                TickResult.GAME_OVER -> {
                    //Game over - compute final stats
                    val finalScore = gameManager.getScore()
                    val finalDistance = gameManager.getDistance()

                    //Save to top scores
                    TopScoresRepository.addScore(
                        context = this@MainActivity,
                        playerName = "Player",
                        score = finalScore,
                        distance = finalDistance,
                        latitude = 32.0853,
                        longitude = 34.7818
                    )

                    //Show game over dialog (do NOT restart immediately)
                    showGameOverDialog(finalScore, finalDistance)
                }
            }

            //Only continue the loop if the game is not over
            if (result != TickResult.GAME_OVER) {
                handler.postDelayed(this, gameTickMillis)
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        initViews()

        // Read game mode from Intent (default: BUTTON_SLOW)
        val modeFromIntent = intent.getStringExtra("GAME_MODE") ?: GameMode.BUTTON_SLOW.name
        gameMode = GameMode.valueOf(modeFromIntent)

        // Set tick speed according to game mode
        gameTickMillis = when (gameMode) {
            GameMode.BUTTON_SLOW -> 1000L   // slow
            GameMode.BUTTON_FAST -> 500L    // fast
            GameMode.SENSOR      -> 800L
        }

        // (Sensor logic will be added later)
        if (gameMode == GameMode.SENSOR) {
            binding.btnLeft.visibility = View.GONE
            binding.btnRight.visibility = View.GONE
        } else {
            binding.btnLeft.visibility = View.VISIBLE
            binding.btnRight.visibility = View.VISIBLE
        }

        val numberOfRows = treeViews.size
        val numberOfCols = treeViews[0].size
        val initialLives = heartViews.size

        gameManager = SkiGameManager(
            numRows = numberOfRows,
            numCols = numberOfCols,
            initialLives = initialLives
        )

        Utils.clearTrees(treeViews)
        Utils.updateSkier(skierViews, gameManager.getPlayerLane())
        Utils.updateHearts(heartViews, gameManager.getLives())
        Utils.updateScore(binding.txtScore, gameManager.getScore())
        Utils.updateOdometer(binding.txtOdometer, gameManager.getDistance())

        // Left button (buttons mode only)
        binding.btnLeft.setOnClickListener {
            if (gameMode != GameMode.SENSOR) {
                gameManager.movePlayerLeft()
                Utils.updateSkier(skierViews, gameManager.getPlayerLane())
            }
        }

        // Right button (buttons mode only)
        binding.btnRight.setOnClickListener {
            if (gameMode != GameMode.SENSOR) {
                gameManager.movePlayerRight()
                Utils.updateSkier(skierViews, gameManager.getPlayerLane())
            }
        }

        startGameLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameLoop)
    }

    private fun startGameLoop() {
        handler.removeCallbacks(gameLoop)
        handler.postDelayed(gameLoop, gameTickMillis)
    }

    private fun initViews() {
        heartViews = arrayOf(
            binding.heart1,
            binding.heart2,
            binding.heart3
        )

        treeViews = arrayOf(
            arrayOf(binding.imgTree00, binding.imgTree01, binding.imgTree02, binding.imgTree03, binding.imgTree04),
            arrayOf(binding.imgTree10, binding.imgTree11, binding.imgTree12, binding.imgTree13, binding.imgTree14),
            arrayOf(binding.imgTree20, binding.imgTree21, binding.imgTree22, binding.imgTree23, binding.imgTree24),
            arrayOf(binding.imgTree30, binding.imgTree31, binding.imgTree32, binding.imgTree33, binding.imgTree34),
            arrayOf(binding.imgTree40, binding.imgTree41, binding.imgTree42, binding.imgTree43, binding.imgTree44),
            arrayOf(binding.imgTree50, binding.imgTree51, binding.imgTree52, binding.imgTree53, binding.imgTree54),
            arrayOf(binding.imgTree60, binding.imgTree61, binding.imgTree62, binding.imgTree63, binding.imgTree64),
            arrayOf(binding.imgTree70, binding.imgTree71, binding.imgTree72, binding.imgTree73, binding.imgTree74)
        )

        skierViews = arrayOf(
            binding.imgSkier0,
            binding.imgSkier1,
            binding.imgSkier2,
            binding.imgSkier3,
            binding.imgSkier4
        )
    }

    private fun updateUi() {
        Utils.updateTrees(treeViews, gameManager.getMap())
        Utils.updateSkier(skierViews, gameManager.getPlayerLane())
        Utils.updateHearts(heartViews, gameManager.getLives())
        Utils.updateScore(binding.txtScore, gameManager.getScore())
        Utils.updateOdometer(binding.txtOdometer, gameManager.getDistance())
    }

    private fun showGameOverDialog(finalScore: Int, finalDistance: Int) {
        // Get best score from repository
        val scores = TopScoresRepository.getScores(this)
        val bestScore = scores.maxOfOrNull { it.score } ?: finalScore

        // Inflate custom layout for the dialog
        val dialogView = layoutInflater.inflate(R.layout.game_over, null)

        val txtTitle = dialogView.findViewById<TextView>(R.id.txtGameOverTitle)
        val txtFinalScore = dialogView.findViewById<TextView>(R.id.txtFinalScore)
        val txtBestScore = dialogView.findViewById<TextView>(R.id.txtBestScore)
        val txtDistance = dialogView.findViewById<TextView>(R.id.txtDistance)
        val btnPlayAgain = dialogView.findViewById<Button>(R.id.btnPlayAgain)
        val btnTopScores = dialogView.findViewById<Button>(R.id.btnTopScores)
        val btnBackToMenu = dialogView.findViewById<Button>(R.id.btnBackToMenu)

        // Set texts
        txtTitle.text = "Game Over"
        txtFinalScore.text = "Your score: $finalScore"
        txtBestScore.text = "Best score: $bestScore"
        txtDistance.text = "Distance: $finalDistance"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // user must choose what to do
            .create()

        // Play again: reset game and restart loop
        btnPlayAgain.setOnClickListener {
            dialog.dismiss()
            gameManager.reset()
            Utils.clearTrees(treeViews)
            updateUi()
            startGameLoop()
        }

        // View top scores: open TopScoresActivity
        btnTopScores.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, TopScoresActivity::class.java)
            startActivity(intent)
        }

        // Back to main menu: go back to MenuActivity
        btnBackToMenu.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MenuActivity::class.java)
            // Clear this activity from the back stack so back won't return here
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // finish MainActivity
        }

        dialog.show()
    }
}

