package com.gameducation.gameducationlibrary

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import java.util.concurrent.CountDownLatch
class AccessCodeProcessor(
    private val context: Context,
    private val webView: WebView,
    private val library: GamEducationLibrary
) {
    private var accessCodeP: String? = null
    private var isAccessCodeValid: Boolean = false
    private var completionCallback: ((Boolean) -> Unit)? = null

    fun showAccessCodeInputPageAndAwait(callback: (Boolean) -> Unit) {
        completionCallback = callback

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
    }

    @JavascriptInterface
    fun onPageLoaded() {
        // This method is called when the WebView page has finished loading
        // You can add any logic here that needs to be executed when the page is loaded
    }

    @JavascriptInterface
    fun onSubmissionComplete(accessCode: String) {
        // Ensure UI-related operations are performed on the main thread
        Handler(Looper.getMainLooper()).post {
            Log.d("a", accessCode.toString())
            if (accessCode.isNullOrEmpty()) {
                library.onAccessCodeProcessed(isAccessCodeValid, accessCode)
            }
         accessCodeP = accessCode
            // Process the access code, update isAccessCodeValid, and trigger the library
            isAccessCodeValid = true
            library.onAccessCodeProcessed(isAccessCodeValid, accessCode)

            // Notify the completion callback
            completionCallback?.invoke(isAccessCodeValid)
        }
    }

    fun getAccessCode(): String? {
        return accessCodeP
    }
}
