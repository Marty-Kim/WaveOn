# WavePark 앱 안정성 개선안 (실전 예시 코드)

앱이 오랜 시간 후 재실행 시 꺼지는 현상, 메모리 누수, ANR 등 주요 문제를 방지하기 위한 실전 개선안과 예시 코드를 정리합니다.

---

## 1. Flow collect 범위 제한

```kotlin
import kotlinx.coroutines.flow.first

init {
    viewModelScope.launch {
        _isLoading.value = true
        eventRepository.syncEventsIfNeeded()
        // Flow collect를 한 번만 실행
        val events = eventRepository.getEventsFlow().first()
        _events.emit(events)
        eventRepository.fetchAndSaveEventImagesIfNeeded()
        _isLoading.value = false
    }
}
```
- `.first()`는 Flow에서 첫 데이터만 받고 종료. 무한 collect로 인한 메모리 누수 방지.

---

## 2. 중첩된 Flow collect 제거 및 단일 쿼리 사용

**Room Dao에 단일 쿼리 함수 추가**
```kotlin
@Query("SELECT * FROM events WHERE isActive = 1 ORDER BY startDate DESC")
suspend fun getAllActiveEventsOnce(): List<EventEntity>
```

**Repository에서 단일 쿼리 사용**
```kotlin
override suspend fun fetchAndSaveEventImagesIfNeeded() {
    val events = eventDao.getAllActiveEventsOnce() // Flow collect 대신 suspend 함수 사용
    events.filter { it.imageList.isNullOrBlank() }.forEach { eventEntity ->
        val images = fetchImagesByJsoup(eventEntity.eventId)
        if (images.isNotEmpty()) {
            val updated = eventEntity.copy(imageList = images.joinToString(","))
            eventDao.updateEvent(updated)
        }
    }
}
```

---

## 3. 메모리 사용량 모니터링 및 로그 추가

```kotlin
import android.util.Log

fun logMemoryUsage(tag: String = "Memory") {
    val runtime = Runtime.getRuntime()
    val usedMemInMB = (runtime.totalMemory() - runtime.freeMemory()) / 1048576L
    val maxHeapSizeInMB = runtime.maxMemory() / 1048576L
    Log.d(tag, "Used Memory: $usedMemInMB MB / Max Heap: $maxHeapSizeInMB MB")
}
```
- 중요한 작업 전후로 `logMemoryUsage()` 호출

---

## 4. ANR(메인스레드 블로킹) 방지

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    // 네트워크/DB 작업
}
```
- 무거운 작업은 항상 IO 디스패처에서 실행

---

## 5. ViewModel/Fragment/Activity의 생명주기 관리

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    homeViewModel.events.collect { events ->
        // UI 업데이트
    }
}
```
- Fragment가 사라질 때 collect도 자동으로 취소되어 메모리 누수 방지

---

## 6. 백그라운드 종료/재시작 대비 데이터 복원

```kotlin
init {
    viewModelScope.launch {
        // 앱이 재시작될 때도 항상 Room에서 최신 데이터 로드
        val events = eventRepository.getEventsFlow().first()
        _events.emit(events)
    }
}
```

---

## 7. 이미지 캐싱/Glide 메모리 관리

```kotlin
Glide.with(context)
    .load(url)
    .diskCacheStrategy(DiskCacheStrategy.ALL)
    .into(imageView)
```
- 이미지 캐시를 적극적으로 활용하여 메모리 사용량을 줄임

---

## 8. 불필요한 코루틴/Job 취소

```kotlin
private var fetchJob: Job? = null

fun fetchData() {
    fetchJob?.cancel()
    fetchJob = viewModelScope.launch {
        // 데이터 fetch
    }
}
```
- 이전 작업이 남아있을 때는 취소하고 새로 시작

---

**이 항목들을 적용하면 앱의 안정성과 메모리 효율이 크게 향상됩니다.** 