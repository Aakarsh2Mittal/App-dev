package com.example.potato3;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;
import android.widget.ImageView;

public class HomeActivity extends AppCompatActivity {
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        imageView = findViewById(R.id.imageView);
        Button instruct = findViewById(R.id.instruct);
        instruct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageView.setImageResource(R.drawable.instructions); // Replace "your_image" with your image resource
                if (imageView.getVisibility() == View.VISIBLE) {
                    imageView.setVisibility(View.GONE);
                } else {
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });


        // Button 1 Click Listener
        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivity1();
            }
        });

        // Button 2 Click Listener
        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivity2();
            }
        });

        // Button 3 Click Listener
        Button button3 = findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToActivity3();
            }
        });
    }

    // Method to go to Activity 1
    public void goToActivity1() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Method to go to Activity 2
    public void goToActivity2() {
        Intent intent = new Intent(this, PepperActivity.class);
        startActivity(intent);
    }

    // Method to go to Activity 3
    public void goToActivity3() {
        Intent intent = new Intent(this, TomatoActivity.class);
        startActivity(intent);
    }
}