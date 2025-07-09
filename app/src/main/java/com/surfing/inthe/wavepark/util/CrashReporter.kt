package com.surfing.inthe.wavepark.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.surfing.inthe.wavepark.BuildConfig
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class CrashReporter private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "CrashReporter"
        private const val PREFS_NAME = "crash_reporter_prefs"
        private const val KEY_LAST_CRASH = "last_crash_data"
        private const val KEY_CRASH_COUNT = "crash_count"
        private const val CRASH_LOG_DIR = "crash_logs"
        
        @Volatile
        private var INSTANCE: CrashReporter? = null
        
        fun getInstance(context: Context): CrashReporter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CrashReporter(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    init {
        setupUncaughtExceptionHandler()
    }
    
    private fun setupUncaughtExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val crashData = collectCrashData(thread, throwable)
                saveCrashData(crashData)
                Log.e(TAG, "Uncaught exception saved: ${crashData.crashId}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving crash data", e)
            } finally {
                // 기존 핸들러 호출 (앱 종료)
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
    
    private fun collectCrashData(thread: Thread, throwable: Throwable): CrashData {
        val crashId = generateCrashId()
        val timestamp = System.currentTimeMillis()
        
        val deviceInfo = collectDeviceInfo()
        val stackTrace = getStackTrace(throwable)
        val logcat = collectRecentLogcat()
        
        return CrashData(
            crashId = crashId,
            timestamp = timestamp,
            threadName = thread.name,
            exceptionType = throwable.javaClass.simpleName,
            exceptionMessage = throwable.message ?: "",
            deviceInfo = deviceInfo,
            stackTrace = stackTrace,
            logcat = logcat
        )
    }
    
    private fun collectDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            appVersion = BuildConfig.VERSION_NAME,
            appVersionCode = BuildConfig.VERSION_CODE
        )
    }
    
    private fun getStackTrace(throwable: Throwable): String {
        val stringWriter = java.io.StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        return stringWriter.toString()
    }
    
    private fun collectRecentLogcat(): String {
        return try {
            val process = Runtime.getRuntime().exec("logcat -d -t 1000")
            val inputStream = process.inputStream
            val scanner = Scanner(inputStream).useDelimiter("\\A")
            if (scanner.hasNext()) scanner.next() else ""
        } catch (e: Exception) {
            "Failed to collect logcat: ${e.message}"
        }
    }
    
    private fun saveCrashData(crashData: CrashData) {
        // SharedPreferences에 저장
        val json = crashData.toJson()
        prefs.edit()
            .putString(KEY_LAST_CRASH, json)
            .putInt(KEY_CRASH_COUNT, getCrashCount() + 1)
            .apply()
        
        // 파일로도 저장
        saveToFile(crashData)
    }
    
    private fun saveToFile(crashData: CrashData) {
        try {
            val crashDir = File(context.filesDir, CRASH_LOG_DIR)
            if (!crashDir.exists()) {
                crashDir.mkdirs()
            }
            
            val fileName = "crash_${crashData.crashId}_${dateFormat.format(Date(crashData.timestamp))}.txt"
            val crashFile = File(crashDir, fileName)
            
            FileWriter(crashFile).use { writer ->
                writer.write("=== CRASH REPORT ===\n")
                writer.write("Crash ID: ${crashData.crashId}\n")
                writer.write("Timestamp: ${Date(crashData.timestamp)}\n")
                writer.write("Thread: ${crashData.threadName}\n")
                writer.write("Exception: ${crashData.exceptionType}\n")
                writer.write("Message: ${crashData.exceptionMessage}\n\n")
                
                writer.write("=== DEVICE INFO ===\n")
                writer.write("Manufacturer: ${crashData.deviceInfo.manufacturer}\n")
                writer.write("Model: ${crashData.deviceInfo.model}\n")
                writer.write("Android Version: ${crashData.deviceInfo.androidVersion}\n")
                writer.write("SDK Version: ${crashData.deviceInfo.sdkVersion}\n")
                writer.write("App Version: ${crashData.deviceInfo.appVersion}\n")
                writer.write("App Version Code: ${crashData.deviceInfo.appVersionCode}\n\n")
                
                writer.write("=== STACK TRACE ===\n")
                writer.write(crashData.stackTrace)
                writer.write("\n\n")
                
                writer.write("=== LOGCAT ===\n")
                writer.write(crashData.logcat)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving crash file", e)
        }
    }
    
    fun getLastCrashData(): CrashData? {
        val json = prefs.getString(KEY_LAST_CRASH, null) ?: return null
        return try {
            CrashData.fromJson(json)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing crash data", e)
            null
        }
    }
    
    fun getCrashCount(): Int = prefs.getInt(KEY_CRASH_COUNT, 0)
    
    fun clearCrashData() {
        prefs.edit()
            .remove(KEY_LAST_CRASH)
            .putInt(KEY_CRASH_COUNT, 0)
            .apply()
        
        // 파일도 삭제
        val crashDir = File(context.filesDir, CRASH_LOG_DIR)
        if (crashDir.exists()) {
            crashDir.deleteRecursively()
        }
    }
    
    fun getAllCrashFiles(): List<File> {
        val crashDir = File(context.filesDir, CRASH_LOG_DIR)
        return if (crashDir.exists()) {
            crashDir.listFiles()?.filter { it.extension == "txt" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    private fun generateCrashId(): String {
        return UUID.randomUUID().toString().substring(0, 8)
    }
    
    // 테스트용 크래시 발생 메서드
    fun triggerTestCrash() {
        throw RuntimeException("테스트 크래시 - 개발자가 의도적으로 발생시킨 예외입니다.")
    }
}

data class CrashData(
    val crashId: String,
    val timestamp: Long,
    val threadName: String,
    val exceptionType: String,
    val exceptionMessage: String,
    val deviceInfo: DeviceInfo,
    val stackTrace: String,
    val logcat: String
) {
    fun toJson(): String {
        return """
        {
            "crashId": "$crashId",
            "timestamp": $timestamp,
            "threadName": "$threadName",
            "exceptionType": "$exceptionType",
            "exceptionMessage": "$exceptionMessage",
            "deviceInfo": {
                "manufacturer": "${deviceInfo.manufacturer}",
                "model": "${deviceInfo.model}",
                "androidVersion": "${deviceInfo.androidVersion}",
                "sdkVersion": ${deviceInfo.sdkVersion},
                "appVersion": "${deviceInfo.appVersion}",
                "appVersionCode": ${deviceInfo.appVersionCode}
            },
            "stackTrace": "${stackTrace.replace("\"", "\\\"")}",
            "logcat": "${logcat.replace("\"", "\\\"")}"
        }
        """.trimIndent()
    }
    
    companion object {
        fun fromJson(json: String): CrashData {
            // 간단한 JSON 파싱 (실제로는 Gson이나 Moshi 사용 권장)
            // 여기서는 기본적인 파싱만 구현
            return try {
                val crashId = json.substringAfter("\"crashId\": \"").substringBefore("\"")
                val timestamp = json.substringAfter("\"timestamp\": ").substringBefore(",").toLong()
                val threadName = json.substringAfter("\"threadName\": \"").substringBefore("\"")
                val exceptionType = json.substringAfter("\"exceptionType\": \"").substringBefore("\"")
                val exceptionMessage = json.substringAfter("\"exceptionMessage\": \"").substringBefore("\"")
                
                CrashData(
                    crashId = crashId,
                    timestamp = timestamp,
                    threadName = threadName,
                    exceptionType = exceptionType,
                    exceptionMessage = exceptionMessage,
                    deviceInfo = DeviceInfo("", "", "", 0, "", 0),
                    stackTrace = "",
                    logcat = ""
                )
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid JSON format", e)
            }
        }
    }
}

data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val appVersion: String,
    val appVersionCode: Int
) 