package com.boredomdenied.capstone;

import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import static com.boredomdenied.capstone.MainActivity.randomColor;
import static com.boredomdenied.capstone.MainActivity.randomNumber;
import static com.boredomdenied.capstone.Utils.randomColor;
import static com.boredomdenied.capstone.Utils.randomNumber;

public class FlashActivity extends AppCompatActivity {

    ImageView myImage;
    int randomNumber;
    int randomColor;
    int myColor;
    int myNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flasher);

        randomColor = (getIntent().getIntExtra("myColor", randomColor));
        randomNumber = getIntent().getIntExtra("myNumber", randomNumber);

        myImage = findViewById(R.id.myImageView);
        DrawableCompat.setTint(myImage.getDrawable(), ContextCompat.getColor(this, R.color.colorPrimaryDark));


        switch (randomNumber) {
            case 1:
                myNumber = R.drawable.ic_number1;
                break;
            case 2:
                myNumber = R.drawable.ic_number2;
                break;
            case 3:
                myNumber = R.drawable.ic_number3;
                break;
            case 4:
                myNumber = R.drawable.ic_number4;
                break;
            case 5:
                myNumber = R.drawable.ic_number5;
                break;
            case 6:
                myNumber = R.drawable.ic_number6;
                break;
            case 7:
                myNumber = R.drawable.ic_number7;
                break;
            case 8:
                myNumber = R.drawable.ic_number8;
                break;
            case 9:
                myNumber = R.drawable.ic_number9;
                break;
        }


        switch (randomColor) {
                case 1: myColor = R.color.colorRed;
                    break;
                case 2:  myColor = R.color.colorOrange;
                    break;
                case 3:  myColor = R.color.colorYellow;
                    break;
                case 4:  myColor = R.color.colorGreen;
                    break;
                case 5:  myColor = R.color.colorBlue;
                    break;
                case 6:  myColor = R.color.colorPurple;
                    break;
                case 7:  myColor = R.color.colorGray;
                    break;
                case 8:  myColor = R.color.colorBlack;
                    break;

            } 

                myImage.setImageResource(myNumber);
                DrawableCompat.setTint(myImage.getDrawable(), ContextCompat.getColor(getApplicationContext(), myColor));

            }


}