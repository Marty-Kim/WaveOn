# WaveOn 앱 개발기 2편: "실시간 데이터 연동하기 - API부터 WebView까지"

안녕하세요. Marty입니다. WaveOn 앱 개발기 2편입니다.

1편에서 Compose로 기본 UI를 만들었는데, 이제 실제 데이터를 가져와서 화면에 표시해보겠습니다.

## API 연동 시작하기

### 시행착오 3: Retrofit 설정
7년차 개발자라도 새로운 프로젝트에서는 항상 새로운 도전이 있습니다. 이번에는 Compose와 함께 사용할 데이터 흐름을 어떻게 구성할지 고민했습니다.

```kotlin
// 처음에 이렇게 했는데... 😅
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

이 코드가 뭔지도 모르고 그냥 복사해서 붙여넣었어요. 하지만 이게 나중에 정말 유용하게 쓰일 줄은 몰랐죠!

## 날씨 API 연동

기상청 API를 사용해서 실시간 날씨 정보를 가져오기로 했어요.

### 시행착오 4: API 응답 파싱
```kotlin
// 처음에는 이렇게 복잡하게 파싱했어요 😂
val sky = items.find { 
    it.category == "SKY" && it.fcstTime == targetFcstTime 
}?.fcstValue

val pty = items.find { 
    it.category == "PTY" && it.fcstTime == targetFcstTime 
}?.fcstValue
```

이런 식으로 하나씩 찾아서 파싱했는데, 나중에 보니 정말 비효율적이었습니다. 하지만 처음에는 이게 최선이었죠.

## 수온 API 연동

WavePark 근처 바다의 수온 정보도 가져와야 했어요. 서핑할 때 수온이 중요하거든요!

```kotlin
@GET("temperature")
suspend fun getWaterTemperature(): TemperatureResponse
```

이렇게 간단한 API 호출이었지만, 처음에는 suspend 함수가 뭔지도 몰랐습니다. 코루틴을 배우면서 비동기 프로그래밍의 세계에 발을 들이게 되었죠.

## WebView로 웹사이트 내장

### 시행착오 5: WebView 설정
WavePark 공식 웹사이트를 앱 안에 넣어야 했는데, 이것도 처음에는 정말 어려웠습니다.

```kotlin
webView.settings.apply {
    javaScriptEnabled = true
    domStorageEnabled = true
    setSupportZoom(true)
}
```

이런 설정들이 뭔지도 모르고 그냥 복사해서 붙여넣었습니다. 하지만 나중에 하나씩 이해하게 되면서 "아, 이게 이래서 필요한구나!" 하게 되었죠.

## Jsoup으로 예약 내역 크롤링

### 가장 어려웠던 부분

웹사이트에서 예약 내역을 가져와야 했는데, API가 없어서 웹페이지를 파싱해야 했습니다.

```kotlin
// 처음에는 이렇게 했는데...
val doc = Jsoup.connect(url)
    .userAgent("Mozilla/5.0")
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

이 코드가 처음에는 정말 마법처럼 보였습니다. HTML을 파싱해서 데이터를 추출한다는 게 신기했죠.

### 시행착오 6: 웹사이트 차단 우회
하지만 웹사이트에서 봇 접근을 차단하더라고요. 헤더를 커스터마이징해서 우회해야 했습니다.

```kotlin
val doc = Jsoup.connect(url)
    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .header("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3")
    .get()
```

이런 식으로 브라우저처럼 보이게 헤더를 설정했습니다.

## StateFlow로 실시간 데이터 관리

### 시행착오 7: Compose와 StateFlow 연동
기존에는 LiveData를 주로 사용했는데, Compose와 함께 사용할 때는 StateFlow가 훨씬 자연스러웠습니다.

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

LiveData에서 StateFlow로 전환하면서 데이터 흐름이 훨씬 명확해졌습니다.

## 첫 번째 성취감

날씨 정보, 수온 정보, 예약 내역이 모두 실시간으로 표시되었습니다. 특히 Compose와 StateFlow를 함께 사용하면서 데이터 바인딩이 훨씬 자연스러워졌습니다.

### 배운 것들:
- Compose와 StateFlow의 완벽한 호환성
- Retrofit + 코루틴으로 API 호출하기
- WebView 설정과 JavaScript 연동
- Jsoup으로 웹 크롤링
- 선언형 UI에서의 데이터 흐름 관리
- HTTP 헤더 커스터마이징

## 다음 편 예고

다음 편에서는 사용자 경험을 개선하고, 오프라인 지원을 위한 Room 데이터베이스를 추가해보겠습니다. 데이터 캐싱부터 시작해서 더 안정적인 앱을 만들어보겠습니다.

---

**Tags:** #Android #Kotlin #Retrofit #Jsoup #WebView #StateFlow #API연동 #개발기 