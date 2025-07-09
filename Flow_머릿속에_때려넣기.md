# Flow 머릿속에 때려넣기 - 실전 활용 가이드

안녕하세요. Marty입니다. 7년차 Android 개발자로 일하고 있습니다. 이번 글에서는 **Kotlin Flow**를 완전히 이해하고 실전에서 활용할 수 있도록 도와드리겠습니다.

WaveOn 앱을 개발하면서 Flow를 정말 많이 사용했는데, 7년간 LiveData만 사용해왔던 저에게는 처음에는 정말 어려웠습니다. 하지만 지금은 정말 유용한 도구가 되었습니다.

## Flow가 뭔가요?

Flow는 **비동기 데이터 스트림**을 처리하는 Kotlin의 라이브러리입니다. 쉽게 말해서 "시간이 지나면서 계속해서 데이터가 흘러나오는 것"을 처리하는 도구입니다.

### 기본 개념
```kotlin
// Flow는 데이터를 "흘려보내는" 파이프라인
val flow = flow {
    emit(1)  // 데이터를 흘려보냄
    delay(1000)
    emit(2)  // 또 다른 데이터를 흘려보냄
    delay(1000)
    emit(3)  // 계속해서...
}
```

## Flow의 핵심 구성요소

### 1. Flow Builder
```kotlin
// 1. flow { } - 가장 기본적인 Flow 생성
val basicFlow = flow {
    for (i in 1..5) {
        emit(i)
        delay(100)
    }
}

// 2. flowOf() - 고정된 값들로 Flow 생성
val fixedFlow = flowOf(1, 2, 3, 4, 5)

// 3. asFlow() - 컬렉션을 Flow로 변환
val listFlow = listOf(1, 2, 3, 4, 5).asFlow()

// 4. callbackFlow - 콜백 기반 API를 Flow로 변환
val callbackFlow = callbackFlow {
    val callback = object : ApiCallback {
        override fun onData(data: String) {
            trySend(data)
        }
    }
    api.registerCallback(callback)
    
    awaitClose { api.unregisterCallback(callback) }
}
```

### 2. StateFlow vs SharedFlow
```kotlin
// StateFlow - 현재 상태를 유지하는 Flow
val stateFlow = MutableStateFlow(0)

// SharedFlow - 이벤트를 발생시키는 Flow
val sharedFlow = MutableSharedFlow<String>()
```

## 실전 활용 사례

### 1. API 호출 결과 처리
```kotlin
// WaveOn 앱에서 실제 사용한 코드
fun fetchWeatherData(): Flow<WeatherResult> = flow {
    emit(WeatherResult.Loading)  // 로딩 상태
    
    try {
        val response = weatherApiService.getWeather()
        val weatherData = response.toWeatherData()
        emit(WeatherResult.Success(weatherData))
    } catch (e: Exception) {
        emit(WeatherResult.Error(e.message))
    }
}
```

### 2. 데이터베이스 실시간 감지
```kotlin
// Room DB와 함께 사용
@Query("SELECT * FROM reservations ORDER BY date ASC")
fun getAllReservations(): Flow<List<ReservationEntity>>

// Repository에서 사용
fun getUpcomingReservations(): Flow<List<Reservation>> {
    return reservationDao.getAllReservations()
        .map { entities -> entities.map { it.toDomainModel() } }
        .map { reservations -> 
            reservations.filter { it.date >= Date() }
        }
}
```

### 3. 여러 데이터 소스 조합
```kotlin
// 날씨 + 수온 데이터를 동시에 가져오기
fun getCombinedData(): Flow<CombinedData> {
    return combine(
        weatherRepository.getWeatherData(),
        temperatureRepository.getTemperatureData()
    ) { weather, temperature ->
        CombinedData(weather, temperature)
    }
}
```

## Flow 연산자 마스터하기

### 1. 변환 연산자
```kotlin
val numbers = flowOf(1, 2, 3, 4, 5)

// map - 각 값을 변환
numbers.map { it * 2 }  // 2, 4, 6, 8, 10

// filter - 조건에 맞는 값만 필터링
numbers.filter { it % 2 == 0 }  // 2, 4

// transform - 복잡한 변환
numbers.transform { value ->
    emit("Number: $value")
    emit("Squared: ${value * value}")
}
```

### 2. 조합 연산자
```kotlin
val flow1 = flowOf(1, 2, 3)
val flow2 = flowOf("A", "B", "C")

// zip - 두 Flow를 짝지어서 조합
flow1.zip(flow2) { number, letter ->
    "$number$letter"
}  // 1A, 2B, 3C

// combine - 여러 Flow의 최신 값들을 조합
combine(flow1, flow2) { number, letter ->
    "$number$letter"
}
```

### 3. 에러 처리 연산자
```kotlin
val riskyFlow = flow {
    emit(1)
    throw Exception("Something went wrong!")
}

// catch - 에러를 잡아서 처리
riskyFlow.catch { error ->
    emit(-1)  // 에러 시 기본값
}

// onEach - 각 값에 대해 부수 효과 실행
riskyFlow.onEach { value ->
    println("Received: $value")
}
```

## Compose에서 Flow 사용하기

### 1. collectAsState
```kotlin
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.weatherData.collectAsState()
    
    when (weatherData) {
        is WeatherResult.Loading -> LoadingSpinner()
        is WeatherResult.Success -> WeatherContent(weatherData.data)
        is WeatherResult.Error -> ErrorMessage(weatherData.message)
    }
}
```

### 2. LaunchedEffect와 함께 사용
```kotlin
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.weatherData.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.fetchWeatherData()
    }
    
    // UI 구성...
}
```

## 실전 팁과 트릭

### 1. 디버깅하기
```kotlin
val debugFlow = originalFlow
    .onEach { println("Flow value: $it") }
    .catch { error -> 
        println("Flow error: $error")
        throw error
    }
```

### 2. 타임아웃 설정
```kotlin
val timeoutFlow = originalFlow
    .timeout(5000)  // 5초 타임아웃
    .catch { error ->
        if (error is TimeoutCancellationException) {
            emit(DefaultValue)
        } else {
            throw error
        }
    }
```

### 3. 중복 제거
```kotlin
val distinctFlow = originalFlow
    .distinctUntilChanged()  // 연속된 중복 값 제거
```

## 시행착오와 해결책

### 시행착오 1: Flow 수집 중 에러 처리
```kotlin
// 잘못된 방법
viewModelScope.launch {
    flow.collect { value ->
        // 에러가 발생하면 Flow가 중단됨
        processValue(value)
    }
}

// 올바른 방법
viewModelScope.launch {
    flow.catch { error ->
        // 에러 처리
        emit(DefaultValue)
    }.collect { value ->
        processValue(value)
    }
}
```

### 시행착오 2: 메모리 누수 방지
```kotlin
// 주의: Flow 수집을 취소해야 함
private var job: Job? = null

fun startCollecting() {
    job = viewModelScope.launch {
        flow.collect { value ->
            // 처리
        }
    }
}

fun stopCollecting() {
    job?.cancel()
}
```

### 시행착오 3: StateFlow 초기값 설정
```kotlin
// 잘못된 방법
val stateFlow = MutableStateFlow<WeatherData?>(null)

// 올바른 방법
val stateFlow = MutableStateFlow(WeatherData.empty())
```

## 성능 최적화

### 1. 적절한 Dispatcher 사용
```kotlin
val optimizedFlow = originalFlow
    .flowOn(Dispatchers.IO)  // 백그라운드에서 처리
    .onEach { value ->
        // UI 업데이트는 Main에서
        withContext(Dispatchers.Main) {
            updateUI(value)
        }
    }
```

### 2. 버퍼링
```kotlin
val bufferedFlow = originalFlow
    .buffer(10)  // 10개까지 버퍼링
    .conflate()  // 최신 값만 유지
```

## 마무리

Flow는 처음에는 어려워 보이지만, 한번 익숙해지면 정말 강력한 도구가 됩니다.

### 핵심 포인트:
1. **Flow는 데이터 스트림** - 시간에 따라 흘러나오는 데이터를 처리
2. **연산자를 활용** - map, filter, combine 등으로 데이터 변환
3. **에러 처리를 잊지 마세요** - catch 연산자로 안전하게 처리
4. **Compose와 완벽 호환** - collectAsState로 쉽게 사용

### WaveOn 앱에서의 활용:
- 실시간 날씨 데이터 업데이트
- 예약 내역 실시간 동기화
- 여러 API 응답 조합
- 데이터베이스 변경 감지

이제 Flow를 마스터하셨나요? 다음에는 더 고급 주제인 **Flow 테스팅**이나 **Custom Flow 연산자 만들기**에 대해 다뤄볼까요?

---

**Tags:** #Android #Kotlin #Flow #Coroutines #Compose #비동기 #개발팁 #실전 