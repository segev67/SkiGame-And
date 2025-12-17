package com.example.homework1


enum class TickResult {
    NONE,
    CRASH,
    GAME_OVER
}
class SkiGameManager(
    private val numRows: Int,
    private val numCols: Int,
    private val initialLives: Int
) {

    private var playerLane = numCols / 2
    private val map: Array<BooleanArray> =
        Array(numRows) { BooleanArray(numCols) { false } }

    private var lives = initialLives
    private var score = 0

    fun getPlayerLane(): Int = playerLane
    fun getLives(): Int = lives
    fun getScore(): Int = score
    fun getMap(): Array<BooleanArray> = map
    fun isGameOver(): Boolean = lives <= 0

    fun movePlayerLeft() {
        val firstLaneIndex = 0
        if (playerLane > firstLaneIndex) {
            playerLane--
        }
    }

    fun movePlayerRight() {
        val lastLaneIndex = numCols - 1
        if (playerLane < lastLaneIndex) {
            playerLane++
        }
    }

    /**
     * One game tick:
     * - Move obstacles down
     * - Create at most one obstacle in the top row
     * - Check for collision
     * @return TickResult describing what happened in this tick.
     */
    fun tick(): TickResult {
        val lastRowIndex = numRows - 1

        //Move all rows down
        for (row in lastRowIndex downTo 1) {
            for (col in 0 until numCols) {
                map[row][col] = map[row - 1][col]
            }
        }

        //Clear top row
        for (col in 0 until numCols) {
            map[0][col] = false
        }

        //Decide if create an obstacle in this row
        val shouldCreateObstacle = (0..1).random() == 1  // 50% chance
        if (shouldCreateObstacle) {
            val laneIndex = (0 until numCols).random()
            map[0][laneIndex] = true
        }

        return checkCollision()
    }

    //Checks collision on the last row and updates lives/score.

    private fun checkCollision(): TickResult {
        val lastRowIndex = numRows - 1

        return if (map[lastRowIndex][playerLane]) {
            // Player hits a tree
            lives--
            map[lastRowIndex][playerLane] = false

            if (lives <= 0) {
                TickResult.GAME_OVER
            } else {
                TickResult.CRASH
            }
        } else {
            //No collision, just increase score
            score++
            TickResult.NONE
        }
    }

    fun reset() {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                map[row][col] = false
            }
        }

        lives = initialLives
        score = 0
        playerLane = numCols / 2
    }
}

