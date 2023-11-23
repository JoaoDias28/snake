package com.gameducation.gameducationlibrary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import androidx.core.app.ActivityCompat.recreate

class GamEducationLibrary(
    private val context: Context,
    private val webView: WebView,
    private val activity: Activity,
    private val accessCodeProcessedCallback: (Boolean) -> Unit
) {

    private val accessCodeProcessor = AccessCodeProcessor(context, webView, this)

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
    fun isSavedAccessCode(context: Context) :Boolean {
        if(AccessCodeManager.getAccessCode(context).isNullOrEmpty()){
            return false
        }
            return true
        }

    fun showAccessCodeInputPageAndAwait() {
        accessCodeProcessor.showAccessCodeInputPageAndAwait { result ->
            onAccessCodeProcessed(result, accessCodeProcessor.getAccessCode().toString())
            accessCodeProcessedCallback(result)
        }
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
