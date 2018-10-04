package com.boredomdenied.capstone;

import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;


public class WelcomeAsyncTask extends AsyncTask<String, Integer, String> {

    private TextView textViewToSet;

    public WelcomeAsyncTask(TextView welcomeTextView) {
        this.textViewToSet = welcomeTextView;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        try {
            URL url = new URL(params[0]);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = null;

            while ((line = in.readLine()) != null) {
                //get lines
                result += line;
            }
            in.close();


        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        // Todo integrate gravity parameter
    }



    protected void onPreExecute() {
        //called before doInBackground() is started
    }

    @Override
    protected void onPostExecute(String result) {
        this.textViewToSet.setText(result);
    }
}
