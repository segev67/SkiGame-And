package com.example.homework1

import com.example.homework1.TickResult
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.homework1.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        private const val GAME_TICK_MILLIS = 1000L  // 1 second per tick
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: SkiGameManager

    private lateinit var treeViews: Array<Array<ImageView>>
    private lateinit var skierViews: Array<ImageView>
    private lateinit var heartViews: Array<ImageView>

    private lateinit var handler: Handler

    private val gameLoop = object : Runnable {
        override fun run() {
            val result = gameManager.tick()
            updateUi()

            when (result) {
                TickResult.NONE -> {
                }

                TickResult.CRASH -> {
                    //Crash but still has lives
                    Toast.makeText(this@MainActivity, "Crash!", Toast.LENGTH_SHORT).show()
                }

                TickResult.GAME_OVER -> {
                    //Last life lost: show ONLY Game Over
                    Toast.makeText(this@MainActivity, "Game Over", Toast.LENGTH_SHORT).show()

                    gameManager.reset()
                    Utils.clearTrees(treeViews)
                    updateUi()
                }
            }

            handler.postDelayed(this, GAME_TICK_MILLIS)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        initViews()

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

        binding.btnLeft.setOnClickListener {
            gameManager.movePlayerLeft()
            Utils.updateSkier(skierViews, gameManager.getPlayerLane())
        }

        binding.btnRight.setOnClickListener {
            gameManager.movePlayerRight()
            Utils.updateSkier(skierViews, gameManager.getPlayerLane())
        }

        startGameLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameLoop)
    }

    private fun startGameLoop() {
        handler.removeCallbacks(gameLoop)
        handler.postDelayed(gameLoop, GAME_TICK_MILLIS)
    }

    private fun initViews() {
        heartViews = arrayOf(
            binding.heart1,
            binding.heart2,
            binding.heart3
        )

        treeViews = arrayOf(
            arrayOf(binding.imgTree00, binding.imgTree01, binding.imgTree02),
            arrayOf(binding.imgTree10, binding.imgTree11, binding.imgTree12),
            arrayOf(binding.imgTree20, binding.imgTree21, binding.imgTree22)
        )

        skierViews = arrayOf(
            binding.imgSkier0,
            binding.imgSkier1,
            binding.imgSkier2
        )
    }

    private fun updateUi() {
        Utils.updateTrees(treeViews, gameManager.getMap())
        Utils.updateSkier(skierViews, gameManager.getPlayerLane())
        Utils.updateHearts(heartViews, gameManager.getLives())
    }
}

