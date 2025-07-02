package com.surfing.inthe.wavepark.ui.notifications

import android.graphics.Bitmap
import android.os.Bundle
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
import com.surfing.inthe.wavepark.data.model.Reservation
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.AndroidEntryPoint

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

@Composable
fun ReservationListScreen(viewModel: ReservationViewModel) {
    val allReservations by viewModel.reservations.collectAsState()
    val loading by viewModel.loading.collectAsState()

    // 필터: 오늘 이후 + 결제완료
    val today = remember { java.time.LocalDate.now() }
    val filtered = allReservations
        .filter { (it.date.isAfter(today) || it.date.isEqual(today)) && it.status == "결제완료" }
        .sortedBy { it.date }

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
                        modifier = Modifier.align(Alignment.CenterHorizontally)
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
                Text("상품명: ${reservation.product}")
                Text("수량: ${reservation.count}")
                Text("이용일자: ${reservation.date}")
                Text("예약일자: ${reservation.applyDate}")
            }
            Button(
                onClick = { showQr = true }
            ) {
                Text("QR")
            }
        }
        if (showQr) {
            QrDialog(qrContent = reservation.number, onDismiss = { showQr = false })
        }
    }
} 