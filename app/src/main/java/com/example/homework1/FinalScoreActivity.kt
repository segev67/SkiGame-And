package com.example.homework1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class FinalScoreActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SCORE = "EXTRA_SCORE"
        const val EXTRA_DISTANCE = "EXTRA_DISTANCE"
        const val EXTRA_GAME_MODE = "EXTRA_GAME_MODE"
    }

    //Handler used only for small UI delays (permission dialog timing)
    private val handler = Handler(Looper.getMainLooper())

    private var finalScore: Int = 0
    private var finalDistance: Int = 0
    private var gameModeName: String = GameMode.BUTTON_SLOW.name

    private var bestScore: Int = 0
    private var currentLocation: Location? = null

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    //Permission request callback (location is optional)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            fetchLocationThenAskName()
        } else {
            //Still save score even without location
            askNameAndSave()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_score)

        //Read game results from Intent
        finalScore = intent.getIntExtra(EXTRA_SCORE, 0)
        finalDistance = intent.getIntExtra(EXTRA_DISTANCE, 0)
        gameModeName = intent.getStringExtra(EXTRA_GAME_MODE)
            ?: GameMode.BUTTON_SLOW.name

        setupBackPress()

        //Compute best score from stored Top 10
        bestScore = TopScoresRepository.getScores(this)
            .maxOfOrNull { it.score }
            ?.coerceAtLeast(finalScore) ?: finalScore

        //Update UI values
        findViewById<TextView>(R.id.txtFinalScore).text =
            getString(R.string.current_score, finalScore)

        findViewById<TextView>(R.id.txtBestScore).text =
            getString(R.string.high_score, bestScore)

        findViewById<TextView>(R.id.txtDistance).text =
            getString(R.string.distance, finalDistance)


        //Restart game with same mode
        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnPlayAgain
        ).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("GAME_MODE", gameModeName)
            }
            startActivity(intent)
            finish()
        }

        //Open Top Scores screen
        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnTopScores
        ).setOnClickListener {
            startActivity(Intent(this, TopScoresActivity::class.java))
            finish()
        }

        //Back to main menu
        findViewById<com.google.android.material.button.MaterialButton>(
            R.id.btnBackToMenu
        ).setOnClickListener {
            navigateToMenu()
        }

        //Small delay so UI is visible before permission dialog appears
        handler.postDelayed({
            requestLocationPermission()
        }, 400)
    }

    //Override system back to always return to menu
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMenu()
            }
        })
    }

    private fun navigateToMenu() {
        val intent = Intent(this, MenuActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(intent)
        finish()
    }

    //Check location permission before trying to fetch location
    private fun requestLocationPermission() {
        val fine = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarse = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fine == PackageManager.PERMISSION_GRANTED ||
            coarse == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocationThenAskName()
            return
        }

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    //Fetch current location
    private fun fetchLocationThenAskName() {
        try {
            val tokenSource =
                com.google.android.gms.tasks.CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token
            ).addOnSuccessListener { location ->
                currentLocation = location
                askNameAndSave()
            }.addOnFailureListener {
                askNameAndSave()
            }
        } catch (_: SecurityException) {
            askNameAndSave()
        }
    }

    //Ask player for name and save score
    private fun askNameAndSave() {
        if (isFinishing || isDestroyed) return

        val input = EditText(this).apply {
            hint = "Enter your name"
            setPadding(50, 20, 50, 20)
        }

        AlertDialog.Builder(this)
            .setTitle("Save your score")
            .setMessage(
                "Score: $finalScore\n" +
                        "Distance: ${finalDistance}m\n\n" +
                        "Enter your name to save:"
            )
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Save") { dialog, _ ->
                val name = input.text.toString().trim().ifEmpty { "Player" }
                saveScore(name)
                dialog.dismiss()
            }
            .setNegativeButton("Skip") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    //Save score to SharedPreferences
    private fun saveScore(playerName: String) {
        val lat = currentLocation?.latitude ?: 0.0
        val lng = currentLocation?.longitude ?: 0.0
        val hasLocation = currentLocation != null

        Thread {
            TopScoresRepository.addScore(
                context = this,
                playerName = playerName,
                score = finalScore,
                distance = finalDistance,
                latitude = lat,
                longitude = lng,
                hasLocation = hasLocation
            )
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
