package com.example.homework1

import android.view.View
import android.widget.ImageView

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
        map: Array<BooleanArray>
    ) {
        for (rowIndex in treeViews.indices) {
            for (colIndex in treeViews[rowIndex].indices) {
                treeViews[rowIndex][colIndex].visibility =
                    if (map[rowIndex][colIndex]) View.VISIBLE else View.INVISIBLE
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
}
