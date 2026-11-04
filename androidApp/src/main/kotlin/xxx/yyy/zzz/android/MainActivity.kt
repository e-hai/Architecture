package xxx.yyy.zzz.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import xxx.yyy.zzz.shared.App

/**
 * Android 独立宿主页面，挂载 Compose Multiplatform 根视图。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
