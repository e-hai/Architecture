package xxx.yyy.zzz.core.analytics

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

/**
 * Firebase Analytics 助手实现
 * 封装 Firebase Analytics 的具体逻辑
 */
class FirebaseAnalyticsHelper(private val context: Context) : AnalyticsHelper {
    private val firebaseAnalytics by lazy {
        // 确保 Firebase 已初始化
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        Firebase.analytics
    }

    /**
     * 记录自定义事件
     * @param event 分析事件对象
     */
    override fun logEvent(event: AnalyticsEvent) {
        firebaseAnalytics.logEvent(event.name) {
            event.params.forEach { (key, value) ->
                param(key, value)
            }
        }
    }


    /**
     * 设置用户属性
     * @param name 属性名称
     * @param value 属性值
     */
    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    /**
     * 设置用户 ID
     * @param userId 用户 ID
     */
    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    /**
     * 启用/禁用分析收集
     * @param enabled 是否启用
     */
    override fun setAnalyticsCollectionEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
}
