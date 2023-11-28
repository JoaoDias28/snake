package com.gameducation.gameducationlibrary

import android.app.Activity
import android.content.Context
import android.webkit.WebView
import android.widget.Button
import com.gameducation.gameducationlibrary.AccessCodeManager.clearAccessCode

class GamEducationLibrary(
    private val context: Context,
    private val activity: Activity
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

    val isAccessCodeValid: Boolean
        get() = !AccessCodeManager.getAccessCode(context).isNullOrEmpty()

    fun setupClearButton(button: Button) {
        button.setOnClickListener {
            clearAccessCode(context)
            activity.recreate()
        }
    }

    fun isSavedAccessCode(context: Context): Boolean {
        return !AccessCodeManager.getAccessCode(context).isNullOrEmpty()
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
        })
    }



}
