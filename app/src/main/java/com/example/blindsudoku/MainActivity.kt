package com.example.blindsudoku

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener

class MainActivity : AppCompatActivity() {

    private val squareSize = 2
    private var sudoku = SudokuGame(squareSize)

    private var currentQuadrantIndex = 0
    private var isUpdatingUI = false

    private lateinit var cellInputs: Array<EditText>
    private lateinit var quadrantTextView: TextView
    private lateinit var timer: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI Components
        val boardGrid = findViewById<GridLayout>(R.id.sudokuGrid)
        //val boardFullGrid = findViewById<GridLayout>(R.id.sudokuFullGrid)
        val btnShowBoard = findViewById<Button>(R.id.btnShowBoard)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnVerify = findViewById<Button>(R.id.btnVerify)
        val btnRestart = findViewById<Button>(R.id.btnRestart)

        quadrantTextView = findViewById(R.id.tvQuadrante)
        timer = findViewById(R.id.chronometerTimer)

        boardGrid.rowCount = squareSize
        boardGrid.columnCount = squareSize


        // --------- BUILDING THE QUADRANT ---------

        // Convert dp to px for dynamic sizing
        val density = resources.displayMetrics.density
        val cellSizePx = (80 * density).toInt()
        val marginPx = (2 * density).toInt()

        // Map and instantiate dynamic EditText cells
        cellInputs = Array(sudoku.totalSize) { cellIndex ->
            EditText(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSizePx
                    height = cellSizePx
                    setMargins(marginPx, marginPx, marginPx, marginPx)
                }
                setBackgroundColor(Color.WHITE)
                gravity = Gravity.CENTER
                inputType = InputType.TYPE_CLASS_NUMBER
                textSize = 24f

                addTextChangedListener { text ->
                    if (isUpdatingUI) return@addTextChangedListener

                    val typedValue = text.toString().toIntOrNull() ?: 0
                    sudoku.setPlay(currentQuadrantIndex, cellIndex, typedValue)
                }
            }
        }

        // Filter to prevent digits below 1 and above totalSize
        val sudokuInputFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.isEmpty()) return@InputFilter null
            val num = source.toString().toIntOrNull()
            if (num in 1..sudoku.totalSize) null else ""
        }

        // Attach cells to the grid and apply filters
        cellInputs.forEach { cell ->
            cell.filters = arrayOf(sudokuInputFilter, InputFilter.LengthFilter(1))
            boardGrid.addView(cell)
        }

        // --------- BUTTONS && TIMER ---------

        startTimer()

        btnPrev.setOnClickListener {
            currentQuadrantIndex = (currentQuadrantIndex - 1 + sudoku.totalSize) % sudoku.totalSize
            renderQuadrant()
        }
        btnNext.setOnClickListener {
            currentQuadrantIndex = (currentQuadrantIndex + 1) % sudoku.totalSize
            renderQuadrant()
        }
        btnShowBoard.setOnClickListener {
            showFullBoardVisual()
        }
        btnVerify.setOnClickListener {
            if (sudoku.checkVictory()) {
                timer.stop()
                val tempoDecorrido = timer.text
                Toast.makeText(this, "Victory in $tempoDecorrido! You solved it!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Not quite! Keep trying or fix the errors.", Toast.LENGTH_SHORT).show()
            }
        }
        btnRestart.setOnClickListener {
            restartGame()
        }

        renderQuadrant()
    }

    private fun startTimer() {
        timer.base = SystemClock.elapsedRealtime()
        timer.start()
    }

    private fun restartGame() {
        sudoku = SudokuGame(squareSize) // Reset Board
        currentQuadrantIndex = 0
        startTimer()
        renderQuadrant()
        Toast.makeText(this, "Game Restarted!", Toast.LENGTH_SHORT).show()
    }

    // --------- BOARD RENDERING ---------

    private fun renderQuadrant() {
        quadrantTextView.text = "Current Quadrant: ${currentQuadrantIndex + 1}"
        val quadrantData = sudoku.getQuadrant(currentQuadrantIndex)

        isUpdatingUI = true

        for (i in cellInputs.indices) {
            val value = quadrantData[i]
            cellInputs[i].setText(if (value == 0) "" else value.toString())
        }

        isUpdatingUI = false
    }

    private fun showFullBoardVisual() {
        val boardSize = sudoku.totalSize

        val density = resources.displayMetrics.density
        val smallCellPx = (30 * density).toInt()
        val normalMarginPx = (1 * density).toInt()
        val quadrantMarginPx = (3 * density).toInt()

        val popupGrid = GridLayout(this).apply {
            rowCount = boardSize
            columnCount = boardSize
            setBackgroundColor(Color.BLACK)
            setPadding(normalMarginPx, normalMarginPx, normalMarginPx, normalMarginPx)
        }

        for (globalRow in 0 until boardSize) {
            for (globalCol in 0 until boardSize) {
                val quadrantIndex = (globalRow / squareSize) * squareSize + (globalCol / squareSize)
                val innerIndex = (globalRow % squareSize) * squareSize + (globalCol % squareSize)
                val value = sudoku.getQuadrant(quadrantIndex)[innerIndex]

                val cellView = TextView(this).apply {
                    text = if (value == 0) "" else value.toString()
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setBackgroundColor(Color.WHITE)
                    setTextColor(Color.BLACK)

                    layoutParams = GridLayout.LayoutParams().apply {
                        width = smallCellPx
                        height = smallCellPx

                        val bottomMargin = if (globalRow % squareSize == squareSize - 1 && globalRow != boardSize - 1) quadrantMarginPx else normalMarginPx
                        val rightMargin = if (globalCol % squareSize == squareSize - 1 && globalCol != boardSize - 1) quadrantMarginPx else normalMarginPx

                        setMargins(normalMarginPx, normalMarginPx, rightMargin, bottomMargin)
                    }
                }
                popupGrid.addView(cellView)
            }
        }

        android.app.AlertDialog.Builder(this)
            .setTitle("Full Sudoku Map")
            .setView(popupGrid)
            .setPositiveButton("Close", null)
            .show()
    }
}