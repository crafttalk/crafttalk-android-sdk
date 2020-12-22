package com.crafttalk.chat.presentation.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel: ViewModel() {

    val exceptionType = MutableLiveData<String>()

    // используется для отправки на сервер информации о том, что конкретно пошло не так и в каком запросе.
    open fun handleError(throwable: Throwable) {
        when (throwable) {

        }
    }

    private fun launch(dispatcher: CoroutineDispatcher, action: suspend () -> Unit) {
        viewModelScope.launch(dispatcher) {
            try {
                action()
            } catch (throwable: Throwable) {
                handleError(throwable)
            }
        }
    }

    fun launchUI(action: suspend () -> Unit) {
        launch(Dispatchers.Main) {
            action()
        }
    }

    fun launchIO(action: suspend () -> Unit) {
        launch(Dispatchers.IO) {
            action()
        }
    }

}