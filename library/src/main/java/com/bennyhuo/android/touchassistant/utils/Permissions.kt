package com.bennyhuo.android.touchassistant.utils

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import java.lang.reflect.InvocationTargetException

private const val OP_SYSTEM_ALERT_WINDOW = 24


fun Context.canDrawOverlays(): Boolean {
    //android 6.0及以上的判断条件
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(this)
    } else checkOp(this, OP_SYSTEM_ALERT_WINDOW)
    //android 4.4~6.0的判断条件
}

/**
 * 请求悬浮窗权限
 */
fun Context.requestDrawOverlays() {
    val intent = Intent(
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            "android.settings.action.MANAGE_OVERLAY_PERMISSION"
        } else {
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        },
        Uri.parse("package:$packageName")
    )
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        Log.e(
            "TouchAssistant",
            "No activity to handle intent"
        )
    }
}

private fun checkOp(context: Context, op: Int): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val clazz: Class<*> = AppOpsManager::class.java
        try {
            val method = clazz.getDeclaredMethod(
                "checkOp",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            return AppOpsManager.MODE_ALLOWED == method.invoke(
                manager,
                op,
                Process.myUid(),
                context.packageName
            ) as Int
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }
    return true
}