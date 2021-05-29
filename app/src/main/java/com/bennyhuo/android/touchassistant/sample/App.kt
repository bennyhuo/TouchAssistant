package com.bennyhuo.android.touchassistant.sample

import android.app.Activity
import android.app.Application
import com.bennyhuo.android.activitystack.TaskManager
import com.bennyhuo.android.touchassistant.TouchAssistantApi

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        TaskManager.setup(this)

        TaskManager.addOnApplicationStateChangedListener(object: TaskManager.OnApplicationStateChangedListener{
            val touchAssistantApi = TouchAssistantApi(this@App, MainPage::class.java)

            override fun onBackground() {
                touchAssistantApi.dismiss()
            }

            override fun onForeground() {
                touchAssistantApi.show()
            }

            override fun onLowMemory() {

            }

            override fun onTerminate(reason: Int, throwable: Throwable?) {

            }

            override fun onActivityChanged(
                previousActivity: Activity?,
                currentActivity: Activity?
            ) {

            }

        })
    }

}