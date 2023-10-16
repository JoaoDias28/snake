package com.GamEducation.snake;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;


public class GamEducationInterface {
    private Context context;


    public GamEducationInterface(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void onSubmissionComplete(float percentage, int correct, String local) {
        // Handle the data received from JavaScript
        // Here, you can close the WebView and process the data


        SharedPreferences sharedPreferences = context.getSharedPreferences("DadosGuardadosPeloJogo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("percentage", (float) percentage).apply();
        editor.putInt("correct", correct).apply();
        editor.putString("localJogo",local).apply();



        // Post the UI-related code to the main thread using a Handler
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // Close the WebView and recreate the activity on the main thread
                ((MainActivity) context).closeWebView();
                ((MainActivity) context).recreate();
            }
        });
    }
}
