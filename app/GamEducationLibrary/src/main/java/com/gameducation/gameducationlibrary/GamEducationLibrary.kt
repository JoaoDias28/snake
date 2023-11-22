package com.gameducation.gameducationlibrary

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import android.webkit.WebView
import androidx.core.app.ActivityCompat.recreate

class GamEducationLibrary(private val context: Context, private val webView: WebView) {

    private val accessCodeProcessor = AccessCodeProcessor(context,webView, this)

    private val handler = Handler()

    fun isSavedCodigoAcesso(context: Context):Boolean {
        if(!getCodigoAcesso(context).isNullOrEmpty()){
            Log.d("isSaved","true")
           return true
        }
        Log.d("isSaved","false")
            return false

    }
    private fun getCodigoAcesso(context: Context ): String {
        if (!AccessCodeManager.getAccessCode(context).isNullOrEmpty() ){
            Log.d("isSaved",AccessCodeManager.getAccessCode(context))
            return AccessCodeManager.getAccessCode(context)
        }
        return ""
    }

    var isAccessCodeValid: Boolean = false
        private set

    fun showAccessCodeInputPage(): Boolean {
        return accessCodeProcessor.showAccessCodeInputPage()

    }

    fun onAccessCodeProcessed(result: Boolean, accessCode: String) {
        if (result) {
            // Set the access code validity
            isAccessCodeValid = true

            Log.d("Library", "codigo acesso : "+accessCode.toString())
            AccessCodeManager.saveAccessCode(context,accessCode)




        } else {
            // Handle case where the access code is not valid
        }

    }


    private fun recreate() {
        // Add logic to close the current WebView and launch another asking for the code
        // You can use intents or any other navigation mechanism depending on your application structure
    }




}
