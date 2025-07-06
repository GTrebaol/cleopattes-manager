import androidx.compose.ui.window.ComposeUIViewController
import com.gtreb.cleopattesmanager.App
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
