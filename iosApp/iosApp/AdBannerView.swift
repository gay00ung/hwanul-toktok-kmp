import SwiftUI
import GoogleMobileAds

struct AdBannerView: UIViewRepresentable {
    let adUnitID = Bundle.main.object(forInfoDictionaryKey: "ADMOB_BANNER_ID") as? String ?? "ca-app-pub-3940256099942544/2435281174"
    
    func makeUIView(context: Context) -> BannerView {
        let bannerView = BannerView()
        bannerView.adUnitID = adUnitID
        
        // iOS 15+ safe way to get root view controller
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootViewController = windowScene.windows.first?.rootViewController {
            bannerView.rootViewController = rootViewController
        }
        
        bannerView.adSize = AdSizeBanner
        return bannerView
    }
    
    func updateUIView(_ uiView: BannerView, context: Context) {
        let request = Request()
        uiView.load(request)
    }
}

// SwiftUI Wrapper for the banner
struct BannerAd: View {
    var body: some View {
        AdBannerView()
            .frame(width: 320, height: 50)
            .background(Color.clear)
    }
}