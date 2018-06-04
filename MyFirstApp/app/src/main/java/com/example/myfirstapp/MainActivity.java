package com.example.myfirstapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    boolean status = true;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick_btn_Change(View i)
    {

        TextView text = (TextView)findViewById(R.id.text_1);
        if (status)
        {
            text.setText("Changed Text");
            status = false;
        }
        else
        {
            text.setText("Changed Again");
            status = true;
        }

    }
}
