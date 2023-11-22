package com.gameducation.gameducationlibrary

import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient

class AccessCodeProcessor(private val context: Context, private val webView: WebView, private val library: GamEducationLibrary) {

    private var isAccessCodeValid: Boolean = false

    fun showAccessCodeInputPage(): Boolean{

        webView.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView.addJavascriptInterface(this, "AccessCodeProcessor")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Handler().postDelayed({
                    webView.loadUrl("javascript:AccessCodeProcessor.onPageLoaded();")


                }, 1000)
            }
        }

        // Load the web page where users input the access code
        webView.loadUrl("http://10.0.2.2:80/framework/pedirCodigoAcesso.html")
       return isAccessCodeValid
    }

    @JavascriptInterface
    fun onPageLoaded() {
        // This method is called when the WebView page has finished loading
        // You can add any logic here that needs to be executed when the page is loaded
    }

    @JavascriptInterface
    fun onSubmissionComplete(accessCode: String) {
        Log.d("a",accessCode.toString())
        if (accessCode.isNullOrEmpty()) {
            isAccessCodeValid = false
            library.onAccessCodeProcessed(isAccessCodeValid,accessCode)
        }

        // Process the access code, update isAccessCodeValid, and trigger the library
        isAccessCodeValid = true
        library.onAccessCodeProcessed(isAccessCodeValid, accessCode)
    }
}