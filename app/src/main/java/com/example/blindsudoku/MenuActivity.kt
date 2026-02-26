package com.example.blindsudoku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        findViewById<Button>(R.id.btnMode2).setOnClickListener { startGame(2) }
        findViewById<Button>(R.id.btnMode3).setOnClickListener { startGame(3) }
    }

    private fun startGame(size: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("SQUARE_SIZE", size)
        }
        startActivity(intent)
    }
}