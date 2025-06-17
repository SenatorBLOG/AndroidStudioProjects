package com.example.tipcalculaator1;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity
            implements TextView.OnEditorActionListener, View.OnClickListener {
    private String billAmountString = "";
    private float tipPercent = .15f;
    private EditText billAmountEditText;
    private TextView percentTextView;
    private Button percentUpBtn;
    private Button percentDownBtn;
    private TextView tipTextView;
    private TextView totalTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //get references to UI control
        billAmountEditText = findViewById(R.id.billAmountEditText);
        percentTextView = findViewById(R.id.percentTextView);
        percentUpBtn = findViewById(R.id.percentUpBtn);
        percentDownBtn = findViewById(R.id.percentDownBtn);
        tipTextView = findViewById(R.id.tipTextView);
        totalTextView = findViewById(R.id.totalTextView);

        //set the listeners on build amount
        billAmountEditText.setOnEditorActionListener(this);
        percentDownBtn.setOnClickListener(this);
        percentUpBtn.setOnClickListener(this);

    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);

        outState.putString("billAmountString", billAmountString);
        outState.putFloat("tipPercent", tipPercent);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState != null) {
            billAmountString = savedInstanceState.getString("billAmountString","");
            tipPercent = savedInstanceState.getFloat("tipPercent", 0.15f);

            billAmountEditText.setText(billAmountString);
            calculateAndDisplay();
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.percentDownBtn){
            tipPercent = tipPercent - 0.01f;
            calculateAndDisplay();
        } else {
            tipPercent = tipPercent + 0.01f;
            calculateAndDisplay();
        }
    }

    private void calculateAndDisplay() {
        // get the bill amount
        billAmountString = billAmountEditText.getText().toString();
        float billAmount;
        if(billAmountString.isEmpty()) {
            billAmount = 0;
        } else {
            billAmount = Float.parseFloat(billAmountString);
        }
        // calculate tip and total
        float tipAmount = billAmount * tipPercent;
        float totalAmount = billAmount + tipAmount;

        // display the result with formating
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipTextView.setText(currency.format(tipAmount));
        totalTextView.setText(currency.format(totalAmount));

        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTextView.setText(percent.format(tipPercent));
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        calculateAndDisplay();
        return false;
    }
}