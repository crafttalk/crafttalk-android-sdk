package com.crafttalk.chat.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crafttalk.chat.presentation.UploadFileListener
import com.crafttalk.chat.utils.TypeFailUpload
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel: ViewModel() {

    fun handleUploadFile(uploadFileListener: UploadFileListener, responseCode: Int, responseMessage: String) {
        when (responseCode) {
            200 -> uploadFileListener.successUpload()
            500 -> uploadFileListener.failUpload(responseMessage, TypeFailUpload.NOT_SUPPORT_TYPE)
            408 -> uploadFileListener.failUpload(responseMessage, TypeFailUpload.TIMEOUT)
            413 -> uploadFileListener.failUpload(responseMessage, TypeFailUpload.LARGE)
            else -> uploadFileListener.failUpload(responseMessage, TypeFailUpload.DEFAULT)
        }
    }

    private fun launch(dispatcher: CoroutineDispatcher, action: suspend () -> Unit): Job {
        return viewModelScope.launch(dispatcher) {
            action()
        }
    }

    fun launchUI(action: suspend () -> Unit) {
        launch(Dispatchers.Main) {
            action()
        }
    }

    fun launchIO(action: suspend () -> Unit): Job {
        return launch(Dispatchers.IO) {
            action()
        }
    }

}