# API 키 설정 가이드

이 프로젝트는 API 키와 URL을 안전하게 관리하기 위해 BuildConfig를 사용합니다.

## 설정 방법

### 1. local.properties 파일 설정

`local.properties` 파일에 다음 API 키들을 추가하세요:

```properties
# API Keys (이 파일은 Git에 포함되지 않습니다)
OPENWEATHER_API_KEY=your_actual_openweather_api_key_here
WAVEPARK_API_KEY=your_actual_wavepark_api_key_here
FIREBASE_API_KEY=your_actual_firebase_api_key_here
```

### 2. gradle.properties 파일 설정

`gradle.properties` 파일에 다음 API URL들을 추가하세요:

```properties
# API URLs (공개 가능한 URL들)
WAVEPARK_BASE_URL=https://wavepark.co.kr
WEATHER_API_BASE_URL=https://api.openweathermap.org/data/2.5
TEMPERATURE_API_BASE_URL=https://api.example.com/temperature
```

### 3. BuildConfig 사용 방법

코드에서 API 키와 URL을 사용할 때는 다음과 같이 사용하세요:

```kotlin
import com.surfing.inthe.wavepark.BuildConfig

// API 키 사용
val apiKey = BuildConfig.OPENWEATHER_API_KEY

// API URL 사용
val baseUrl = BuildConfig.WAVEPARK_BASE_URL
```

또는 유틸리티 클래스를 사용하세요:

```kotlin
import com.surfing.inthe.wavepark.util.ApiConfig

// API 키 사용
val apiKey = ApiConfig.OPENWEATHER_API_KEY

// API URL 사용
val baseUrl = ApiConfig.WAVEPARK_BASE_URL

// API 키 유효성 검사
if (ApiConfig.areAllApiKeysConfigured()) {
    // API 호출 로직
}
```

## 보안 주의사항

1. **local.properties는 절대 Git에 커밋하지 마세요**
   - 이 파일은 이미 `.gitignore`에 포함되어 있습니다
   - API 키가 포함된 파일이므로 보안상 중요합니다

2. **gradle.properties는 공개 가능한 URL만 포함**
   - API 키는 포함하지 마세요
   - 기본 URL만 포함하세요

3. **실제 API 키는 안전하게 관리**
   - 개발팀 내에서만 공유
   - 환경별로 다른 키 사용 권장

## 환경별 설정

### 개발 환경
- `local.properties`에 개발용 API 키 설정

### 프로덕션 환경
- CI/CD 파이프라인에서 환경변수로 설정
- 또는 별도의 프로덕션용 설정 파일 사용

## 문제 해결

### BuildConfig가 생성되지 않는 경우
1. `build.gradle.kts`에서 `buildConfig = true` 확인
2. 프로젝트를 Clean & Rebuild
3. Android Studio 재시작

### API 키가 "YOUR_API_KEY"로 표시되는 경우
1. `local.properties`에 올바른 API 키가 설정되었는지 확인
2. 프로젝트를 Sync
3. BuildConfig 재생성

## 지원되는 API

- **OpenWeather API**: 날씨 정보
- **WavePark API**: WavePark 서비스 API
- **Firebase API**: Firebase 서비스
- **수온 API**: 해수 온도 정보 