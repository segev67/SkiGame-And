package com.example.homework1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ScoreMapFragment : Fragment() {

    private lateinit var txtMapInfo: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_score_map, container, false)
        txtMapInfo = view.findViewById(R.id.txtMapInfo)
        return view
    }

    // readed from HighScoresActivity wehn click on row
    fun showLocation(score: TopScore) {
        txtMapInfo.text =
            "Location for ${score.playerName}\n" +
                    "Score: ${score.score}, Dist: ${score.distance}m\n" +
                    "Lat: ${score.latitude}, Lng: ${score.longitude}"

    }
}
