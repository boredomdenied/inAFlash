package com.boredomdenied.capstone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    private static final String PROBLEM_URL = "https://raw.githubusercontent.com/boredomdenied/inAFlash/master/problem.txt";
    private static final String SOLUTION_URL = "https://raw.githubusercontent.com/boredomdenied/inAFlash/master/solution.txt";
    private static final String WELCOME_URL = "https://raw.githubusercontent.com/boredomdenied/inAFlash/master/welcome.txt";

    TextView problemTextView;
    TextView solutionTextView;
    TextView welcomeTextView;
    Button welcomeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        problemTextView = findViewById(R.id.problemTextView);
        solutionTextView = findViewById(R.id.solutionTextView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeButton = findViewById(R.id.welcomeButton);

        new WelcomeAsyncTask(problemTextView).execute(PROBLEM_URL);
        new WelcomeAsyncTask(solutionTextView).execute(SOLUTION_URL);
        new WelcomeAsyncTask(welcomeTextView).execute(WELCOME_URL);

        welcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
