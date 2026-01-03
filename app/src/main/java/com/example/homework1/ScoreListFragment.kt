package com.example.homework1

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment

class ScoreListFragment : Fragment() {

    interface ScoreClickListener {
        fun onScoreSelected(score: TopScore)
    }

    private var listener: ScoreClickListener? = null
    private lateinit var listView: ListView
    private var scores: List<TopScore> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ScoreClickListener) {
            listener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_score_list, container, false)
        listView = view.findViewById(R.id.listScores)
        return view
    }

    override fun onResume() {
        super.onResume()
        //upload the top scores from the repository
        val ctx = requireContext()
        scores = TopScoresRepository.getScores(ctx)

        //Score + Distance each line
        val items = scores.mapIndexed { index, s ->
            "${index + 1}. ${s.playerName} - Score: ${s.score}, Dist: ${s.distance}m"
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_score_row,
            items
        )
        listView.adapter = adapter

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val score = scores[position]
                listener?.onScoreSelected(score)
            }
    }
}