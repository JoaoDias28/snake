package com.gameducation.gameducationlibrary

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class QuestionAndContentProcessor(
    private val context: Context,
    private val library: GamEducationLibrary
) {
    private var webView: WebView? = null
    private lateinit var deferred: CompletableDeferred<Int> // Use Int or any appropriate type
    private var localJogo: String? = null

    interface QuestionCallback {
        fun onSuccess(result: Int)
    }

    fun showQuestionPageAndAwait(
        local_jogo: String,
        web_view: WebView,
        callback: QuestionCallback
    ) {
        localJogo = local_jogo
        webView = web_view
        deferred = CompletableDeferred()

        // Send access code and localJogo to the server to get the URL
        GetPerguntaOrContentAsyncTask().execute(localJogo)
        GlobalScope.launch(Dispatchers.Main) {
            val result = deferred.await()
            callback.onSuccess(result)
        }
    }

    private fun loadQuestionPage(url: String) {
        webView?.settings?.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView?.addJavascriptInterface(this, "QuestionAndContentProcessor")

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView?.loadUrl("javascript:QuestionAndContentProcessor.onPageLoaded();")
            }
        }

        // Load the received URL in the WebView
        webView?.loadUrl(url)
    }

    @JavascriptInterface
    fun onPageLoaded() {
        // This method is called when the WebView page has finished loading
        // You can add any logic here that needs to be executed when the page is loaded
    }

    @JavascriptInterface
    fun onSubmissionComplete(value: Int) {
        // Ensure UI-related operations are performed on the main thread
        GlobalScope.launch(Dispatchers.Main) {
            // Process the value received from the server
            Log.d("a", "Received value from server: $value")

            // Notify the completion callback
            deferred.complete(value)

            // Delay the destruction of the WebView until it's detached from the window
            delay(1000) // Adjust the delay as needed
            webView?.loadData("", "text/html", "utf-8")
            webView?.clearCache(true)
            webView?.clearHistory()
            webView?.destroy()
        }
    }

    private inner class GetPerguntaOrContentAsyncTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val localJogo = params[0]

            val serverUrl =
                "http://10.0.2.2:80/framework/getCodigoAcesso.php?codigoAcesso=${AccessCodeManager.getAccessCode(context)}&identificacaoJogo=$localJogo"

            try {
                val url = URL(serverUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                Log.d("QuestionAndContent", "GetPergunta " + response.toString())
                return response.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                return "Error: ${e.message}"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            try {
                val jsonResponse = JSONObject(result)
                Log.d("QuestionAndContent", jsonResponse.getString("redirectURL").toString())
                // Check if the JSON response contains a redirectURL
                if (jsonResponse.has("redirectURL")) {
                    Log.d("QuestionAndContent", jsonResponse.getString("redirectURL").toString())
                    val redirectURL =
                        "http://10.0.2.2:80/framework/" + jsonResponse.getString("redirectURL")

                    // Load the redirect URL in the WebView
                    loadQuestionPage(redirectURL)
                } else {
                    // Handle the case where there is no redirectURL in the response
                    // You can display an error message or take appropriate action
                    Log.d("QuestionAndContent", "else de ter rediredctUrl")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle JSON parsing or other exceptions here
                Log.d("QuestionAndContent", "exception")
            }
        }
    }
}
