package com.surfing.inthe.wavepark.ui.notifications

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.surfing.inthe.wavepark.data.model.Reservation
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.surfing.inthe.wavepark.util.ApiConfig
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ReservationListActivity : ComponentActivity() {
    private val viewModel: ReservationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReservationListScreen(viewModel)
        }
    }
}


fun isAfterToday(dateMillis: Date): Boolean {
    // millis를 LocalDate로 변환 (시스템 타임존 기준)
    val inputDate = Instant.ofEpochMilli(dateMillis.time)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    // 오늘 날짜 구하기
    val today = LocalDate.now()
    // 오늘 이후면 true
    return inputDate.isBefore(today)
}
@Composable
fun ReservationListScreen(viewModel: ReservationViewModel) {
    val allReservations by viewModel.reservations.collectAsStateWithLifecycle()
    val loading by viewModel.loading.collectAsStateWithLifecycle()


    Log.d("예약 뷰" , "size : ${    allReservations.size} ")

    // 필터: 오늘 이후 + 확인된 예약
    val today = remember { Date().apply { hours = 0 }}
    val filtered = allReservations
        .filter {
            Log.d("예약 뷰" , "it.status : ${    it.status} date : ${it.sessionDate}")
            !(isAfterToday(it.sessionDate)) && (it.status == "confirmed" || it.status == "pending") }
        .sortedBy { it.sessionDate }

    Scaffold(
        topBar = {
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            if (loading) {
                Column(
                    modifier = Modifier.align ( Alignment.Center )

                ) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = "예약 정보를 가져오는 중입니다.")
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 20.dp)
                    )
                }
            }else{
                if (filtered.isNullOrEmpty()){
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(20.dp),
                        text = "예약 정보가 없어요!")
                }

            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { reservation ->
                    ReservationItem(reservation)
                }
            }
        }
    }
}

// QR 코드 생성 함수
fun generateQrCodeBitmap(content: String, size: Int = 400): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bmp
}

@Composable
fun QrDialog(qrContent: String, onDismiss: () -> Unit) {
    val qrBitmap = remember(qrContent) { generateQrCodeBitmap(qrContent) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("예약 QR 코드") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(240.dp)
                )
//                Text(qrContent, modifier = Modifier.padding(top = 8.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        }
    )
}

@Composable
fun ReservationItem(reservation: Reservation) {
    var showQr by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("세션 타입: ${reservation.sessionType}")
//                Text("세션 시간: ${reservation.sessionTime}")
                Text("이용일자: ${dateFormatter.format(reservation.sessionDate)}")
//                Text("잔여 좌석: ${reservation.remainingSeats}/${reservation.totalSeats}")
//                Text("가격: ${reservation.price}원")
                Text("상태: ${reservation.status}")
            }
            Button(
                onClick = { showQr = true }
            ) {
                Text("QR")
            }
        }
        if (showQr) {
            if (reservation.reservationNumber.contains("re=ini")){
                val toast = Toast(LocalContext.current)
                toast.setText("지원되지 않는 티켓입니다")
                toast.show()
                val url = "${ApiConfig.WAVEPARK_BASE_URL}/mypage/orderview/${reservation.reservationNumber}"
                val inte = Intent(ACTION_VIEW,Uri.parse(url))
                LocalContext.current.startActivity(inte)
            }else{
                QrDialog(qrContent = reservation.reservationNumber, onDismiss = { showQr = false })
            }
        }
    }
} 