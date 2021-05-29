package com.bennyhuo.android.touchassistant

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.bennyhuo.android.touchassistant.content.TouchAssistantService
import com.bennyhuo.android.touchassistant.content.TouchAssistantService.AssistantBinder
import com.bennyhuo.android.touchassistant.page.Page

/**
 * Created by benny on 04/06/2018.
 */
class TouchAssistantApi(
    context: Context,
    private val entryPageClass: Class<out Page>
) {
    private var binder: AssistantBinder? = null
    private val context = context.applicationContext

    val isShowing: Boolean
        get() = binder != null

    fun show() {
        val intent = Intent(context, TouchAssistantService::class.java)
        intent.putExtra(TouchAssistantService.EXTRA_ENTRY_PAGE, entryPageClass)

        context.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is AssistantBinder) {
                    binder = service
                    service.showTouchAssistant()
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }, Context.BIND_AUTO_CREATE)
    }

    fun dismiss() {
        binder?.dismissTouchAssistant()
        binder = null

        context.stopService(Intent(context, TouchAssistantService::class.java))
    }
}