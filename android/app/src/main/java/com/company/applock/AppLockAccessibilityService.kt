package com.company.applock
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
class AppLockAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            if (packageName == "com.company.applock" || packageName == "com.android.systemui") return
            val prefs = getSharedPreferences("AppLockPrefs", MODE_PRIVATE)
            val lockedPackages = prefs.getStringSet("locked_packages", emptySet()) ?: emptySet()
            if (lockedPackages.contains(packageName)) {
                val expiryTime = prefs.getLong("unlock_expiry", 0L)
                if (System.currentTimeMillis() > expiryTime) {
                    val intent = Intent(this, LockScreenActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
                    startActivity(intent)
                }
            }
        }
    }
    override fun onInterrupt() {}
}
