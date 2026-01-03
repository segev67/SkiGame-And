package com.example.homework1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.homework1.databinding.ActivityTopScoresBinding

class TopScoresActivity : AppCompatActivity(), ScoreListFragment.ScoreClickListener {

    private lateinit var mapFragment: ScoreMapFragment
    private lateinit var binding: ActivityTopScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //init view binding for this activity
        binding = ActivityTopScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listFragment = ScoreListFragment()
        mapFragment = ScoreMapFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_scores_container, listFragment)
            .replace(R.id.fragment_map_container, mapFragment)
            .commit()

        binding.btnBackToMenu.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    //callback when click on row in table
    override fun onScoreSelected(score: TopScore) {
        mapFragment.showLocation(score)
    }

}
