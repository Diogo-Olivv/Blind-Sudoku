package com.example.blindsudoku

import kotlin.random.Random

class SudokuGame(private val squareSize: Int) {
    val totalSize = squareSize * squareSize
    private val quadrantArray = Array(totalSize) { IntArray(totalSize) }
    private val fixedCells = Array(totalSize) { BooleanArray(totalSize) }

    fun setPlay(quadrantIndex: Int, innerIndex: Int, value: Int) {
        if (fixedCells[quadrantIndex][innerIndex]) return // Prevent changing fixed cells

        require(quadrantIndex in 0 until totalSize && innerIndex in 0 until totalSize) {
            "Index out of bounds. Quadrant: $quadrantIndex, Cell: $innerIndex"
        }

        require(value in 0..totalSize) {
            "Value $value is invalid for a $totalSize x $totalSize Sudoku."
        }

        quadrantArray[quadrantIndex][innerIndex] = value
    }

    fun isFixed(quadrantIndex: Int, innerIndex: Int): Boolean {
        return fixedCells[quadrantIndex][innerIndex]
    }

    fun getQuadrant(quadrantIndex: Int): IntArray {
        require(quadrantIndex in 0 until totalSize) {
            "Invalid quadrant index: $quadrantIndex"
        }

        return quadrantArray[quadrantIndex]
    }

    fun generateLevel(percentage: Double) {
        val totalCells = totalSize * totalSize
        val cellsToFill = (totalCells * percentage).toInt()
        
        // Clear board
        for (i in 0 until totalSize) {
            quadrantArray[i].fill(0)
            fixedCells[i].fill(false)
        }

        var filled = 0
        val maxAttempts = totalCells * 10
        var attempts = 0

        while (filled < cellsToFill && attempts < maxAttempts) {
            attempts++
            val q = Random.nextInt(totalSize)
            val i = Random.nextInt(totalSize)
            
            if (quadrantArray[q][i] == 0) {
                val value = Random.nextInt(1, totalSize + 1)
                
                // Temporary set to check validity
                quadrantArray[q][i] = value
                if (isValidMove(q, i, value)) {
                    fixedCells[q][i] = true
                    filled++
                } else {
                    quadrantArray[q][i] = 0
                }
            }
        }
    }

    private fun isValidMove(quadrantIndex: Int, innerIndex: Int, value: Int): Boolean {
        val globalRow = (quadrantIndex / squareSize) * squareSize + (innerIndex / squareSize)
        val globalCol = (quadrantIndex % squareSize) * squareSize + (innerIndex % squareSize)

        // Check Row and Column (excluding current cell)
        for (q in 0 until totalSize) {
            for (i in 0 until totalSize) {
                if (q == quadrantIndex && i == innerIndex) continue
                
                val r = (q / squareSize) * squareSize + (i / squareSize)
                val c = (q % squareSize) * squareSize + (i % squareSize)
                
                if (r == globalRow || c == globalCol || q == quadrantIndex) {
                    if (quadrantArray[q][i] == value) return false
                }
            }
        }
        return true
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