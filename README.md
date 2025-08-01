# 📱 환율톡톡 (HwanulTokTok KMP)

**매일 아침, 오늘의 환율을 톡! 알려주는 귀여운 환율 알리미 앱** ☀️

환율톡톡은 한국수출입은행 공식 환율 데이터를 기반으로 Android와 iOS 모두에서 실시간 환율 정보와 맞춤형 푸시 알림을 제공하는 크로스플랫폼 앱입니다. Kotlin Multiplatform으로 개발되어 핵심 비즈니스 로직을 두 플랫폼에서 공유하며, 효율적이고 일관된 사용자 경험을 제공합니다.

---

## 📱 앱 개요

- **플랫폼**: Android & iOS (Kotlin Multiplatform)
- **아키텍처**: MVVM + Clean Architecture + Koin DI
- **UI 프레임워크**: Jetpack Compose Multiplatform
- **주요 기능**:
  - 실시간 환율 정보 조회
  - 맞춤형 환율 알림 설정
  - 백그라운드 환율 모니터링
  - AdMob 광고 통합
  - 다크/라이트 테마 지원

---

## 🛠 기술 스택

### 📱 Frontend (Android & iOS)
- **Kotlin Multiplatform**: 크로스플랫폼 개발
- **Jetpack Compose Multiplatform**: 선언형 UI 프레임워크
- **Koin**: 의존성 주입 (DI)
- **Navigation Compose**: 화면 이동 관리
- **Ktor Client**: HTTP 클라이언트 (REST API 통신)
- **SQLDelight**: 크로스플랫폼 데이터베이스
- **Kotlinx Serialization**: JSON 직렬화
- **Kotlinx DateTime**: 날짜/시간 처리

### 🔧 Platform Specific
**Android:**
- **WorkManager**: 백그라운드 작업 스케줄링
- **AdMob**: 광고 통합
- **Material 3**: 디자인 시스템

**iOS:**
- **Background Tasks**: 백그라운드 환율 체크
- **User Notifications**: 로컬 알림
- **AdMob**: 광고 통합
- **ATT (App Tracking Transparency)**: 광고 추적 권한
- **SwiftUI**: iOS 네이티브 UI (광고 컴포넌트)

### 📊 데이터 소스
- **한국수출입은행 환율 API**: 공식 환율 데이터 제공

---

## ⚙️ 개발 & 빌드 환경

### 필수 요구사항
- **JDK 17+**
- **Android Studio Hedgehog 이상**
- **Xcode 15+ (iOS 개발 시)**
- **Gradle 8+**

---

## 📂 프로젝트 구조

```
hwanul-toktok-kmp/
├── composeApp/                    # 메인 KMP 모듈
│   ├── src/
│   │   ├── commonMain/           # 공통 코드 (비즈니스 로직)
│   │   │   ├── kotlin/
│   │   │   │   ├── data/         # 데이터 레이어
│   │   │   │   ├── domain/       # 도메인 레이어
│   │   │   │   ├── presentation/ # 프레젠테이션 레이어
│   │   │   │   └── di/          # 의존성 주입
│   │   │   └── composeResources/ # 공통 리소스
│   │   ├── androidMain/          # Android 전용 코드
│   │   │   └── kotlin/
│   │   │       ├── data/        # Android 구현체
│   │   │       ├── service/     # 백그라운드 서비스
│   │   │       └── ui/          # Android UI 컴포넌트
│   │   └── iosMain/             # iOS 전용 코드
│   │       └── kotlin/
│   │           ├── data/        # iOS 구현체
│   │           └── di/          # iOS Koin 설정
│   └── build.gradle.kts
├── iosApp/                       # iOS 앱 모듈
│   ├── iosApp/
│   │   ├── iOSApp.swift         # iOS 앱 진입점
│   │   ├── AppDelegate.swift    # 백그라운드 태스크
│   │   ├── ContentView.swift    # 메인 SwiftUI 뷰
│   │   └── AdBannerView.swift   # 광고 배너 컴포넌트
│   └── iosApp.xcodeproj/
└── gradle/
    └── libs.versions.toml        # 의존성 버전 관리
```

---

## 🚀 주요 기능

### 💱 실시간 환율 정보
- **다양한 통화** 지원 (USD, EUR, JPY, CNY 등)
- **한국수출입은행 공식 데이터** 기반

### 🔔 스마트 알림 시스템
- **맞춤형 환율 알림**: 원하는 통화와 목표 환율 설정
- **조건부 알림**: 설정한 환율 도달 시에만 알림 발송
- **백그라운드 모니터링**: 앱이 종료되어도 환율 체크
- **주말 제외**: 환율 업데이트가 없는 주말에는 알림 생략

### 📊 사용자 친화적 UI/UX
- **직관적인 디자인**: 환율 정보를 한눈에 파악
- **다크/라이트 테마**: 시스템 설정에 따른 자동 전환
- **반응형 레이아웃**: 다양한 화면 크기 지원
- **애니메이션**: 부드러운 화면 전환과 상태 변화

### 📱 크로스플랫폼 최적화
- **공통 비즈니스 로직**: Kotlin Multiplatform으로 코드 재사용
- **플랫폼별 최적화**: 각 플랫폼의 고유 기능 활용
- **일관된 사용자 경험**: Android와 iOS에서 동일한 기능 제공

---

## 📈 향후 개선 계획

- **🎯 위젯 지원**: Android/iOS 홈화면 위젯
- **📊 환율 차트**: 히스토리컬 데이터 시각화
- **🌍 다국어 지원**: 영어, 중국어, 일본어 추가
- **🔔 푸시 알림**: FCM/APNs 기반 원격 알림
- **📱 Watch 앱**: Apple Watch/Wear OS 지원

---

## 👩‍💻 개발자

- **신가영** ([GitHub](https://github.com/gay00ung))
- **이메일**: gayoung990911@gmail.com
- **블로그**: [Velog](https://velog.io/@tlsrkdud0911/posts)

---

## 📦 다운로드

<a href="https://play.google.com/store/apps/details?id=net.ifmain.hwanultoktok.kmp">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/ko_badge_web_generic.png" alt="Google Play에서 다운로드" width="200"/>
</a>

> 본 앱은 실제 배포용으로 개발되어 [Google Play 스토어](https://play.google.com/store/apps/details?id=net.ifmain.hwanultoktok.kmp)에 정식 출시되었습니다.

---

## 📄 라이선스

이 프로젝트는 Apache License 2.0 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

---




