# WaveOn 앱 개발기 3편: "오프라인 지원과 사용자 경험 개선 - Room DB 추가하기"

안녕하세요. Marty입니다. WaveOn 앱 개발기 마지막 편입니다.

이번 편에서는 앱을 더욱 안정적이고 사용자 친화적으로 만들어보겠습니다.

## 왜 Room DB가 필요했나요?

### 시행착오 8: 데이터 손실 문제
앱을 종료했다가 다시 열면 모든 데이터가 사라지는 문제가 있었어요. 사용자가 예약 내역을 확인하려고 하는데 매번 새로 로딩해야 하니까 불편했죠.

"오프라인에서도 데이터를 볼 수 있게 해야겠다!"라는 생각이 들었어요. 특히 Compose와 함께 사용할 때 데이터 지속성이 더욱 중요해졌어요.

## Room DB 설정하기

### 시행착오 9: 의존성 추가
먼저 build.gradle.kts에 Room 의존성을 추가해야 했습니다.

```kotlin
// Room Database
val roomVersion = "2.6.1"
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")
```

처음에는 ksp가 뭔지도 몰랐습니다. Kotlin Symbol Processing의 줄임말이라고 하더라고요. 컴파일 타임에 코드를 생성해주는 도구라고 하네요.

## Entity 클래스 만들기

데이터베이스에 저장할 테이블 구조를 정의해야 했어요.

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
```

### 시행착오 10: Date 타입 변환
Date 타입을 데이터베이스에 저장하려고 하는데 에러가 났습니다. Room에서는 Date를 직접 저장할 수 없다고 하더라고요.

```kotlin
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

이런 TypeConverter를 만들어야 했습니다. 처음에는 이게 뭔지도 몰랐는데, 나중에 보니 정말 유용한 기능이었습니다.

## DAO (Data Access Object) 만들기

데이터베이스에 접근하는 인터페이스를 만들어야 했어요.

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

### 시행착오 11: Flow 사용법
Flow가 뭔지도 몰랐는데, 구글링해보니 데이터베이스의 변화를 실시간으로 관찰할 수 있는 도구라고 하더라고요. 정말 신기했습니다.

## Repository 패턴 개선

기존 Repository를 Room DB와 연동하도록 수정했어요. Compose와 함께 사용할 때는 Flow가 정말 유용해요.

```kotlin
@Singleton
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao
) {
    fun getUpcomingReservations(): Flow<List<Reservation>> {
        return reservationDao.getUpcomingReservations(Date()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    suspend fun insertReservation(reservation: Reservation): Long {
        return reservationDao.insertReservation(reservation.toEntity())
    }
}

// Compose에서 사용
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

### 시행착오 12: Entity와 Domain Model 변환
Entity(데이터베이스 모델)와 Domain Model(비즈니스 로직 모델)을 분리해야 한다고 해서 처음에는 복잡하게 느껴졌습니다. 하지만 나중에 보니 정말 좋은 패턴이었습니다.

## Hilt DI 모듈 추가

데이터베이스와 DAO들을 Hilt로 주입받을 수 있도록 모듈을 만들었어요.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WaveParkDatabase {
        return WaveParkDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideReservationDao(database: WaveParkDatabase): ReservationDao {
        return database.reservationDao()
    }
}
```

## 사용자 경험 개선

### QR 코드 생성 기능 추가
예약 내역에 QR 코드를 추가해서 입장할 때 편리하게 했어요. Compose로 QR 코드 다이얼로그를 만들어보았어요.

```kotlin
// ZXing 라이브러리 사용
implementation("com.google.zxing:core:3.5.2")

// QR 코드 생성
val writer = QRCodeWriter()
val bitMatrix = writer.encode(reservationNumber, BarcodeFormat.QR_CODE, 200, 200)

// Compose에서 QR 코드 다이얼로그
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
                // QR 코드 이미지
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

### 시행착오 13: QR 코드 라이브러리 선택
QR 코드를 만들려고 하는데 어떤 라이브러리를 써야 할지 몰랐습니다. ZXing이 가장 유명하다고 해서 선택했는데, 정말 잘 작동했습니다.

## 성능 최적화

### 무한 로딩 방지
데이터를 가져올 때 무한 로딩이 걸리는 문제가 있었어요. 타임아웃을 설정해서 해결했어요.

```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()
```

### FAB ProgressBar 추가
로딩 중일 때 사용자에게 피드백을 주기 위해 ProgressBar를 추가했습니다.

## 최종 성취감

이제 앱이 정말 완성도 있어 보였습니다.

### 주요 기능들:
- 실시간 날씨/수온 정보
- 예약 내역 실시간 크롤링
- 오프라인 데이터 캐싱
- QR 코드 생성
- 사용자 친화적 UI
- 안정적인 네트워크 처리

## 개발하면서 배운 것들

### 기술적 성장:
- XML에서 Compose로의 성공적인 전환
- MVVM 아키텍처와 Compose의 완벽한 조합
- Hilt DI 완전 이해
- Room DB와 Flow를 활용한 데이터 캐싱
- 코루틴과 비동기 프로그래밍
- WebView와 Jsoup 크롤링
- StateFlow 상태 관리

### 개발자로서의 성장:
- 문제 해결 능력 향상
- 코드 구조화 능력
- 사용자 관점에서 생각하기
- 지속적인 학습과 개선

## 다음 목표

앞으로 추가하고 싶은 기능들:
- 빈자리 알림 푸시
- 카풀/커뮤니티 기능
- 관리자/운영자 기능
- 다국어 지원
- 테스트 코드 및 CI/CD

## 마무리

7년차 개발자로서 새로운 기술인 Compose를 도입하면서 많은 것을 배웠습니다. XML에서 Compose로의 전환은 처음에는 어려웠지만, 지금은 정말 만족스럽습니다.

경험 많은 개발자분들도 새로운 기술에 도전해보세요. 익숙한 것에서 벗어나는 건 항상 어렵지만, 새로운 가능성을 발견할 수 있습니다.

특히 Compose와 Flow, Room DB를 함께 사용하면서 현대적인 Android 개발의 진수를 경험할 수 있었습니다.

---

**Tags:** #Android #Kotlin #Room #Database #QR코드 #성능최적화 #개발기 #완성 