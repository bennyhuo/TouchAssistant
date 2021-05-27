package com.bennyhuo.android.touchassistant

import android.content.*
import android.os.IBinder
import com.bennyhuo.android.touchassistant.content.TouchAssistantService
import com.bennyhuo.android.touchassistant.content.TouchAssistantService.AssistantBinder
import com.bennyhuo.android.touchassistant.page.Page

/**
 * Created by benny on 04/06/2018.
 */
class TouchAssistantApi(
    private val contextWrapper: ContextWrapper,
    private val entryPageClass: Class<out Page>
) {
    private var binder: AssistantBinder? = null
    fun show() {
        val intent = Intent(contextWrapper, TouchAssistantService::class.java)
        intent.putExtra(TouchAssistantService.EXTRA_ENTRY_PAGE, entryPageClass)

        contextWrapper.bindService(intent, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service is AssistantBinder) {
                    binder = service
                    service.showMiniMode()
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }, Context.BIND_AUTO_CREATE)
    }

    fun dismiss() {
        if (binder != null) {
            binder!!.hideMiniMode()
            contextWrapper.stopService(Intent(contextWrapper, TouchAssistantService::class.java))
        }
    }
}