# WaveOn 앱 개발기 1편: "서핑 앱을 만들어보자! - 기획부터 첫 번째 화면까지"

안녕하세요. Marty입니다. 7년차 Android 개발자로 일하고 있습니다.

오늘부터 WavePark 서핑 레저 시설을 위한 Android 앱 "WaveOn" 개발 과정을 공유하려고 합니다. 

## 왜 이 앱을 만들게 되었나요?

서핑을 좋아해서 자주 WavePark에 가는데, 웹사이트가 불편했습니다. 모바일에서 접속하면 화면이 작고, 예약 정보를 확인하기도 번거로웠죠. 

그래서 직접 앱을 만들어보기로 했습니다. 

## 새로운 도전: Compose로 UI 구축하기

7년간 XML로 UI를 만들어왔는데, 이번 프로젝트에서는 **Jetpack Compose**를 도입해보기로 했습니다. 새로운 기술을 배우는 건 항상 설레면서도 긴장되는 일이죠.

### 시행착오 1: XML에서 Compose로의 전환
- 7년간 익숙했던 XML 레이아웃과 완전히 다른 패러다임
- LinearLayout, ConstraintLayout 대신 Column, Row, Box 사용
- findViewById() 대신 remember와 state 관리
- 처음에는 정말 어색했습니다

### 시행착오 2: 선언형 UI 적응하기
- XML에서는 "어떻게 그릴까?"를 생각했는데
- Compose에서는 "무엇을 보여줄까?"를 생각해야 합니다
- 상태 변화에 따른 UI 업데이트가 자동으로 되는 게 신기하면서도 어려웠습니다

## 첫 번째 화면 만들기

### Splash 화면부터 시작했습니다:
```kotlin
@AndroidEntryPoint
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaveOnTheme {
                SplashScreen()
            }
        }
    }
}

@Composable
fun SplashScreen() {
    var startAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(3000)
        // 메인 화면으로 이동
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 로고 애니메이션
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(if (startAnimation) 1f else 0.5f)
                .animateContentSize()
        )
    }
}
```

XML로는 10줄이면 끝날 코드가 Compose로는 30줄이 되었습니다. 하지만 애니메이션과 상태 관리가 훨씬 직관적이었습니다.

## Bottom Navigation 구현

메인 화면에는 4개의 탭을 만들었습니다:
- 홈 (이벤트, 날씨, 수온 정보)
- 대시보드 (세션 정보)
- 카풀 (커뮤니티)
- 알림 (예약 내역)

```kotlin
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            BottomNavigation {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("홈") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                // ... 다른 탭들
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeScreen()
            1 -> DashboardScreen()
            2 -> CarpoolScreen()
            3 -> NotificationScreen()
        }
    }
}
```

## 첫 번째 성취감

Splash 화면과 Bottom Navigation이 완성되었습니다. 7년간 XML로만 작업해왔는데, Compose로 UI를 만드는 새로운 경험을 했다는 게 신기했습니다.

### XML vs Compose 비교
**XML 방식 (기존):**
```xml
<!-- 20줄의 XML 코드 -->
<LinearLayout>
    <ImageView />
    <TextView />
    <!-- 복잡한 레이아웃 구조 -->
</LinearLayout>
```

**Compose 방식 (새로운):**
```kotlin
// 10줄의 Compose 코드
Column {
    Image()
    Text()
    // 직관적인 구조
}
```

### 배운 것들:
- Jetpack Compose의 선언형 UI 패러다임
- remember와 mutableStateOf를 활용한 상태 관리
- LaunchedEffect를 통한 사이드 이펙트 처리
- XML에서 Compose로의 마이그레이션 전략

## 다음 편 예고

다음 편에서는 실제 데이터를 가져와서 화면에 표시하는 작업을 해볼 예정입니다. API 연동부터 시작해서 날씨 정보, 수온 정보를 실시간으로 가져오는 기능을 구현해보겠습니다.

---

**Tags:** #Android #Kotlin #MVVM #Hilt #개발기 #서핑앱 #WaveOn 