package com.example.homework1
import com.example.homework1.utilities.GameConfig


enum class TickResult {
    NONE,
    CRASH,
    GAME_OVER
}

enum class CellType {
    EMPTY,
    TREE,
    COIN
}

class SkiGameManager(
    private val numRows: Int,
    private val numCols: Int,
    initialLives: Int
) {

    private var playerLane = numCols / 2
    private val map: Array<Array<CellType>> =
        Array(numRows) { Array(numCols) { CellType.EMPTY } }

    private var lives = initialLives
    //private var survivalPoints = 0
    var coinValue = GameConfig.COIN_VALUE
        private  set
    private var coins = 0
    private var distance = 0

    fun getPlayerLane(): Int = playerLane
    fun getLives(): Int = lives
    fun getMap(): Array<Array<CellType>> = map
    fun getScore(): Int = coins * coinValue
    fun getDistance(): Int = distance

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

    fun movePlayerToLane(lane: Int) {
        playerLane = lane.coerceIn(0, numCols - 1)
    }

    /**
     * One game tick:
     * - Move obstacles down
     * - Create at most one obstacle in the top row
     * - Check for collision
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
            map[0][col] = CellType.EMPTY
        }

        //Decide if create an obstacle or coin in this row
        val shouldCreateObstacle = (0..1).random() == 1 //50% chance
        if (shouldCreateObstacle) {
            val laneIndex = (0 until numCols).random()
            val isCoin = (0..4).random() == 0           //20% chance its a coin, otherwise is tree.
            if (isCoin) {
                map[0][laneIndex] = CellType.COIN
            }
            else {
                map[0][laneIndex] = CellType.TREE
            }
        }

        val result = checkCollision()
        distance++
        return result
    }

    //Checks collision on the last row and updates lives/score.
    private fun checkCollision(): TickResult {
        val lastRowIndex = numRows - 1
        val cell = map[lastRowIndex][playerLane]

        return when (cell){
            CellType.TREE -> {
                //player hits obstacle
                lives--
                map[lastRowIndex][playerLane] = CellType.EMPTY

                if (lives <= 0) {
                    TickResult.GAME_OVER
                }
                else {
                    TickResult.CRASH
                }
            }

            CellType.COIN -> {
                //Every tick we get a point
                //Player collects a coin
                coins++
                //Clean cell in the map
                map[lastRowIndex][playerLane] = CellType.EMPTY
                TickResult.NONE
            }

            CellType.EMPTY -> {
                //We didnt hit obstacle or coin
                TickResult.NONE
            }
        }
    }
}