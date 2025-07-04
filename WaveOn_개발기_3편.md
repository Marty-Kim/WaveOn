# WaveOn 앱 개발기 3편: 오프라인 지원과 사용자 경험 개선 – Room DB 적용기

안녕하세요, marty입니다.
이번 글에서는 WaveOn 앱의 오프라인 지원과 사용자 경험 개선을 위해 Room 데이터베이스를 도입하고, 주요 기능을 확장한 과정을 정리합니다.

---

## 오프라인 데이터의 필요성

앱을 종료 후 재실행할 때마다 데이터가 초기화되는 문제를 경험했습니다. 예약 내역 등 주요 정보가 매번 새로 로딩되어야 하는 불편함이 있었고, 오프라인 환경에서도 데이터를 확인할 수 있도록 데이터 지속성이 필요하다고 판단했습니다. Compose 기반 UI와의 결합을 고려할 때, 데이터의 안정적 관리가 더욱 중요해졌습니다.

---

## Room DB 설정 및 적용

### 의존성 추가

build.gradle.kts에 Room 관련 의존성을 추가하여 데이터베이스 환경을 구축했습니다.

```kotlin
val roomVersion = "2.6.1"
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")
```

---

### Entity 및 TypeConverter 구현

데이터베이스 테이블 구조는 Entity 클래스로 정의했습니다. Date 타입 등은 Room에서 직접 지원하지 않으므로 TypeConverter를 별도로 구현해 타입 변환을 처리했습니다.

```kotlin
@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reservationNumber: String,
    val sessionDate: Date,
    val sessionTime: String,
    val sessionType: String,
    val remainingSeats: Int,
    val totalSeats: Int,
    val price: Int,
    val status: String
)

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
```

---

### DAO (Data Access Object) 설계

Room의 DAO를 통해 데이터베이스 접근 인터페이스를 정의했습니다. Flow를 반환하도록 하여 데이터 변경을 실시간으로 관찰할 수 있도록 했습니다.

```kotlin
@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations ORDER BY sessionDate ASC")
    fun getAllReservations(): Flow<List<ReservationEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity): Long
    
    @Delete
    suspend fun deleteReservation(reservation: ReservationEntity)
}
```

---

## Repository 패턴과 Compose 연동

Repository 계층에서 Room DB와 연동하여, 도메인 모델과 Entity 간 변환을 명확히 분리했습니다. Flow를 활용해 Compose UI와 자연스럽게 연결할 수 있었습니다.

```kotlin
@Singleton
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao
) {
    fun getUpcomingReservations(): Flow<List<Reservation>> =
        reservationDao.getUpcomingReservations(Date()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    
    suspend fun insertReservation(reservation: Reservation): Long =
        reservationDao.insertReservation(reservation.toEntity())
}

@Composable
fun ReservationScreen(viewModel: ReservationViewModel) {
    val reservations by viewModel.reservations.collectAsState()
    LazyColumn {
        items(reservations) { reservation ->
            ReservationCard(reservation)
        }
    }
}
```

Entity와 Domain Model을 분리함으로써 데이터 계층과 비즈니스 로직의 독립성을 확보했습니다.

---

## Hilt를 통한 DI 모듈 구성

Room 데이터베이스와 DAO를 Hilt DI 모듈로 제공하여, 의존성 주입을 통해 각 계층의 결합도를 낮췄습니다.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WaveParkDatabase =
        WaveParkDatabase.getDatabase(context)
    
    @Provides
    @Singleton
    fun provideReservationDao(database: WaveParkDatabase): ReservationDao =
        database.reservationDao()
}
```

---

## 사용자 경험 개선 – QR 코드 및 로딩 피드백

예약 내역에 QR 코드를 추가하여 입장 절차를 간소화했습니다. ZXing 라이브러리를 활용해 QR 코드를 생성하고, Compose로 다이얼로그 UI를 구현했습니다.

```kotlin
val writer = QRCodeWriter()
val bitMatrix = writer.encode(reservationNumber, BarcodeFormat.QR_CODE, 200, 200)

@Composable
fun QRCodeDialog(reservationNumber: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("예약 QR 코드", style = MaterialTheme.typography.h6)
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = rememberQrBitmapPainter(reservationNumber),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
            }
        }
    }
}
```

네트워크 통신에는 타임아웃을 설정해 무한 로딩을 방지했고, 로딩 중에는 ProgressBar를 통해 사용자에게 명확한 피드백을 제공했습니다.

```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

---

## 주요 기능 및 기술적 성장

- 실시간 날씨/수온 정보 제공
- 예약 내역 실시간 크롤링 및 오프라인 데이터 캐싱
- QR 코드 생성 및 사용자 친화적 UI
- 안정적인 네트워크 처리와 로딩 피드백
- XML에서 Compose로의 전환, MVVM 아키텍처, Hilt DI, Room DB, Flow, 코루틴, WebView, Jsoup, StateFlow 등 다양한 기술의 실전 적용

---

## 다음 목표

- 빈자리 알림 푸시
- 카풀/커뮤니티 기능
- 관리자/운영자 기능
- 다국어 지원
- 테스트 코드 및 CI/CD

---

## 마치며

Compose, Flow, Room DB 등 현대적인 Android 개발 기술을 실제 서비스에 적용하며 데이터 관리와 사용자 경험의 중요성을 다시 한 번 확인할 수 있었습니다. 익숙한 환경에서 벗어나 새로운 기술을 도입하는 과정은 쉽지 않았지만, 그만큼 의미 있는 결과를 얻을 수 있었습니다.

---

**Tags:** #Android #Kotlin #Room #Database #QR코드 #성능최적화 #개발기 #완성 