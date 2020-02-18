package com.garate.tara_j.model;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.garate.tara_j.R;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    public EditText searchBar;
    public Button searchButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        searchBar = findViewById(R.id.searchBar);
        searchButton = findViewById(R.id.findButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
                databaseAccess.open();

                String route = searchBar.getText().toString();
                ArrayList<Integer> id = databaseAccess.getDataID(route);
                ArrayList<Double> lat = databaseAccess.getDataLat(route);
                ArrayList<Double> lng = databaseAccess.getDataLng(route);

                databaseAccess.close();
            }
        });
    }
}
