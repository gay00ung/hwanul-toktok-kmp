import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        ZStack {
            (colorScheme == .dark ?
             Color(red: 0.10, green: 0.10, blue: 0.10) : // DarkSurface (#1A1A1A)
             Color(red: 0.969, green: 0.976, blue: 0.984)) // Gray100 (#F7FAFC)
                .ignoresSafeArea(.all)
            
            ComposeView()
                .ignoresSafeArea(.keyboard)
            
            VStack {
                Spacer()
                AdBannerView()
                    .frame(width: 320, height: 50)
                    .background(Color.clear)
                    .padding(.bottom, 56)
            }
        }
    }
}



