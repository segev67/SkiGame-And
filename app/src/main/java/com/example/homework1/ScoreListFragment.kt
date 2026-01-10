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
import com.example.homework1.interfaces.Callback_TopScoreClicked

class ScoreListFragment : Fragment() {

    private var callback: Callback_TopScoreClicked? = null
    private lateinit var listView: ListView
    private var scores: List<TopScore> = emptyList()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Host activity must implement the interface
        callback = context as? Callback_TopScoreClicked
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
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
        //Load the top scores from repository
        val ctx = requireContext()
        scores = TopScoresRepository.getScores(ctx)

        //Build text for each row: index + player name + score + distance
        val items = scores.mapIndexed { index, s ->
            "${index + 1}. ${s.playerName} - Score: ${s.score}, Dist: ${s.distance}m"
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_score_row,
            items
        )
        listView.adapter = adapter

        //When user clicks on row – trigger callback
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->

                val score = scores[position]

                //Trigger callback (notify activity)
                callback?.topScoreItemClicked(score)
            }

    }
}
