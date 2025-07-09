package com.surfing.inthe.wavepark.ui.crash

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.surfing.inthe.wavepark.util.CrashData
import com.surfing.inthe.wavepark.util.CrashReporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class CrashReportViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<CrashReportUiState>(CrashReportUiState.Loading)
    val uiState: StateFlow<CrashReportUiState> = _uiState
    
    fun loadCrashData(context: Context) {
        viewModelScope.launch {
            try {
                val crashReporter = CrashReporter.getInstance(context)
                val crashData = crashReporter.getLastCrashData()
                val crashFiles = crashReporter.getAllCrashFiles()
                
                _uiState.value = CrashReportUiState.Success(
                    crashData = crashData,
                    crashFiles = crashFiles
                )
            } catch (e: Exception) {
                _uiState.value = CrashReportUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun showCrashDetails(crashData: CrashData) {
        // 다이얼로그 표시 로직은 UI에서 처리
    }
    
    fun shareCrashData(context: Context, crashData: CrashData) {
        val shareText = buildString {
            appendLine("=== 크래시 리포트 ===")
            appendLine("크래시 ID: ${crashData.crashId}")
            appendLine("발생 시간: ${formatTimestamp(crashData.timestamp)}")
            appendLine("스레드: ${crashData.threadName}")
            appendLine("예외 타입: ${crashData.exceptionType}")
            appendLine("예외 메시지: ${crashData.exceptionMessage}")
            appendLine()
            appendLine("=== 디바이스 정보 ===")
            appendLine("제조사: ${crashData.deviceInfo.manufacturer}")
            appendLine("모델: ${crashData.deviceInfo.model}")
            appendLine("Android 버전: ${crashData.deviceInfo.androidVersion}")
            appendLine("앱 버전: ${crashData.deviceInfo.appVersion}")
            appendLine()
            appendLine("=== 스택 트레이스 ===")
            appendLine(crashData.stackTrace)
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "WaveOn 앱 크래시 리포트")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        context.startActivity(Intent.createChooser(intent, "크래시 리포트 공유"))
    }
    
    fun showCrashFile(context: Context, file: File) {
        try {
            val content = file.readText()
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "크래시 로그: ${file.name}")
                putExtra(Intent.EXTRA_TEXT, content)
            }
            context.startActivity(Intent.createChooser(intent, "크래시 로그 공유"))
        } catch (e: Exception) {
            // 에러 처리
        }
    }
    
    fun deleteCrashFile(context: Context, file: File) {
        if (file.delete()) {
            loadCrashData(context) // 목록 새로고침
        }
    }
    
    fun clearAllCrashData(context: Context) {
        val crashReporter = CrashReporter.getInstance(context)
        crashReporter.clearCrashData()
        loadCrashData(context)
    }
    
    fun exportAllCrashData(context: Context) {
        val crashReporter = CrashReporter.getInstance(context)
        val crashFiles = crashReporter.getAllCrashFiles()
        
        if (crashFiles.isNotEmpty()) {
            val allContent = buildString {
                crashFiles.forEach { file ->
                    appendLine("=== ${file.name} ===")
                    appendLine(file.readText())
                    appendLine()
                }
            }
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "WaveOn 앱 모든 크래시 로그")
                putExtra(Intent.EXTRA_TEXT, allContent)
            }
            
            context.startActivity(Intent.createChooser(intent, "모든 크래시 로그 내보내기"))
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date(timestamp))
    }
    
    fun triggerTestCrash(context: Context) {
        val crashReporter = CrashReporter.getInstance(context)
        crashReporter.triggerTestCrash()
    }
}

sealed class CrashReportUiState {
    object Loading : CrashReportUiState()
    data class Success(
        val crashData: CrashData?,
        val crashFiles: List<File>
    ) : CrashReportUiState()
    data class Error(val message: String) : CrashReportUiState()
} 