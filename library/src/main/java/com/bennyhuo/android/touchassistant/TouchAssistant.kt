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
class TouchAssistant(
    context: Context,
    entryPageClass: Class<out Page>
) {
    private var binder: AssistantBinder? = null
    private val context = context.applicationContext

    private val doOnBind = mutableListOf<() -> Unit>()

    init {
        val intent = Intent(context, TouchAssistantService::class.java)
        intent.putExtra(TouchAssistantService.EXTRA_ENTRY_PAGE, entryPageClass)

        context.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is AssistantBinder) {
                    binder = service
                    // do delayed work.
                    doOnBind.forEach { it.invoke() }
                    doOnBind.clear()
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }, Context.BIND_AUTO_CREATE)
    }

    val isShowing: Boolean
        get() = binder?.isTouchAssistantShowing() ?: false

    fun show() {
        binder?.showTouchAssistant() ?: doOnBind.add {
            binder?.showTouchAssistant()
        }
    }

    fun dismiss() {
        binder?.dismissTouchAssistant()
    }

    fun release() {
        binder = null
        context.stopService(Intent(context, TouchAssistantService::class.java))
    }
}