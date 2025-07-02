# WaveOn (웨이브온)

> **WaveOn**은 WavePark 서핑 레저 시설의 웹사이트를 더 편리하게 이용할 수 있도록 만든 Android 앱입니다.  
> 웹사이트의 불편함을 개선하고, 실시간 세션 정보와 다양한 소식을 한눈에 볼 수 있도록 **MVVM + Hilt + Retrofit** 기반으로 개발되었습니다.

---

## 🌊 주요 기능 (Features)

- **WavePark 공식 웹사이트 WebView 내장**
- **날짜/시간별 파도 세션 남은 좌석 실시간 표시**
- **이벤트 및 소식 리스트업**
- **가장 가까운 예약건 자동 노출**
- **마이페이지 예약 내역 실시간 크롤링/공유/표시 (Jsoup + WebView + StateFlow)**
- **예약 내역 QR코드 다이얼로그 생성 (zxing)**
- **카풀/커뮤니티 기능(예정)**
- **원하는 세션 빈자리 알림(예정)**

---

## 🛠️ 기술 스택 (Tech Stack)

- **언어**: Kotlin
- **아키텍처**: MVVM
- **DI**: Hilt (싱글톤 Repository, ViewModel 주입)
- **네트워크**: Retrofit, Jsoup, Firestore (Firebase)
- **알림**: FCM (Firebase Cloud Messaging)
- **UI**: ViewBinding, DataBinding, Jetpack Compose (일부 화면)
- **상태관리**: StateFlow, LiveData
- **기타**: Google Analytics, Firebase Authentication(예정), ZXing(QR)

---

## 📦 프로젝트 구조

```
WaveOn/
 ├─ app/
 │   ├─ src/main/java/com/surfing/inthe/wavepark/
 │   │   ├─ ui/           # 화면별 Fragment, Activity, Compose, Adapter 등
 │   │   ├─ data/         # API, 모델, Repository, Jsoup 파싱
 │   │   ├─ di/           # Hilt DI 모듈
 │   │   ├─ util/         # 공통 유틸리티
 │   │   ├─ ...
 │   ├─ res/              # 레이아웃, 리소스, 아이콘, 폰트
 │   ├─ ...
 ├─ build.gradle.kts
 ├─ README.md
 └─ ...
```

---

## 🧩 구조 및 패턴

- **API 키/URL**: BuildConfig로 안전하게 관리
- **DI**: Hilt 기반 싱글톤 Repository, ViewModel 주입
- **데이터 공유**: Repository에서 StateFlow로 관리, ViewModel에서 그대로 노출
- **WebView + Jsoup**: WebView 세션 유지, Jsoup로 예약 내역 크롤링 및 실시간 반영
- **Compose**: 예약 리스트 등 일부 화면 Compose로 구현, 실시간 데이터 반영
- **QR코드**: 예약번호로 QR코드 생성 및 다이얼로그 표시

---

## 🏄‍♂️ 개발 히스토리/특이사항

- WebView와 Jsoup 연동, 서버 차단 우회(헤더 커스텀), 비동기 크롤링, StateFlow 실시간 데이터 공유 등 고도화
- Activity/Fragment/Compose 간 ViewModel/Repository 데이터 싱크 문제 해결(Hilt 싱글톤 패턴)
- 예약 내역: 결제완료+오늘 이후만 필터, 예약일자 기준 오름차순 정렬, QR 다이얼로그까지 완성
- FAB ProgressBar, 무한로딩 방지, 실시간 UI 반영 등 UX 개선

---

## 💡 TODO & Roadmap

- [ ] 빈자리 알림 푸시
- [ ] 카풀/커뮤니티 기능
- [ ] 관리자/운영자 기능
- [ ] 다국어 지원
- [ ] 테스트 코드 및 CI/CD

---

**화이팅! 서핑도, 개발도, 성장도!** 🏄‍♂️



