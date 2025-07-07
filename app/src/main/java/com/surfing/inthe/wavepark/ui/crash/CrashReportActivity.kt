package com.surfing.inthe.wavepark.ui.crash

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.surfing.inthe.wavepark.util.CrashReporter
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CrashReportActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrashReportScreen()
                }
            }
        }
    }
}

@Composable
fun CrashReportScreen(
    viewModel: CrashReportViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState :CrashReportUiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadCrashData(context)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 헤더
        Text(
            text = "크래시 리포트",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when (val currentState = uiState) {
            is CrashReportUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            is CrashReportUiState.Success -> {
                val crashData = currentState.crashData
                val crashFiles = currentState.crashFiles
                
                // 최근 크래시 정보
                if (crashData != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "최근 크래시",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text("크래시 ID: ${crashData.crashId}")
                            Text("발생 시간: ${formatTimestamp(crashData.timestamp)}")
                            Text("스레드: ${crashData.threadName}")
                            Text("예외 타입: ${crashData.exceptionType}")
                            Text("예외 메시지: ${crashData.exceptionMessage}")
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row {
                                Button(
                                    onClick = { viewModel.showCrashDetails(crashData) }
                                ) {
                                    Text("상세 보기")
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Button(
                                    onClick = { viewModel.shareCrashData(context, crashData) }
                                ) {
                                    Text("공유")
                                }
                            }
                        }
                    }
                }
                
                // 크래시 파일 목록
                if (crashFiles.isNotEmpty()) {
                    Text(
                        text = "크래시 로그 파일",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn {
                        items(crashFiles) { file ->
                            CrashFileItem(
                                file = file,
                                onItemClick = { viewModel.showCrashFile(context, file) },
                                onDeleteClick = { viewModel.deleteCrashFile(context, file) }
                            )
                        }
                    }
                }
                
                // 액션 버튼들
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.clearAllCrashData(context) }
                    ) {
                        Text("모든 로그 삭제")
                    }
                    
                    Button(
                        onClick = { viewModel.exportAllCrashData(context) }
                    ) {
                        Text("모든 로그 내보내기")
                    }
                }
                
                // 테스트 버튼 (개발용)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.triggerTestCrash(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("테스트 크래시 발생 (개발용)")
                }
            }
            
            is CrashReportUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("크래시 데이터를 불러오는데 실패했습니다: ${uiState.message}")
                }
            }
        }
    }
}

@Composable
fun CrashFileItem(
    file: File,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "크기: ${formatFileSize(file.length())} | 수정: ${formatTimestamp(file.lastModified())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onItemClick) {
                Text("보기")
            }
            
            IconButton(onClick = onDeleteClick) {
                Text("삭제")
            }
        }
    }
}

@Composable
fun CrashDetailsDialog(
    crashData: com.surfing.inthe.wavepark.util.CrashData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("크래시 상세 정보") },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = "=== 디바이스 정보 ===\n" +
                                "제조사: ${crashData.deviceInfo.manufacturer}\n" +
                                "모델: ${crashData.deviceInfo.model}\n" +
                                "Android 버전: ${crashData.deviceInfo.androidVersion}\n" +
                                "SDK 버전: ${crashData.deviceInfo.sdkVersion}\n" +
                                "앱 버전: ${crashData.deviceInfo.appVersion}\n\n" +
                                "=== 스택 트레이스 ===\n${crashData.stackTrace}\n\n" +
                                "=== 로그캣 ===\n${crashData.logcat}",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> "${size / (1024 * 1024)} MB"
    }
} 