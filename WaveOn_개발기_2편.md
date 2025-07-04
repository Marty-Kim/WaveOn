# WaveOn 앱 개발기 2편: 실시간 데이터 연동의 실제 – API부터 WebView까지

안녕하세요, marty입니다.
이번 글에서는 WaveOn 앱에 실시간 데이터를 연동하는 과정을 정리해보려 합니다.
새로운 기술 스택(MVVM, Flow, Compose)을 적용하며 겪었던 시행착오와, 그 과정에서 얻은 인사이트를 공유합니다.

---

## 데이터 연동의 시작 – Retrofit 설정

새로운 프로젝트를 시작할 때마다 익숙한 라이브러리도 다시 한 번 점검하게 됩니다.
Retrofit과 OkHttp를 활용해 API 통신을 구성했습니다.
HttpLoggingInterceptor를 추가해 네트워크 통신을 투명하게 확인할 수 있도록 했고,
GsonConverterFactory로 데이터 파싱의 편의성도 챙겼습니다.

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    })
    .build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://apis.data.go.kr/")
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

---

## 날씨 API 연동 – 데이터 파싱의 고민

기상청 API를 통해 실시간 날씨 정보를 받아오는 과정에서는
카테고리별로 필요한 데이터를 추출하는 로직에 신경을 썼습니다.
처음에는 단순히 리스트에서 원하는 값을 찾는 방식으로 접근했지만,
점차 데이터 구조를 이해하고, 더 효율적인 파싱 방법을 고민하게 되었습니다.

```kotlin
val sky = items.find { 
    it.category == "SKY" && it.fcstTime == targetFcstTime 
}?.fcstValue

val pty = items.find { 
    it.category == "PTY" && it.fcstTime == targetFcstTime 
}?.fcstValue
```

---

## 수온 API 연동 – 코루틴과 비동기 처리

WavePark 인근의 바다 수온 정보도 앱에서 제공하고자 했습니다.
API 호출은 suspend 함수를 통해 코루틴 기반으로 처리했습니다.
비동기 프로그래밍의 장점을 살려, UI와 데이터 처리를 분리할 수 있었습니다.

```kotlin
@GET("temperature")
suspend fun getWaterTemperature(): TemperatureResponse
```

---

## WebView 내장 – 웹사이트와의 연결

WavePark 공식 웹사이트를 앱 내에서 바로 확인할 수 있도록 WebView를 구성했습니다.
JavaScript, DOM Storage, Zoom 등 필요한 설정을 꼼꼼히 적용해
웹 환경과 유사한 사용자 경험을 제공하고자 했습니다.

```kotlin
webView.settings.apply {
    javaScriptEnabled = true
    domStorageEnabled = true
    setSupportZoom(true)
}
```

---

## Jsoup을 활용한 예약 내역 크롤링

공식 API가 제공되지 않는 예약 내역은 Jsoup을 활용해 웹 크롤링 방식으로 처리했습니다.
HTML 구조를 파악하고, 필요한 데이터를 추출하는 과정에서
HTTP 헤더를 커스터마이징하여 접근 차단을 우회하는 등
실제 서비스 환경에서 마주칠 수 있는 다양한 상황을 경험할 수 있었습니다.

```kotlin
val doc = Jsoup.connect(url)
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .header("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3")
    .get()

val reservations = doc.select(".reservation-item")
    .map { element ->
        Reservation(
            number = element.select(".number").text(),
            date = element.select(".date").text(),
            // ...
        )
    }
```

---

## Compose와 StateFlow – 선언형 UI와 데이터 흐름

Compose와 StateFlow를 조합해
실시간 데이터가 자연스럽게 UI에 반영되도록 설계했습니다.
MutableStateFlow로 데이터를 관리하고,
collectAsState를 통해 Compose에서 손쉽게 상태를 구독할 수 있었습니다.

```kotlin
class ReservationRepository @Inject constructor() {
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    val reservations: StateFlow<List<Reservation>> = _reservations
}

// Compose에서 사용
@Composable
fun ReservationList(viewModel: ReservationViewModel) {
    val reservations by viewModel.reservations.collectAsState()
    
    LazyColumn {
        items(reservations) { reservation ->
            ReservationItem(reservation)
        }
    }
}
```

---

## 마치며

날씨, 수온, 예약 내역 등 다양한 실시간 데이터를 앱에 연동하며
MVVM, Flow, Compose 등 최신 기술 스택의 장점을 직접 체감할 수 있었습니다.
각 기술의 특성을 이해하고, 실제 서비스에 적용하는 과정에서
데이터 흐름과 UI의 결합이 얼마나 중요한지 다시 한 번 느꼈습니다.

다음 글에서는 오프라인 지원을 위한 Room 데이터베이스 적용과
데이터 캐싱 전략에 대해 다뤄볼 예정입니다.

---

**Tags:** #Android #Kotlin #Retrofit #Jsoup #WebView #StateFlow #MVVM #Compose #API연동 #개발기 