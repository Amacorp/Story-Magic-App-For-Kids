package com.storymagic.kids.domain

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized logging system for the app.
 * Stores logs in memory and persists to file.
 */
object LogManager {

    private const val TAG = "StoryMagicLog"
    private const val MAX_ENTRIES = 1000
    private const val LOG_FILE_NAME = "storymagic_log.txt"

    data class LogEntry(
        val timestamp: Long,
        val tag: String,
        val level: String,
        val message: String,
        val threadName: String = Thread.currentThread().name
    )

    private val _logs = mutableListOf<LogEntry>()
    val logs: List<LogEntry> get() = _logs.toList()

    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun initialize(filesDir: File) {
        logFile = File(filesDir, LOG_FILE_NAME)
        loadLogsFromFile()
        log("LogManager", "INFO", "LogManager initialized with ${_logs.size} existing entries")
        log("LogManager", "INFO", "Device: Android ${android.os.Build.VERSION.RELEASE}, Model: ${android.os.Build.MODEL}")
        log("LogManager", "INFO", "App Version: 1.0.0 (${android.os.Build.VERSION.SDK_INT})")
    }

    fun log(tag: String, level: String, message: String) {
        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            tag = tag,
            level = level,
            message = message,
            threadName = Thread.currentThread().name
        )

        // Add to in-memory list
        synchronized(_logs) {
            _logs.add(entry)
            // Trim if exceeds max
            while (_logs.size > MAX_ENTRIES) {
                _logs.removeAt(0)
            }
        }

        // Log to Android Logcat
        when (level) {
            "DEBUG" -> Log.d(tag, "[$level] $message")
            "INFO" -> Log.i(tag, "[$level] $message")
            "WARN" -> Log.w(tag, "[$level] $message")
            "ERROR" -> Log.e(tag, "[$level] $message")
            else -> Log.d(tag, "[$level] $message")
        }

        // Persist to file asynchronously
        saveLogToFile(entry)
    }

    fun logApiRequest(tag: String, url: String, model: String, promptLength: Int, apiKeyPrefix: String) {
        log(tag, "INFO", "=== API REQUEST ===")
        log(tag, "INFO", "URL: $url")
        log(tag, "INFO", "Model: $model")
        log(tag, "INFO", "Prompt length: $promptLength characters")
        log(tag, "INFO", "API Key: Bearer ${apiKeyPrefix}...")
        log(tag, "INFO", "Timestamp: ${fullDateFormat.format(Date())}")
    }

    fun logApiResponse(tag: String, statusCode: Int, responseTimeMs: Long, responseBodyLength: Int) {
        log(tag, "INFO", "=== API RESPONSE ===")
        log(tag, "INFO", "Status Code: $statusCode")
        log(tag, "INFO", "Response Time: ${responseTimeMs}ms")
        log(tag, "INFO", "Response Body Length: $responseBodyLength characters")
        if (statusCode != 200) {
            log(tag, "ERROR", "API request failed with status code: $statusCode")
        }
    }

    fun logApiError(tag: String, errorCode: Int, errorBody: String?, exception: Exception? = null) {
        log(tag, "ERROR", "=== API ERROR ===")
        log(tag, "ERROR", "Error Code: $errorCode")
        log(tag, "ERROR", "Error Body: ${errorBody ?: "null"}")
        if (exception != null) {
            log(tag, "ERROR", "Exception: ${exception.javaClass.simpleName}: ${exception.message}")
            log(tag, "ERROR", "Stack trace: ${android.util.Log.getStackTraceString(exception)}")
        }
    }

    fun getFormattedLogs(): String {
        synchronized(_logs) {
            return _logs.joinToString("\n") { entry ->
                "[${dateFormat.format(Date(entry.timestamp))}] [${entry.level}] [${entry.tag}] [${entry.threadName}] ${entry.message}"
            }
        }
    }

    fun clearLogs() {
        synchronized(_logs) {
            _logs.clear()
        }
        logFile?.writeText("")
        log("LogManager", "INFO", "Logs cleared")
    }

    fun getLogCount(): Int = synchronized(_logs) { _logs.size }

    suspend fun saveLogsToFile() = withContext(Dispatchers.IO) {
        logFile?.writeText(getFormattedLogs())
    }

    private fun loadLogsFromFile() {
        try {
            logFile?.let { file ->
                if (file.exists()) {
                    val content = file.readText()
                    if (content.isNotBlank()) {
                        // Parse existing logs if needed, or just start fresh
                        _logs.clear()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading logs from file", e)
        }
    }

    private fun saveLogToFile(entry: LogEntry) {
        try {
            val logLine = "[${dateFormat.format(Date(entry.timestamp))}] [${entry.level}] [${entry.tag}] ${entry.message}\n"
            logFile?.appendText(logLine)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving log to file", e)
        }
    }

    fun getLogsByLevel(level: String): List<LogEntry> {
        return synchronized(_logs) {
            _logs.filter { it.level == level }
        }
    }

    fun getRecentLogs(count: Int): List<LogEntry> {
        return synchronized(_logs) {
            _logs.takeLast(count)
        }
    }
}
