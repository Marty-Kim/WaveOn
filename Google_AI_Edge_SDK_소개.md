# Google AI Edge SDK: 기기 내 AI 모델로 앱을 더 스마트하게 만들기

안녕하세요. Marty입니다. 7년차 Android 개발자로 일하고 있습니다.

오늘은 Google에서 최근에 발표한 **AI Edge SDK**와 **Gemini Nano experimental access**에 대해 소개해드리겠습니다. 이 SDK는 기기 내에서 AI 모델을 실행할 수 있게 해주는 혁신적인 도구로, 현재 실험적 접근 프로그램을 통해 개발자들이 테스트할 수 있습니다.

## AI Edge SDK란?

AI Edge SDK는 Android 기기에서 **Gemini Nano** 모델을 실행할 수 있게 해주는 Google의 새로운 SDK입니다. 기존의 클라우드 기반 AI 서비스와 달리, 이 SDK는 사용자의 기기에서 직접 AI 모델을 실행하므로 개인정보 보호와 응답 속도 면에서 큰 장점이 있습니다.

### 주요 특징

- **기기 내 실행**: 인터넷 연결 없이도 AI 기능 사용 가능
- **개인정보 보호**: 데이터가 기기를 벗어나지 않음
- **빠른 응답**: 네트워크 지연 없이 즉시 응답
- **오프라인 지원**: 인터넷이 없는 환경에서도 사용 가능

## 실험적 접근 프로그램 참여하기

현재 Gemini Nano는 실험적 접근 프로그램을 통해 테스트할 수 있습니다. 참여하기 위해서는 다음 단계를 따라야 합니다:

### 필수 요구사항
- **Pixel 9 시리즈 기기**가 필요합니다
- 테스트용으로 사용할 계정으로만 로그인되어 있어야 합니다

### 참여 단계
1. **aicore-experimental Google 그룹에 가입**
2. **Android AICore 테스팅 프로그램에 참여**
3. **Play 스토어에서 앱 이름이 "Android AICore"에서 "Android AICore (Beta)"로 변경되는지 확인**

### APK 업데이트
1. **AICore APK 업데이트**:
   - 프로필 아이콘 → 관리 앱 및 기기 → 관리
   - Android AICore → 업데이트 (가능한 경우)

2. **Private Compute Service APK 업데이트**:
   - 프로필 아이콘 → 관리 앱 및 기기 → 관리
   - Private Compute Services → 업데이트 (가능한 경우)
   - 앱 정보 탭에서 버전이 1.0.release.658389993 이상인지 확인

3. **기기 재시작** 후 몇 분 대기하여 테스팅 등록이 적용되도록 함

4. **Play 스토어에서 AICore APK 버전 확인** (앱 정보 탭에서 0.thirdpartyeap으로 시작하는지 확인)

## 사용 사례

AI Edge SDK는 다음과 같은 특정 태스크에 최적화되어 있습니다:

### 1. 텍스트 문구 변경
사용자의 메시지나 텍스트의 어조와 스타일을 변경할 수 있습니다.

```kotlin
// 예시: 캐주얼한 메시지를 격식 있는 스타일로 변경
val casualMessage = "안녕! 오늘 날씨 진짜 좋네"
val formalMessage = aiEdgeSDK.changeTextStyle(casualMessage, Style.FORMAL)
// 결과: "안녕하세요. 오늘 날씨가 정말 좋습니다."
```

### 2. 스마트 답장
채팅 대화에서 맥락에 맞는 응답을 생성합니다.

```kotlin
// 대화 맥락을 기반으로 적절한 답장 생성
val conversation = listOf(
    "안녕하세요",
    "안녕! 오늘 뭐해?",
    "서핑하러 갈까 해"
)
val smartReply = aiEdgeSDK.generateSmartReply(conversation)
// 결과: "와! 좋은 날씨에 서핑하기 딱이네요. 즐거운 시간 보내세요!"
```

### 3. 교정
맞춤법과 문법 오류를 자동으로 수정합니다.

```kotlin
val textWithErrors = "나는 어제 친구와 함께 영화관에 갔어요."
val correctedText = aiEdgeSDK.correctText(textWithErrors)
// 결과: "저는 어제 친구와 함께 영화관에 갔습니다."
```

### 4. 요약
긴 문서나 텍스트를 간결한 요약으로 압축합니다.

```kotlin
val longDocument = "매우 긴 문서 내용..."
val summary = aiEdgeSDK.summarizeText(longDocument)
// 결과: "주요 내용을 요약한 간결한 텍스트"
```

## 실제 Google 앱에서의 활용

Google은 이미 여러 앱에서 AI Edge SDK를 활용하고 있습니다:

### TalkBack
Android의 접근성 앱인 TalkBack은 Gemini Nano의 다중 모드 입력 기능을 활용하여 시각 장애가 있는 사용자를 위한 이미지 설명을 개선했습니다.

### Pixel Voice Recorder
Pixel Voice Recorder 앱은 AI Edge SDK를 사용하여 기기 내 요약 기능을 지원합니다. 긴 녹음 파일을 자동으로 요약해주는 기능이 추가되었습니다.

### Gboard
Gboard의 스마트 답장 기능은 AI Edge SDK를 통해 온디바이스 Gemini Nano를 활용하여 정확한 스마트 답장을 제공합니다.

## 개발 환경 설정

### 1. Gradle 설정

```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.ai.edge.aicore:aicore:0.0.1-exp01")
}

android {
    defaultConfig {
        minSdk = 31  // 최소 SDK 31 이상 필요
        // ...
    }
}
```

### 2. GenerationConfig 생성

AI 모델의 추론 방식을 커스터마이징할 수 있는 설정 객체를 생성합니다:

```kotlin
val generationConfig = generationConfig {
    context = ApplicationProvider.getApplicationContext() // 필수
    temperature = 0.2f        // 무작위성 제어 (높을수록 다양성 증가)
    topK = 16                // 고순위 토큰 중 고려할 토큰 수
    maxOutputTokens = 256    // 응답 길이
}
```

### 3. GenerativeModel 초기화

```kotlin
// 선택적 다운로드 콜백 생성 (디버깅용)
val downloadConfig = DownloadConfig(downloadCallback)

// GenerativeModel 객체 생성
val generativeModel = GenerativeModel(
    generationConfig = generationConfig,
    downloadConfig = downloadConfig // 선택사항
)
```

## 실제 사용 예제

### 1. 기본 추론 실행

```kotlin
scope.launch {
    // 단일 문자열 입력 프롬프트
    val input = """
        I want you to act as an English proofreader. I will provide you texts, 
        and I would like you to review them for any spelling, grammar, or 
        punctuation errors. Once you have finished reviewing the text, provide me 
        with any necessary corrections or suggestions for improving the text: 
        These arent the droids your looking for.
    """.trimIndent()
    
    val response = generativeModel.generateContent(input)
    println(response.text)
}
```

### 2. 다중 문자열 입력

```kotlin
scope.launch {
    val response = generativeModel.generateContent(
        content {
            text("I want you to act as an English proofreader. I will provide you texts and I would like you to review them for any spelling, grammar, or punctuation errors.")
            text("Once you have finished reviewing the text, provide me with any necessary corrections or suggestions for improving the text:")
            text("These arent the droids your looking for.")
        }
    )
    println(response.text)
}
```

### 3. 텍스트 교정 기능

```kotlin
class TextCorrector {
    private val generativeModel: GenerativeModel = // 초기화
    
    suspend fun correctText(text: String): String {
        val prompt = """
            I want you to act as an English proofreader. I will provide you texts, 
            and I would like you to review them for any spelling, grammar, or 
            punctuation errors. Once you have finished reviewing the text, provide me 
            with any necessary corrections or suggestions for improving the text: $text
        """.trimIndent()
        
        val response = generativeModel.generateContent(prompt)
        return response.text ?: text
    }
}
```

### 4. 스마트 답장 생성 (이모지 예측)

```kotlin
class SmartReplyGenerator {
    private val generativeModel: GenerativeModel = // 초기화
    
    suspend fun predictEmojis(message: String): String {
        val prompt = """
            Predict up to 5 emojis as a response to a text chat message. 
            The output should only include emojis.
            
            input: $message
            output:
        """.trimIndent()
        
        val response = generativeModel.generateContent(prompt)
        return response.text ?: "👍"
    }
}

// 사용 예시
val emojis = smartReplyGenerator.predictEmojis("The new visual design is blowing my mind 🤯")
// 결과: ➕,💘,❤‍🔥
```

## 프롬프트 설계 팁

프롬프트 설계는 언어 모델로부터 최적의 응답을 이끌어내는 과정입니다. 잘 구조화된 프롬프트를 작성하는 것은 정확하고 고품질의 응답을 보장하는 필수적인 부분입니다.

### 주의사항
- **Gemini Nano는 최대 12,000개의 입력 토큰을 허용**합니다
- 명확하고 구체적인 지시사항을 제공하세요
- 원하는 출력 형식을 명시하세요

### 텍스트 교정용 프롬프트

```kotlin
val correctionPrompt = """
    I want you to act as an English proofreader. I will provide you texts, and I
    would like you to review them for any spelling, grammar, or punctuation errors.
    Once you have finished reviewing the text, provide me with any necessary
    corrections or suggestions for improving the text: $inputText
""".trimIndent()
```

### 스마트 답장용 프롬프트

```kotlin
val smartReplyPrompt = """
    Predict up to 5 emojis as a response to a text chat message. The output
    should only include emojis.
    
    input: $message
    output:
""".trimIndent()
```

### 예시 결과들

```kotlin
// 입력: "The new visual design is blowing my mind 🤯"
// 출력: ➕,💘,❤‍🔥

// 입력: "Well that looks great regardless"
// 출력: 💗,🪄

// 입력: "Unfortunately this won't work"
// 출력: 💔,😔

// 입력: "sounds good, I'll look into that"
// 출력: 🙏,👍
```

## 성능 최적화 팁

### 1. 메시지 표시 전략
사용자에게 AI 처리 중임을 알리는 것이 중요합니다. 로딩 인디케이터를 표시하여 사용자 경험을 개선하세요.

```kotlin
@Composable
fun AITextProcessor() {
    var isProcessing by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    
    Column {
        if (isProcessing) {
            CircularProgressIndicator()
            Text("AI가 처리 중입니다...")
        } else {
            Text(result)
        }
        
        Button(
            onClick = {
                isProcessing = true
                // AI 처리 로직
                isProcessing = false
            }
        ) {
            Text("텍스트 처리하기")
        }
    }
}
```

### 2. 에러 처리
모델 로딩 실패나 추론 오류에 대한 적절한 에러 처리를 구현하세요.

```kotlin
suspend fun processWithAI(text: String): Result<String> {
    return try {
        val response = generativeModel.generateContent(text)
        Result.success(response.text ?: "")
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## 주의사항

### 1. 실험적 접근 프로그램
- 현재 Gemini Nano는 실험적 접근 프로그램을 통해만 사용 가능합니다
- Pixel 9 시리즈 기기가 필수적으로 필요합니다
- 테스팅 계정으로만 로그인되어 있어야 합니다

### 2. 기기 호환성
- 현재 지원되는 기기 목록을 확인하고, 지원되지 않는 기기에서는 대체 기능을 제공하세요
- AICore APK와 Private Compute Service APK가 최신 버전인지 확인하세요

### 3. 모델 크기와 성능
- Gemini Nano는 기기 내 실행을 위해 최적화되어 있지만, 여전히 상당한 메모리를 사용할 수 있습니다
- 최대 12,000개의 입력 토큰 제한이 있습니다

### 4. 배터리 소모
- AI 모델 실행은 배터리 소모를 증가시킬 수 있으므로, 적절한 사용 패턴을 권장하세요

## 미래 전망

Google은 AI Edge SDK의 기능을 지속적으로 확장할 계획입니다:

- **추가 기기 지원**: 더 많은 Android 기기에서 사용 가능
- **새로운 모달리티**: 이미지, 음성 등 다양한 입력 형태 지원
- **향상된 성능**: 더 빠르고 정확한 AI 모델

## 결론

Google AI Edge SDK와 Gemini Nano experimental access는 Android 앱 개발에 새로운 가능성을 열어주는 혁신적인 도구입니다. 기기 내 AI 실행을 통해 개인정보 보호와 성능을 모두 확보할 수 있어, 앞으로 많은 앱에서 활용될 것으로 예상됩니다.

현재는 실험적 접근 프로그램을 통해 Pixel 9 시리즈 기기에서만 테스트할 수 있지만, 이는 AI 기술의 미래를 미리 경험할 수 있는 좋은 기회입니다.

특히 서핑 앱 WaveOn과 같은 서비스에서는 사용자 메시지의 스마트 답장, 예약 정보 요약, 또는 서핑 팁 생성 등에 활용할 수 있을 것 같습니다.

새로운 기술에 대한 학습과 적용은 항상 도전적이지만, 이런 혁신적인 도구들을 활용하면 더욱 스마트하고 사용자 친화적인 앱을 만들 수 있습니다.

### 피드백 제공
Google AI Edge SDK나 기타 피드백이 있으시면 [Google 개발자 지원팀에 티켓을 제출](https://support.google.com/)하실 수 있습니다.

---

**참고 자료:**
- [Google AI Edge SDK 공식 문서](https://developer.android.com/ai/gemini-nano/ai-edge-sdk)
- [Gemini Nano Experimental Access 가이드](https://developer.android.com/ai/gemini-nano/ai-edge-sdk)

**Tags:** #Android #AI #Google #Gemini #EdgeSDK #ExperimentalAccess #개발팁 #AI모델

---

**참고 자료:**
- [Google AI Edge SDK 공식 문서](https://developer.android.com/ai/gemini-nano/ai-edge-sdk)

**Tags:** #Android #AI #Google #Gemini #EdgeSDK #개발팁 #AI모델 