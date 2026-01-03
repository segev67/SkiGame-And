package com.example.homework1

import android.view.View
import android.widget.ImageView
import android.widget.TextView


object Utils {

    fun clearTrees(treeViews: Array<Array<ImageView>>) {
        for (row in treeViews) {
            for (tree in row) {
                tree.visibility = View.INVISIBLE
            }
        }
    }

    fun updateTrees(
        treeViews: Array<Array<ImageView>>,
        map: Array<Array<CellType>>
    ) {
        for (rowIndex in treeViews.indices) {
            for (colIndex in treeViews[rowIndex].indices) {
                val cell = map[rowIndex][colIndex]
                val view = treeViews[rowIndex][colIndex]

                when (cell) {
                    CellType.EMPTY -> {
                        view.visibility = View.INVISIBLE
                    }
                    CellType.TREE -> {
                        view.visibility = View.VISIBLE
                        view.setImageResource(R.drawable.img_tree)
                    }
                    CellType.COIN -> {
                        view.visibility = View.VISIBLE
                        view.setImageResource(R.drawable.ic_coin)
                    }
                }
            }
        }
    }

    fun updateSkier(
        skierViews: Array<ImageView>,
        playerLane: Int
    ) {
        for (laneIndex in skierViews.indices) {
            skierViews[laneIndex].visibility =
                if (laneIndex == playerLane) View.VISIBLE else View.INVISIBLE
        }
    }

    fun updateHearts(
        heartViews: Array<ImageView>,
        lives: Int
    ) {
        heartViews.forEachIndexed { index, imageView ->
            val lifeNumber = index + 1
            imageView.visibility =
                if (lives >= lifeNumber) View.VISIBLE else View.INVISIBLE
        }
    }

    fun updateScore(
        scoreView: TextView,
        score: Int
    ) {
        scoreView.text = "Score: $score"
    }

    fun updateOdometer(
        odometerView: TextView,
        distance: Int
    ) {
        odometerView.text = "Distance: $distance"
    }
}
