package com.example.homework1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TopScoresActivity : AppCompatActivity(), ScoreListFragment.ScoreClickListener {

    private lateinit var mapFragment: ScoreMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_scores)

        val listFragment = ScoreListFragment()
        mapFragment = ScoreMapFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_scores_container, listFragment)
            .replace(R.id.fragment_map_container, mapFragment)
            .commit()
    }

    // callback when click on row in table
    override fun onScoreSelected(score: TopScore) {
        mapFragment.showLocation(score)
    }
}
