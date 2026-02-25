package com.example.blindsudoku

class SudokuGame(private val squareSize: Int) {
    val totalSize = squareSize * squareSize
    private val quadrantArray = Array(totalSize) { IntArray(totalSize) }

    fun setPlay(quadrantIndex: Int, innerIndex: Int, value: Int) {
        require(quadrantIndex in 0 until totalSize && innerIndex in 0 until totalSize) {
            "Index out of bounds. Quadrant: $quadrantIndex, Cell: $innerIndex"
        }

        require(value in 0..totalSize) {
            "Value $value is invalid for a $totalSize x $totalSize Sudoku."
        }

        quadrantArray[quadrantIndex][innerIndex] = value
    }

    fun getQuadrant(quadrantIndex: Int): IntArray {
        require(quadrantIndex in 0 until totalSize) {
            "Invalid quadrant index: $quadrantIndex"
        }

        return quadrantArray[quadrantIndex]
    }

    fun checkVictory(): Boolean {
        // Using HashSet because the add function returns 'false' if the same Object already exists
        val colSets = Array(totalSize) { HashSet<Int>() }
        val rowSets = Array(totalSize) { HashSet<Int>() }
        val quadrantSets = Array(totalSize) { HashSet<Int>() }

        for ((quadrantIndex, quadrant) in quadrantArray.withIndex()) {
            for ((innerIndex, value) in quadrant.withIndex()) {

                // Board is not completely filled
                if (value == 0) return false

                // Convert local quadrant indices to global row and column indices
                val globalRow = (quadrantIndex / squareSize) * squareSize + (innerIndex / squareSize)
                val globalCol = (quadrantIndex % squareSize) * squareSize + (innerIndex % squareSize)

                // Check for duplicates in the corresponding row, column, and quadrant
                if (!rowSets[globalRow].add(value)) return false
                if (!colSets[globalCol].add(value)) return false
                if (!quadrantSets[quadrantIndex].add(value)) return false
            }
        }
        return true
    }
}