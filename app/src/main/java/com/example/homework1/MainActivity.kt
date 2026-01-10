package com.example.homework1
import com.example.homework1.utilities.SensorConfig
import com.example.homework1.utilities.GameConfig

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import android.widget.ImageView
import android.content.Intent
import com.example.homework1.interfaces.TiltCallback
import com.example.homework1.utilities.TiltDetector
import androidx.appcompat.app.AppCompatActivity
import com.example.homework1.databinding.ActivityMainBinding
import com.example.homework1.utilities.GameUiUpdater


class MainActivity : AppCompatActivity() {

    private var gameTickMillis = GameConfig.TICK_SLOW_MS
    private var baseTickMillis = gameTickMillis

    private var isSpeedBoosted: Boolean = false
    private var lastY: Float = 0f
    private var tiltDirection: Int = 0   // range [-1,1]
    private var lastTiltTime: Long = 0L

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameManager: SkiGameManager

    private lateinit var treeViews: Array<Array<ImageView>>
    private lateinit var skierViews: Array<ImageView>
    private lateinit var heartViews: Array<ImageView>

    private lateinit var handler: Handler
    private lateinit var gameMode: GameMode
    private var isGameRunning = false
    private var isGameOver = false

    private lateinit var tiltDetector: TiltDetector
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
                    isGameOver = true
                    stopGameLoop()

                    val finalScore = gameManager.getScore()
                    val finalDistance = gameManager.getDistance()

                    val intent = Intent(this@MainActivity, FinalScoreActivity::class.java).apply {
                        putExtra(FinalScoreActivity.EXTRA_SCORE, finalScore)
                        putExtra(FinalScoreActivity.EXTRA_DISTANCE, finalDistance)
                        putExtra(FinalScoreActivity.EXTRA_GAME_MODE, gameMode.name)
                    }

                    startActivity(intent)
                    finish()
                    return
                }
            }

            //Only continue the loop if the game is not over
            handler.postDelayed(this, gameTickMillis)
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())

        initViews()

        //Read game mode from Intent (default: BUTTON_SLOW)
        val modeFromIntent = intent.getStringExtra("GAME_MODE") ?: GameMode.BUTTON_SLOW.name
        gameMode = GameMode.valueOf(modeFromIntent)

        initTiltDetector()

        //Set tick speed according to game mode
        gameTickMillis = when (gameMode) {
            GameMode.BUTTON_SLOW -> GameConfig.TICK_SLOW_MS
            GameMode.BUTTON_FAST -> GameConfig.TICK_FAST_MS
            GameMode.SENSOR      -> GameConfig.TICK_SENSOR_MS
        }
        baseTickMillis = gameTickMillis

        //(Sensor logic will be added later)
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

        GameUiUpdater.clearTrees(treeViews)
        GameUiUpdater.updateSkier(skierViews, gameManager.getPlayerLane())
        GameUiUpdater.updateHearts(heartViews, gameManager.getLives())
        GameUiUpdater.updateScore(binding.txtScore, gameManager.getScore())
        GameUiUpdater.updateOdometer(binding.txtOdometer, gameManager.getDistance())

        // Left button (buttons mode only)
        binding.btnLeft.setOnClickListener {
            if (gameMode != GameMode.SENSOR) {
                gameManager.movePlayerLeft()
                GameUiUpdater.updateSkier(skierViews, gameManager.getPlayerLane())
            }
        }

        //Right button (buttons mode only)
        binding.btnRight.setOnClickListener {
            if (gameMode != GameMode.SENSOR) {
                gameManager.movePlayerRight()
                GameUiUpdater.updateSkier(skierViews, gameManager.getPlayerLane())
            }
        }

        startGameLoop()
    }

    override fun onDestroy() {
        stopGameLoop()

        if (gameMode == GameMode.SENSOR) {
            tiltDetector.stop()
        }

        super.onDestroy()
    }

    override fun onPause() {
        stopGameLoop()

        //Stop sensors when leaving the screen
        if (gameMode == GameMode.SENSOR) {
            tiltDetector.stop()
        }

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

        //Start sensors when returning to the screen
        if (gameMode == GameMode.SENSOR && !isGameOver) {
            tiltDetector.start()
        }

        startGameLoop()
    }

    private fun startGameLoop() {
        if (isGameRunning || isGameOver)
            return

        isGameRunning = true
        handler.removeCallbacks(gameLoop)
        handler.postDelayed(gameLoop, gameTickMillis)
    }

    private fun stopGameLoop() {
        //Stop ticks and mark game as not running
        isGameRunning = false
        handler.removeCallbacks(gameLoop)
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
        GameUiUpdater.updateTrees(treeViews, gameManager.getMap())
        GameUiUpdater.updateSkier(skierViews, gameManager.getPlayerLane())
        GameUiUpdater.updateHearts(heartViews, gameManager.getLives())
        GameUiUpdater.updateScore(binding.txtScore, gameManager.getScore())
        GameUiUpdater.updateOdometer(binding.txtOdometer, gameManager.getDistance())
    }

    private fun initTiltDetector() {
        tiltDetector = TiltDetector(
            this,
            object : TiltCallback {
                override fun onSensorData(x: Float, y: Float, z: Float) {
                    if (gameMode != GameMode.SENSOR || isGameOver || !isGameRunning) return

                    val numCols = treeViews[0].size  // should be 5 in your game

                    //Normalize X to [0..1]
                    val normalizedX = ((x + SensorConfig.MAX_TILT) / (2f * SensorConfig.MAX_TILT))
                        .coerceIn(0f, 1f)

                    //Map to lane index [0..4]
                    val targetLane = (normalizedX * (numCols - 1) + 0.5f)
                    .toInt()
                        .coerceIn(0, numCols - 1)

                    if (targetLane != gameManager.getPlayerLane()) {
                        gameManager.movePlayerToLane(targetLane)
                        GameUiUpdater.updateSkier(skierViews, gameManager.getPlayerLane())
                    }

                    // -------- tilt back and forth for speed (Y axis) --------
                    val now = System.currentTimeMillis()
                    val yDiff = y - lastY

                    //Detect swing
                    if (kotlin.math.abs(yDiff) > SensorConfig.Y_SWING_THRESHOLD) {
                        val currentDirection = if (yDiff > 0) 1 else -1

                        //Speed boost triggers on direction change (back-and-forth)
                        if (currentDirection != tiltDirection && tiltDirection != 0) {
                            lastTiltTime = now

                            if (!isSpeedBoosted) {
                                isSpeedBoosted = true
                                gameTickMillis = baseTickMillis / SensorConfig.FAST_SPEED_MULTIPLIER

                                //Apply immediately
                                handler.removeCallbacks(gameLoop)
                                handler.postDelayed(gameLoop, gameTickMillis)
                            }
                        }
                        tiltDirection = currentDirection
                    }
                    lastY = y

                    //Turn off boost after duration
                    if (isSpeedBoosted && now - lastTiltTime > SensorConfig.SPEED_BOOST_DURATION_MS) {
                        isSpeedBoosted = false
                        gameTickMillis = baseTickMillis

                        //Apply immediately
                        handler.removeCallbacks(gameLoop)
                        handler.postDelayed(gameLoop, gameTickMillis)
                    }
                }
            }
        )
    }
}

