package com.sccodesoft.dagorider;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class InTripActivity extends AppCompatActivity {

    TextView txtArrived;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_trip);

        txtArrived = (TextView)findViewById(R.id.txtArrived);

        if(getIntent()!=null && getIntent().getBooleanExtra("arrived",false)==true)
        {
            txtArrived.setText("You are in Trip..");
        }

    }
}
