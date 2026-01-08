package com.example.homework1

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object TopScoresRepository {

    private const val PREF_NAME = "top_scores_prefs"
    private const val KEY_SCORES = "scores_json"
    private const val MAX_SCORES = 10

    fun getScores(context: Context): List<TopScore> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCORES, null) ?: return emptyList()

        val list = mutableListOf<TopScore>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val score = TopScore(
                    playerName = obj.getString("playerName"),
                    score = obj.getInt("score"),
                    distance = obj.getInt("distance"),
                    latitude = obj.optDouble("latitude", 0.0),
                    longitude = obj.optDouble("longitude", 0.0),
                    hasLocation = obj.optBoolean("hasLocation", false)
                )
                list.add(score)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //Always return sorted from high to low
        return list.sortedWith(compareByDescending<TopScore> { it.score }
            .thenByDescending { it.distance })
    }

    fun addScore(
        context: Context,
        playerName: String,
        score: Int,
        distance: Int,
        latitude: Double,
        longitude: Double,
        hasLocation: Boolean
    ) {
        val currentScores = getScores(context).toMutableList()

        //Create new score item
        val newScore = TopScore(
            playerName = playerName,
            score = score,
            distance = distance,
            latitude = latitude,
            longitude = longitude,
            hasLocation = hasLocation
        )

        //If we have less than MAX_SCORES, just add it
        if (currentScores.size < MAX_SCORES) {
            currentScores.add(newScore)
        } else {
            //We already have MAX_SCORES entries
            //Find the weakest score (last one because list is sorted descending)
            val weakest = currentScores.last()

            //If new score is not better than the weakest, do nothing
            val isBetter =
                newScore.score > weakest.score //||
                        //(newScore.score == weakest.score && newScore.distance > weakest.distance)

            if (!isBetter) {
                //Do not change the list at all
                return
            }

            //Otherwise, add the new score
            currentScores.add(newScore)
        }

        //Sort again (best first)
        val sorted = currentScores.sortedWith(
            compareByDescending<TopScore> { it.score }
                .thenByDescending { it.distance }
        )

        //Keep only top MAX_SCORES
        val top = sorted.take(MAX_SCORES)

        //Save back to SharedPreferences as JSON
        saveScores(context, top)
    }

    private fun saveScores(context: Context, scores: List<TopScore>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()

        for (s in scores) {
            val obj = JSONObject().apply {
                put("playerName", s.playerName)
                put("score", s.score)
                put("distance", s.distance)
                put("latitude", s.latitude)
                put("longitude", s.longitude)
                put("hasLocation", s.hasLocation)
            }
            array.put(obj)
        }

        prefs.edit()
            .putString(KEY_SCORES, array.toString())
            .apply()
    }
}
