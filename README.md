# WaveOn (웨이브온)

> **WaveOn**은 WavePark 서핑 레저 시설의 웹사이트를 더 편리하게 이용할 수 있도록 만든 Android 앱입니다. 
> 웹사이트의 불편함을 개선하고, 실시간 세션 정보와 다양한 소식을 한눈에 볼 수 있도록 MVVM 패턴 기반으로 개발되었습니다.

---

## 🌊 주요 기능 (Features)

- **WavePark 공식 웹사이트 웹뷰**
- **날짜/시간별 파도 세션 남은 좌석 실시간 표시**
- **이벤트 및 소식 리스트업**
- **가장 가까운 예약건 자동 노출**
- **원하는 세션 빈자리 알림(예정)**
- **카풀/커뮤니티 기능(예정)**

---

## 🛠️ 기술 스택 (Tech Stack)

- **언어**: Kotlin
- **아키텍처**: MVVM
- **DI**: Hilt
- **네트워크**: Retrofit, Firestore (Firebase)
- **알림**: FCM (Firebase Cloud Messaging)
- **UI**: ViewBinding, DataBinding, 일부 Compose
- **기타**: Google Analytics, Firebase Authentication(예정)

---

## 📦 프로젝트 구조

```
WaveOn/
 ├─ app/
 │   ├─ src/main/java/com/surfing/inthe/wavepark/
 │   │   ├─ ui/           # 화면별 Fragment, Adapter 등
 │   │   ├─ data/         # API, 네트워크, 모델
 │   │   ├─ di/           # Hilt DI 모듈
 │   │   ├─ ...
 │   ├─ res/              # 레이아웃, 리소스
 │   ├─ ...
 ├─ build.gradle.kts
 ├─ README.md
 └─ ...
```

---

## 🚀 빌드 & 실행 방법

1. **Firebase 연동**: `google-services.json` 파일을 `app/` 폴더에 위치시킵니다.
2. **SHA-1 등록**: Firebase 콘솔에 앱의 SHA-1, SHA-256을 등록하세요.
3. **의존성 설치**: Android Studio에서 `Sync Project with Gradle Files` 실행
4. **실행**: Google Play 지원 에뮬레이터 또는 실기기에서 빌드 & 실행

---

## 🤝 기여 및 개발자 스토리

> 7년간 MVC만 고집하며, private repo만 써서 잔디가 휑했던 개발자입니다. 
> "늦었다고 생각할 때가 진짜 시작이다"라는 마음으로, MVVM과 최신 안드로이드 기술을 적극 도입해 발전 중입니다. 
> 화면 구성과 기능은 계속 발전/변경될 예정이며, ViewBinding, DataBinding, Compose까지 다양한 UI 방식을 실험합니다. 

- **기여 환영**: Issue/PR/피드백 모두 환영합니다!
- **문의**: [이메일/깃허브 이슈 등]

---

## 💡 TODO & Roadmap

- [ ] 빈자리 알림 푸시
- [ ] 카풀/커뮤니티 기능
- [ ] 관리자/운영자 기능
- [ ] 다국어 지원
- [ ] 테스트 코드 및 CI/CD

---

**화이팅! 서핑도, 개발도, 성장도!** 🏄‍♂️



