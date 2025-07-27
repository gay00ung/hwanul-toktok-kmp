import SwiftUI
import AppTrackingTransparency
import AdSupport

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
                    requestTrackingPermission()
                }
        }
    }
    
    private func requestTrackingPermission() {
        // iOS 14.5 이상에서만 ATT 권한 요청
        if #available(iOS 14.5, *) {
            ATTrackingManager.requestTrackingAuthorization { status in
                switch status {
                case .authorized:
                    // 추적 권한이 허용됨
                    print("ATT: 사용자가 추적을 허용했습니다")
                    print("IDFA: \(ASIdentifierManager.shared().advertisingIdentifier.uuidString)")
                case .denied:
                    // 추적 권한이 거부됨
                    print("ATT: 사용자가 추적을 거부했습니다")
                case .notDetermined:
                    // 아직 권한 요청을 하지 않음
                    print("ATT: 권한이 아직 결정되지 않았습니다")
                case .restricted:
                    // 시스템에 의해 제한됨
                    print("ATT: 추적이 제한되었습니다")
                @unknown default:
                    print("ATT: 알 수 없는 상태입니다")
                }
            }
        }
    }
}