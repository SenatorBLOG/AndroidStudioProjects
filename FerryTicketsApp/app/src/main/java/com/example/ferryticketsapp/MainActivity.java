package com.example.ferryticketsapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity {
    double costPerTicketToCatalina = 34.0;
    double cosstPerTicketToLongBeach = 40.0;
    int numberOfTickets;
    double totalCost;
    String tripChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        EditText tickets = findViewById(R.id.idTickets);
        Spinner destination = findViewById(R.id.spinnerDes);
        TextView result = findViewById(R.id.result);
        Button cost = findViewById(R.id.cost);

        cost.setOnClickListener(view -> {
            try{
                numberOfTickets = Integer.parseInt(tickets.getText().toString());
            } catch (NumberFormatException e){
                result.setText("Please enter a number of tickets.");
                return;
            }
            NumberFormat currency = NumberFormat.getCurrencyInstance();
            tripChoice = destination.getSelectedItem().toString();
            if(destination.getSelectedItemPosition() == 0){
                totalCost = numberOfTickets * costPerTicketToCatalina;
            } else {
                totalCost = numberOfTickets * cosstPerTicketToLongBeach;
            }
            result.setText("One way trip to " + tripChoice + " for" + numberOfTickets +
                    " tickets will cost " + currency.format(totalCost));
        });
        }
    }
