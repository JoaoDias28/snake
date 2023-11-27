package com.gameducation.gameducationlibrary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import androidx.core.app.ActivityCompat.recreate
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class GamEducationLibrary(
    private val context: Context,
    private val webView: WebView,
    private val activity: Activity,
    private val accessCodeProcessedCallback: (Boolean) -> Unit ,
    private val questionAndContentCallback: (Int) -> Unit
) {
    private var lastQuestionResult: Int? = null


    private val accessCodeProcessor = AccessCodeProcessor(context, webView, this)
    private val questionAndContentProcessor = QuestionAndContentProcessor(context,webView,this)
    val isAccessCodeValid: Boolean
        get() = !AccessCodeManager.getAccessCode(context).isNullOrEmpty()

    // Method to setup a button to clear the access code
    fun setupClearButton(button: Button) {
        button.setOnClickListener {
            clearAccessCode()
            recreate(activity)
        }
    }

    // Method to clear the access code
    private fun clearAccessCode() {
        AccessCodeManager.clearAccessCode(context)
        // Perform any additional logic as needed after clearing the access code
    }

    fun isSavedAccessCode(context: Context): Boolean {
        if (AccessCodeManager.getAccessCode(context).isNullOrEmpty()) {
            return false
        }
        return true
    }

    fun showQuestionPageAndAwait(local_jogo: String) {
        // Remove this line: questionAndContentProcessor.showQuestionPageAndAwait { result ->
        // Add this line:
        questionAndContentProcessor.showQuestionPageAndAwait(questionAndContentCallback,local_jogo)
    }

    fun showAccessCodeInputPageAndAwait() {
        accessCodeProcessor.showAccessCodeInputPageAndAwait { result ->
            onAccessCodeProcessed(result, accessCodeProcessor.getAccessCode().toString())
            accessCodeProcessedCallback(result)
        }
    }

    fun onQuestionResultProcessed(result: Int){
        lastQuestionResult = result
    }
    fun onAccessCodeProcessed(result: Boolean, accessCode: String) {
        if (result) {

            Log.d("Library", "codigo acesso: $accessCode")
            AccessCodeManager.saveAccessCode(context, accessCode)
        } else {
            // Handle case where the access code is not valid
        }
    }

}

