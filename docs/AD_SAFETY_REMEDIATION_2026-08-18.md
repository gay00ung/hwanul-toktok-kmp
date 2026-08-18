# 광고 안전 조치 기록 (2026-08-18)

## 상태

이 문서는 로컬 소스에 적용된 긴급 조치를 기록합니다. 아직 스토어에 배포되거나
Google에 이의신청으로 제출된 상태는 아닙니다.

## 조치 내용

Android와 iOS에서 광고 요청이 발생할 수 있는 런타임 경로를 모두 제거했습니다.

- Android Google Mobile Ads SDK 의존성 및 앱 ID 매니페스트 항목 제거
- Android SDK 초기화 제거
- 30초 주기의 배너 재요청 로직과 배너 UI 제거
- 알림 추가 시 사전 로드 및 노출되던 전면 광고 제거
- iOS Google Mobile Ads Swift Package, SDK 초기화, 배너 UI 제거
- iOS AdMob 앱/광고 단위 ID, 추적 설명, SKAdNetwork 설정 제거
- 광고 관련 코드나 설정이 다시 들어오면 빌드가 실패하는 `verifyAdsDisabled` 검사 추가
- Android 릴리스 버전을 versionName 1.0.10 / versionCode 11로 증가

`public/app-ads.txt`는 게시자 소유권을 선언하는 파일이며 광고 요청을 발생시키지
않으므로 유지했습니다.

## 검증

다음 검증을 통과했습니다.

- `./gradlew clean :composeApp:assembleDebug :composeApp:testDebugUnitTest :composeApp:compileKotlinIosSimulatorArm64`
- Android 병합 매니페스트에 Mobile Ads 컴포넌트와 AdMob 앱 ID가 없음
- 생성된 APK에 Mobile Ads 매니페스트 항목이 없음
- 소스 및 플랫폼 설정에 광고 로드 API, 광고 단위 ID, Mobile Ads SDK 참조가 없음
- iOS `Info.plist` 문법 검사 및 Xcode 프로젝트 로드 성공

Firebase Analytics의 전이 의존성인 광고 식별자 라이브러리는 남아 있지만 광고를
로드하거나 표시하는 Google Mobile Ads SDK는 아닙니다.

## 남은 작업

- Play Console에 새 빌드 배포 및 광고 포함 여부를 현재 상태에 맞게 수정
- 배포 일시와 스토어 처리 완료 화면 보관
- 배포 완료 후 사실에 근거한 이의신청 제출

이 저장소에는 CI/CD 설정이 없어 버전 변경, 태그, 스토어 배포는 수동 릴리스 절차가
필요합니다.
