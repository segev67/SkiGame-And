package com.example.homework1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.homework1.databinding.ActivityTopScoresBinding
import com.example.homework1.interfaces.Callback_TopScoreClicked

class TopScoresActivity : AppCompatActivity() {

    private lateinit var mapFragment: ScoreMapFragment
    private lateinit var binding: ActivityTopScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Init view binding for this activity
        binding = ActivityTopScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listFragment = ScoreListFragment()
        mapFragment = ScoreMapFragment()

        //Attach fragments to their containers
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_scores_container, listFragment)
            .replace(R.id.fragment_map_container, mapFragment)
            .commit()

        //Register callback
        ScoreListFragment.topScoreItemClicked =
            object : Callback_TopScoreClicked {
                override fun topScoreItemClicked(score: TopScore) {
                    //When a high score row is clicked -> update the map
                    mapFragment.showLocation(score)
                }
            }

        binding.btnBackToMenu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }
}
