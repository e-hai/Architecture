@file:Suppress("ktlint:standard:import-ordering")

package xxx.yyy.zzz.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
// [KIT:mmp:START]
import xxx.yyy.zzz.android.bridge.delegates.AndroidMmpBridge
// [KIT:mmp:END]
// [KIT:push:START]
import androidx.activity.result.contract.ActivityResultContracts
import xxx.yyy.zzz.android.bridge.delegates.AndroidPushBridge
// [KIT:push:END]
import xxx.yyy.zzz.shared.App

/**
 * Android 独立宿主页面，挂载 Compose Multiplatform 根视图与运行时生命周期/深链桥接。
 */
class MainActivity : ComponentActivity() {
    // [KIT:push:START]
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            AndroidPushBridge.onPermissionResult(granted)
        }

    // [KIT:push:END]
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // [KIT:push:START]
        AndroidPushBridge.bindPermissionLauncher(notificationPermissionLauncher)
        AndroidPushBridge.handleNotificationIntent(intent)
        // [KIT:push:END]
        // [KIT:mmp:START]
        AndroidMmpBridge.handleDeepLink(this, intent)
        // [KIT:mmp:END]
        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // [KIT:push:START]
        AndroidPushBridge.handleNotificationIntent(intent)
        // [KIT:push:END]
        // [KIT:mmp:START]
        AndroidMmpBridge.handleDeepLink(this, intent)
        // [KIT:mmp:END]
    }

    // [KIT:push:START]
    override fun onDestroy() {
        AndroidPushBridge.clearPermissionLauncher()
        super.onDestroy()
    }
    // [KIT:push:END]
}
