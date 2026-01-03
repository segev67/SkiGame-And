package com.example.homework1

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object TopScoresRepository {

    private const val PREFS_NAME = "top_scores_prefs"
    private const val KEY_SCORES = "scores_json"
    private const val MAX_SCORES = 10

    fun addScore(
        context: Context,
        playerName: String,
        score: Int,
        distance: Int,
        latitude: Double,
        longitude: Double
    ) {
        val scores = getScores(context).toMutableList()
        scores.add(TopScore(playerName, score, distance, latitude, longitude))

        // sort top 10 scores from high to low
        val sorted = scores.sortedByDescending { it.score }.take(MAX_SCORES)

        saveScores(context, sorted)
    }

    fun getScores(context: Context): List<TopScore> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SCORES, null) ?: return emptyList()

        val arr = JSONArray(json)
        val result = mutableListOf<TopScore>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            result.add(
                TopScore(
                    playerName = obj.getString("playerName"),
                    score = obj.getInt("score"),
                    distance = obj.getInt("distance"),
                    latitude = obj.getDouble("lat"),
                    longitude = obj.getDouble("lng")
                )
            )
        }
        return result
    }

    private fun saveScores(context: Context, scores: List<TopScore>) {
        val arr = JSONArray()
        scores.forEach { s ->
            val obj = JSONObject()
            obj.put("playerName", s.playerName)
            obj.put("score", s.score)
            obj.put("distance", s.distance)
            obj.put("lat", s.latitude)
            obj.put("lng", s.longitude)
            arr.put(obj)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCORES, arr.toString()).apply()
    }
}
