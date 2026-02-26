package com.example.blindsudoku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<Button>(R.id.btnMode2).setOnClickListener { showDifficultySelector(2) }
        findViewById<Button>(R.id.btnMode3).setOnClickListener { showDifficultySelector(3) }
    }

    private fun showDifficultySelector(squareSize: Int) {
        val difficulties = arrayOf("Fácil", "Médio", "Difícil")
        val percentages = doubleArrayOf(0.4, 0.3, 0.15)

        MaterialAlertDialogBuilder(this)
            .setTitle("Selecione a Dificuldade")
            .setItems(difficulties) { _, which ->
                startGame(squareSize, percentages[which])
            }
            .show()
    }

    private fun startGame(squareSize: Int, difficulty: Double) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("SQUARE_SIZE", squareSize)
            putExtra("DIFFICULTY", difficulty)
        }
        startActivity(intent)
    }
}