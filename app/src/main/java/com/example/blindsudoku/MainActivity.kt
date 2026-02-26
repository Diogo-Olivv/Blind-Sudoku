package com.example.blindsudoku

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private var squareSize = 2
    private lateinit var sudoku: SudokuGame

    private var currentQuadrantIndex = 0
    private var isUpdatingUI = false

    private lateinit var cellInputs: Array<EditText>
    private lateinit var timer: Chronometer
    private lateinit var quadrantIndicatorGrid: GridLayout
    private lateinit var indicatorCells: Array<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Receber o tamanho do quadrado do Intent
        squareSize = intent.getIntExtra("SQUARE_SIZE", 2)
        sudoku = SudokuGame(squareSize)

        // UI Components
        val boardGrid = findViewById<GridLayout>(R.id.sudokuGrid)
        val btnShowBoard = findViewById<Button>(R.id.btnShowBoard)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnPrev = findViewById<Button>(R.id.btnPrev)
        val btnRestart = findViewById<Button>(R.id.btnRestart)
        
        quadrantIndicatorGrid = findViewById(R.id.quadrantIndicatorGrid)
        timer = findViewById(R.id.chronometerTimer)

        boardGrid.rowCount = squareSize
        boardGrid.columnCount = squareSize

        setupQuadrantIndicator()

        // --------- BUILDING THE QUADRANT ---------

        val density = resources.displayMetrics.density
        val cellSizeDp = when(squareSize) {
            1 -> 120
            2 -> 80
            else -> 60
        }
        val cellSizePx = (cellSizeDp * density).toInt()
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
                textSize = if (squareSize == 3) 20f else 24f

                addTextChangedListener { text ->
                    if (isUpdatingUI) return@addTextChangedListener

                    val typedValue = text.toString().toIntOrNull() ?: 0
                    sudoku.setPlay(currentQuadrantIndex, cellIndex, typedValue)
                    
                    checkAutoVictory()
                }
            }
        }

        val sudokuInputFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.isEmpty()) return@InputFilter null
            val num = source.toString().toIntOrNull()
            if (num in 1..sudoku.totalSize) null else ""
        }

        cellInputs.forEach { cell ->
            cell.filters = arrayOf(sudokuInputFilter, InputFilter.LengthFilter(if (sudoku.totalSize >= 10) 2 else 1))
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
        btnRestart.setOnClickListener {
            showRestartConfirmationDialog()
        }

        renderQuadrant()
    }

    private fun checkAutoVictory() {
        val currentData = sudoku.getQuadrant(currentQuadrantIndex)
        if (currentData.all { it != 0 }) {
            if (sudoku.checkVictory()) {
                timer.stop()
                val tempoDecorrido = timer.text
                MaterialAlertDialogBuilder(this)
                    .setTitle("Parabéns!")
                    .setMessage("Você venceu o Blind Sudoku em $tempoDecorrido!")
                    .setPositiveButton("Novo Jogo") { _, _ -> restartGame() }
                    .setNegativeButton("Fechar", null)
                    .show()
            }
        }
    }

    private fun setupQuadrantIndicator() {
        val density = resources.displayMetrics.density
        val indicatorSizePx = (15 * density).toInt()
        val marginPx = (2 * density).toInt()

        quadrantIndicatorGrid.rowCount = squareSize
        quadrantIndicatorGrid.columnCount = squareSize
        quadrantIndicatorGrid.removeAllViews()

        indicatorCells = Array(sudoku.totalSize) { i ->
            View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = indicatorSizePx
                    height = indicatorSizePx
                    setMargins(marginPx, marginPx, marginPx, marginPx)
                }
                setBackgroundColor(Color.WHITE)
            }
        }

        indicatorCells.forEach { quadrantIndicatorGrid.addView(it) }
    }

    private fun updateIndicatorUI() {
        indicatorCells.forEachIndexed { index, view ->
            if (index == currentQuadrantIndex) {
                view.setBackgroundColor(Color.GRAY) // Destaque para o quadrante atual
            } else {
                view.setBackgroundColor(Color.WHITE)
            }
        }
    }

    private fun startTimer() {
        timer.base = SystemClock.elapsedRealtime()
        timer.start()
    }

    private fun showRestartConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmar Reinício")
            .setMessage("Quer mesmo reiniciar o mapa?")
            .setPositiveButton("Sim") { _, _ ->
                restartGame()
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun restartGame() {
        sudoku = SudokuGame(squareSize)
        currentQuadrantIndex = 0
        startTimer()
        renderQuadrant()
        Toast.makeText(this, "Jogo Reiniciado!", Toast.LENGTH_SHORT).show()
    }

    // --------- BOARD RENDERING ---------

    private fun renderQuadrant() {
        updateIndicatorUI()
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
        
        val cellSideDp = when(boardSize) {
            1 -> 80
            4 -> 50
            9 -> 35
            else -> 30
        }
        val cellSide = (cellSideDp * density).toInt()
        val thinLine = (1 * density).toInt()
        val thickLine = (3 * density).toInt()

        val container = FrameLayout(this).apply {
            setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
        }

        val popupGrid = GridLayout(this).apply {
            rowCount = boardSize
            columnCount = boardSize
            setBackgroundColor(Color.DKGRAY)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        for (globalRow in 0 until boardSize) {
            for (globalCol in 0 until boardSize) {
                val quadrantIndex = (globalRow / squareSize) * squareSize + (globalCol / squareSize)
                val innerIndex = (globalRow % squareSize) * squareSize + (globalCol % squareSize)
                val value = sudoku.getQuadrant(quadrantIndex)[innerIndex]

                val cellView = TextView(this).apply {
                    text = if (value == 0) "" else value.toString()
                    textSize = if (boardSize > 4) 12f else 16f
                    setTextColor(Color.BLACK)
                    gravity = Gravity.CENTER
                    setBackgroundColor(Color.WHITE)

                    layoutParams = GridLayout.LayoutParams().apply {
                        width = cellSide
                        height = cellSide
                        val right = if ((globalCol + 1) % squareSize == 0 && globalCol != boardSize - 1) thickLine else thinLine
                        val bottom = if ((globalRow + 1) % squareSize == 0 && globalRow != boardSize - 1) thickLine else thinLine
                        setMargins(0, 0, right, bottom)
                    }
                }
                popupGrid.addView(cellView)
            }
        }

        container.addView(popupGrid)

        MaterialAlertDialogBuilder(this)
            .setTitle("Mapa Completo")
            .setView(container)
            .setPositiveButton("Fechar", null)
            .show()
    }
}