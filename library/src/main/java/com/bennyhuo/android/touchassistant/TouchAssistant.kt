package com.bennyhuo.android.touchassistant

import android.app.Application
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
object TouchAssistant {
    private const val STATE_RELEASED = 0
    private const val STATE_INITIALIZING = 1
    private const val STATE_INITIALIZED = 2

    private var binder: AssistantBinder? = null
    private var application: Application? = null

    private var state = STATE_RELEASED
    private val doOnBind = mutableListOf<() -> Unit>()

    val isShowing: Boolean
        get() = binder?.isTouchAssistantShowing() ?: false

    fun init(
        context: Context,
        entryPageClass: Class<out Page>
    ) {
        this.application = context.applicationContext as Application
        val intent = Intent(context, TouchAssistantService::class.java)
        intent.putExtra(TouchAssistantService.EXTRA_ENTRY_PAGE, entryPageClass)

        context.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is AssistantBinder) {
                    state = STATE_INITIALIZED
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

        state = STATE_INITIALIZING
    }

    fun show() {
        when (state) {
            STATE_INITIALIZING -> doOnBind.add { binder?.showTouchAssistant() }
            STATE_INITIALIZED -> binder?.showTouchAssistant()
        }
    }

    fun dismiss() {
        binder?.dismissTouchAssistant()
    }

    fun release() {
        binder = null
        application?.let { app ->
            app.stopService(Intent(app, TouchAssistantService::class.java))
        }
        application = null
    }
}