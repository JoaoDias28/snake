package com.gameducation.gameducationlibrary

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
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
    private var accessCode: String? = null
    private var localJogo: String? = null
    private var completionCallback: ((Int) -> Unit)? = null

    fun showQuestionPageAndAwait(callback: (Int) -> Unit, local_jogo: String, web_view: WebView) {
        completionCallback = callback
        localJogo = local_jogo
        accessCode = AccessCodeManager.getAccessCode(context)
        webView = web_view

        // Send access code and localJogo to the server to get the URL
        GetPerguntaOrContentAsyncTask().execute(accessCode, localJogo)
    }

    private fun loadQuestionPage(url: String) {
        webView?.settings?.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        webView?.addJavascriptInterface(this, "QuestionAndContentProcessor")

        webView?.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                Handler().postDelayed({
                    webView?.loadUrl("javascript:QuestionAndContentProcessor.onPageLoaded();")
                }, 1000)
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
        Handler(Looper.getMainLooper()).post {
            // Process the value received from the server
            Log.d("a", "Received value from server: $value")

            // Notify the completion callback
            completionCallback?.invoke(value)
            library.onQuestionResultProcessed(value)
        }
    }

    private inner class GetPerguntaOrContentAsyncTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg params: String?): String {
            val codigoAcesso = params[0]
            val localJogo = params[1]

            val serverUrl = "http://10.0.2.2:80/framework/getCodigoAcesso.php?codigoAcesso=$codigoAcesso&identificacaoJogo=$localJogo"

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
                Log.d("QuestionAndContent","GetPergunta "+response.toString())
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
                Log.d("QuestionAndContent",jsonResponse.getString("redirectURL").toString())
                // Check if the JSON response contains a redirectURL
                if (jsonResponse.has("redirectURL")) {
                    Log.d("QuestionAndContent",jsonResponse.getString("redirectURL").toString())
                    val redirectURL = "http://10.0.2.2:80/framework/" + jsonResponse.getString("redirectURL")

                    // Load the redirect URL in the WebView
                    loadQuestionPage(redirectURL)
                } else {
                    // Handle the case where there is no redirectURL in the response
                    // You can display an error message or take appropriate action
                    Log.d("QuestionAndContent","else de ter rediredctUrl")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle JSON parsing or other exceptions here
                Log.d("QuestionAndContent","exception")
            }
        }
    }
}
