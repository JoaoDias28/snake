package com.gameducation.gameducationlibrary

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import com.gameducation.gameducationlibrary.AccessCodeManager.clearAccessCode
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GamEducationLibrary(
    private val context: Context
) {
    interface AccessCodeCallback {
        fun onSuccess(accessCode: Boolean)
        fun onFailure()
    }

    interface QuestionCallback {
        fun onSuccess(result: Int)
    }

    private var lastQuestionResult: Int? = null
    private val accessCodeProcessor = AccessCodeProcessor(context, this)
    private val questionAndContentProcessor = QuestionAndContentProcessor(context, this)

    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()



    fun startPeriodicAccessCodeCheck(callback: (Boolean) -> Unit) {
        executor.scheduleAtFixedRate({
            isSavedAccessCode(context) { accessCodeValid ->
                callback(accessCodeValid)
            }
        }, 2, 60, TimeUnit.SECONDS)
    }

    val isAccessCodeValid: Boolean
        get() = !AccessCodeManager.getAccessCode(context).isNullOrEmpty()

    fun setupClearButton(button: Button, callback: () -> Unit) {
        button.setOnClickListener {
            clearAccessCode(context)
            callback()
        }
    }








    fun isSavedAccessCode(context: Context, callback: (Boolean) -> Unit) {
        val codigo = AccessCodeManager.getAccessCode(context)
        verifyAccessCode(codigo, object : AccessCodeCallback {
            override fun onSuccess(accessCodeValid: Boolean) {
                // Do something based on the access code validation result
                if (!accessCodeValid) {
                    clearAccessCode(context)
                    callback(false) // Invoke the callback with false if access code is not valid
                } else {
                    callback(true) // Invoke the callback with true if access code is valid
                }
                Log.d("AccessCodeValidation", "Access code $codigo is valid: $accessCodeValid")
            }

            override fun onFailure() {
                // Handle failure
                clearAccessCode(context)
                callback(false) // Invoke the callback with false if access code validation fails

                Log.e("AccessCodeValidation", "Failed to verify access code $codigo")
            }
        })

    }

    fun showQuestionPageAndAwait(local_jogo: String, webView: WebView, callback: QuestionCallback) {
        questionAndContentProcessor.showQuestionPageAndAwait(local_jogo, webView, object : QuestionAndContentProcessor.QuestionCallback {
            override fun onSuccess(result: Int) {
                callback.onSuccess(result)
            }
        })
    }

    fun showAccessCodeInputPage(webView: WebView, callback: AccessCodeCallback) {
        if(isAccessCodeValid){

            callback.onSuccess(true)
            return
        }
        accessCodeProcessor.showAccessCodeInputPageAndAwait(webView, object : AccessCodeProcessor.AccessCodeCallback {
            override fun onSuccess(result: Boolean, accessCode: String) {
                if (result) {
                    AccessCodeManager.saveAccessCode(context, accessCode)
                    callback.onSuccess(result)
                } else {
                    // Handle case where the access code is not valid

                    callback.onFailure()
                }
            }
            override fun onFailure() {
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
        })
    }

    fun verifyAccessCode(accessCode: String, callback: AccessCodeCallback) {
        val url = URL("http://10.0.2.2:80/framework/pedircodigoaccesso.php")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Set up the connection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.doOutput = true

            // Create form data
            val formData = "accessCode=$accessCode"

            // Write data to the connection
            val outputStream: OutputStream = BufferedOutputStream(connection.outputStream)
            outputStream.write(formData.toByteArray(StandardCharsets.UTF_8))
            outputStream.flush()

            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = BufferedReader(InputStreamReader(connection.inputStream))
                val response = inputStream.use(BufferedReader::readText)
                inputStream.close()

                // Parse the JSON response
                val jsonResponse = JSONObject(response)
                val success = jsonResponse.getBoolean("success")
                Log.d("AccessCodeValidation","success= "+ success.toString()+" response= "+response.toString())
                callback.onSuccess(success)
            } else {
                callback.onFailure()
            }
        } catch (e: Exception) {
            callback.onFailure()
            Log.e("AccessCodeValidation", "Exception: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

}
