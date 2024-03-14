package com.gameducation.gameducationlibrary

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

class AccessCodeProcessor(
    private val context: Context,
    private val library: GamEducationLibrary
) {
    private var accessCodeP: String? = null
    private var isAccessCodeValid: Boolean = false
    private lateinit var deferred: CompletableDeferred<String>

    interface AccessCodeCallback {
        fun onSuccess(result: Boolean, accessCode: String)
        fun onFailure()
    }

    fun showAccessCodeInputPageAndAwait(webView: WebView, callback: AccessCodeCallback) {
        deferred = CompletableDeferred()

        webView.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView.addJavascriptInterface(this, "AccessCodeProcessor")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.loadUrl("javascript:AccessCodeProcessor.onPageLoaded();")
            }
        }

        // Load the web page where users input the access code
        webView.loadUrl("http://10.0.2.2:80/framework/pedirCodigoAcesso.html")

        GlobalScope.launch(Dispatchers.Main) {
            val accessCode = deferred.await()
            callback.onSuccess(isAccessCodeValid, accessCode)
        }
    }

    @JavascriptInterface
    fun onPageLoaded() {
        // This method is called when the WebView page has finished loading
        // You can add any logic here that needs to be executed when the page is loaded
    }

    @JavascriptInterface
    fun onSubmissionComplete(accessCode: String) {
        // Ensure UI-related operations are performed on the main thread
        GlobalScope.launch(Dispatchers.Main) {
            if (accessCode.isNullOrEmpty()) {
                isAccessCodeValid = false
            } else {
                accessCodeP = accessCode
                isAccessCodeValid = true
            }
            deferred.complete(accessCode)
        }
    }
    @JavascriptInterface
     fun onSubmissionDenied() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            val alertDialogBuilder = AlertDialog.Builder(context)
            alertDialogBuilder.setTitle("Ocorreu um erro")
            alertDialogBuilder.setMessage("Comunique com o seu professor para continuar.")
            alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    fun getAccessCode(): String? {
        return accessCodeP
    }
}
