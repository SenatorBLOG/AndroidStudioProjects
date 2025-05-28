package com.example.converterkotlin

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun convertCurrency (view: View){

        val editText = findViewById<EditText>(R.id.editTextNumber)
        val textView = findViewById<TextView>(R.id.textView)

        if (editText.text.toString().isNotEmpty()){
            val dollarValue = editText.text.toString().toFloat()
            val euroValue = dollarValue * 0.92f
            textView.text = euroValue.toString()
        }else{
            textView.text = getString(R.string.no_value_string)

        }
    }
}