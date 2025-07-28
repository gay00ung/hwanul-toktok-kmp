import UIKit
import BackgroundTasks
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        // 백그라운드 태스크 등록
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: "net.ifmain.hwanultoktok.kmp.exchangeRateCheck",
            using: nil
        ) { task in
            self.handleExchangeRateCheck(task: task as! BGAppRefreshTask)
        }
        
        return true
    }
    
    private func handleExchangeRateCheck(task: BGAppRefreshTask) {
        // 백그라운드 태스크 시작
        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }
        
        // 주말 체크 (토요일: 7, 일요일: 1)
        let calendar = Calendar.current
        let weekday = calendar.component(.weekday, from: Date())
        if weekday == 1 || weekday == 7 {
            print("주말이므로 환율 알림을 건너뜁니다.")
            task.setTaskCompleted(success: true)
            scheduleNextBackgroundTask()
            return
        }
        
        // Koin 초기화 (이미 초기화되어 있으면 무시됨)
        IOSKoinComponentKt.doInitKoinIOS()
        
        // 백그라운드에서 환율 체크 실행
        Task {
            do {
                // KMP 모듈에서 필요한 객체들 가져오기
                let checkAlertConditionsUseCase = IOSKoinComponentKt.getCheckAlertConditionsUseCaseIOS()
                let notificationService = IOSKoinComponentKt.getNotificationServiceIOS()
                let alertRepository = IOSKoinComponentKt.getAlertRepositoryIOS()
                
                // 알림 조건 체크
                let alertResults = try await checkAlertConditionsUseCase.invoke()
                
                // 조건을 만족하는 알림 발송
                for result in alertResults {
                    if result.shouldTrigger {
                        try await notificationService.showNotification(
                            title: "환율 알림",
                            message: result.message,
                            notificationId: Int32(result.alert.id.hashValue)
                        )
                        
                        // 마지막 알림 시간 업데이트
                        try await alertRepository.updateLastTriggeredTime(
                            alertId: result.alert.id,
                            timestamp: Int64(Date().timeIntervalSince1970 * 1000)
                        )
                    }
                }
                
                task.setTaskCompleted(success: true)
            } catch {
                print("백그라운드 환율 체크 실패: \(error)")
                task.setTaskCompleted(success: false)
            }
        }
        
        // 다음 백그라운드 태스크 스케줄
        scheduleNextBackgroundTask()
    }
    
    private func scheduleNextBackgroundTask() {
        let request = BGAppRefreshTaskRequest(identifier: "net.ifmain.hwanultoktok.kmp.exchangeRateCheck")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 24 * 60 * 60) // 24시간 후
        
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("백그라운드 태스크 스케줄링 실패: \(error)")
        }
    }
}
