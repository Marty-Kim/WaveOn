# Compose와 함께 사용하는 데이터 라이브러리 비교 (LiveData vs Flow)

안녕하세요. Marty입니다. 7년차 Android 개발자로 일하고 있습니다.

Android 개발을 하면서 가장 많이 고민하는 부분 중 하나가 바로 **상태 관리**입니다. 특히 Jetpack Compose를 사용하면서 LiveData와 Flow 중 어떤 것을 사용해야 할지 고민하신 분들이 많을 것입니다.

오늘은 제가 WaveOn 앱을 개발하면서 겪은 경험을 바탕으로 두 라이브러리를 비교해보겠습니다.

## 왜 이 글을 쓰게 되었나요?

7년간 주로 LiveData를 사용해왔는데, WaveOn 앱에서 Compose를 도입하면서 Flow로 마이그레이션하게 되었습니다. 그 과정에서 두 라이브러리의 차이점을 정말 많이 느꼈습니다.

## LiveData vs Flow 기본 개념

### LiveData란?
```kotlin
class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData
    
    fun fetchWeather() {
        viewModelScope.launch {
            val data = repository.getWeatherData()
            _weatherData.value = data
        }
    }
}
```

### Flow란?
```kotlin
class WeatherViewModel : ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()
    
    fun fetchWeather() {
        viewModelScope.launch {
            repository.getWeatherData().collect { data ->
                _weatherData.value = data
            }
        }
    }
}
```

## Compose에서의 사용법 비교

### LiveData 사용법
```kotlin
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.weatherData.observeAsState()
    
    weatherData?.let { data ->
        Text(text = "온도: ${data.temperature}°C")
        Text(text = "날씨: ${data.weatherStatus}")
    }
}
```

### Flow 사용법
```kotlin
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.weatherData.collectAsState()
    
    weatherData?.let { data ->
        Text(text = "온도: ${data.temperature}°C")
        Text(text = "날씨: ${data.weatherStatus}")
    }
}
```

## 실제 개발 경험담

### 시행착오 1: LiveData의 한계
7년간 LiveData를 사용해왔는데, Compose와 함께 사용하면서 몇 가지 한계점을 발견했습니다:

```kotlin
// 문제가 있던 코드
class ReservationRepository @Inject constructor() {
    private val _reservations = MutableLiveData<List<Reservation>>()
    val reservations: LiveData<List<Reservation>> = _reservations
    
    fun addReservation(reservation: Reservation) {
        val currentList = _reservations.value ?: emptyList()
        _reservations.value = currentList + reservation
    }
}

// Compose에서 사용할 때
@Composable
fun ReservationList(viewModel: ReservationViewModel) {
    val reservations by viewModel.reservations.observeAsState()
    // observeAsState()를 사용해야 하는 번거로움
}
```

**문제점:**
- LiveData는 단일 값만 저장할 수 있어서 리스트 업데이트가 복잡합니다
- 데이터 변환 작업이 어렵습니다
- 여러 데이터 소스를 조합하기 어렵습니다
- Compose에서 사용할 때 observeAsState() 변환이 필요합니다

### 시행착오 2: Flow로 마이그레이션
Flow로 바꾸면서 정말 편해졌습니다:

```kotlin
// 개선된 코드
class ReservationRepository @Inject constructor(
    private val reservationDao: ReservationDao
) {
    fun getUpcomingReservations(): Flow<List<Reservation>> {
        return reservationDao.getUpcomingReservations(Date()).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}

// Compose에서 사용할 때
@Composable
fun ReservationList(viewModel: ReservationViewModel) {
    val reservations by viewModel.reservations.collectAsState()
    // collectAsState()로 직접 사용 가능!
}
```

**장점:**
- 데이터 변환이 쉬워집니다 (map, filter, combine 등)
- 여러 데이터 소스를 쉽게 조합할 수 있습니다
- 실시간 데이터 스트림 처리에 최적화되어 있습니다
- Compose에서 collectAsState()로 직접 사용 가능합니다

## 성능 비교

### LiveData
- 메모리 효율적 (Observer 패턴)
- 생명주기 인식
- 단일 값만 처리 가능
- 복잡한 데이터 변환 어려움

### Flow
- 강력한 연산자들 (map, filter, combine, etc.)
- 여러 데이터 소스 조합 가능
- 코루틴과 완벽 호환
- 초기 학습 곡선이 있음

## 실제 사용 사례

### WaveOn 앱에서의 활용

**1. 날씨 데이터 실시간 업데이트**
```kotlin
// Flow 사용
fun getWeatherData(): Flow<WeatherData> {
    return weatherApiService.getWeather()
        .map { response -> response.toWeatherData() }
        .catch { error -> 
            emit(WeatherData.error(error.message))
        }
}
```

**2. 예약 내역 필터링**
```kotlin
// Flow의 강력한 연산자 활용
fun getUpcomingReservations(): Flow<List<Reservation>> {
    return reservationDao.getAllReservations()
        .map { reservations ->
            reservations.filter { it.date >= Date() }
                .sortedBy { it.date }
        }
}
```

**3. 여러 데이터 소스 조합**
```kotlin
// 날씨 + 수온 데이터 조합
fun getCombinedData(): Flow<CombinedData> {
    return combine(
        weatherRepository.getWeatherData(),
        temperatureRepository.getTemperatureData()
    ) { weather, temperature ->
        CombinedData(weather, temperature)
    }
}
```

## 언제 어떤 것을 사용할까요?

### LiveData를 사용하는 경우
- 간단한 UI 상태 관리
- 단일 값 업데이트
- 기존 View 시스템과 호환성 필요
- 빠른 프로토타이핑

### Flow를 사용하는 경우
- 복잡한 데이터 변환 필요
- 여러 데이터 소스 조합
- 실시간 데이터 스트림
- Compose와 함께 사용
- 장기적인 프로젝트

## 마이그레이션 팁

### LiveData → Flow 전환
```kotlin
// 기존 LiveData
private val _data = MutableLiveData<String>()

// Flow로 변경
private val _data = MutableStateFlow<String?>(null)
val data: StateFlow<String?> = _data.asStateFlow()
```

### Compose에서 사용
```kotlin
// LiveData
val data by viewModel.data.observeAsState()

// Flow
val data by viewModel.data.collectAsState()
```

## 결론

개인적인 추천: Flow를 사용하세요.

특히 Compose를 사용한다면 Flow가 훨씬 더 자연스럽고 강력합니다. 처음에는 조금 어려울 수 있지만, 한번 익숙해지면 정말 편리합니다.

### WaveOn 앱에서의 최종 선택
저는 WaveOn 앱에서 Flow를 선택했습니다. 그 이유는:
- 실시간 데이터 업데이트가 많아서
- 여러 API 응답을 조합해야 해서
- Compose와 함께 사용하기 때문에
- 향후 확장성을 고려해서

### 배운 점
- Flow의 강력한 연산자들을 활용하면 코드가 훨씬 깔끔해집니다
- 데이터 변환과 조합이 정말 쉬워집니다
- Compose와의 호환성이 완벽합니다

---

**Tags:** #Android #Kotlin #Compose #LiveData #Flow #상태관리 #개발팁 