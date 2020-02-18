package com.garate.tara_j;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.garate.tara_j.fragments.FaresFragment;

import java.text.DecimalFormat;

public class FareInfo extends AppCompatActivity {

    private TextView fare;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_info);

        fare = findViewById(R.id.fareText);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Double tempFare = MapsActivity.fare;
        DecimalFormat df = new DecimalFormat("###.00");
        String currentFare = df.format(tempFare);

        //OUTPUT
        fare.setText("PHP " + currentFare);
    }
}
