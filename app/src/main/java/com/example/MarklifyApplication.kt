package com.example

import android.app.Application
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.android.OpenCVLoader

object OpenCVState {
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    fun initialize(): Boolean {
        if (_isInitialized.value) return true
        val loaded = try {
            val localOk = OpenCVLoader.initLocal()
            Log.d("Marklify", "OpenCV loaded via initLocal: $localOk")
            localOk
        } catch (t: Throwable) {
            Log.w("Marklify", "OpenCVLoader.initLocal failed, trying initDebug: ${t.message}")
            try {
                OpenCVLoader.initDebug()
            } catch (e: Throwable) {
                Log.e("Marklify", "OpenCV initDebug failed: ${e.message}")
                false
            }
        }
        _isInitialized.value = loaded
        return loaded
    }
}

class MarklifyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OpenCVState.initialize()
    }
}
