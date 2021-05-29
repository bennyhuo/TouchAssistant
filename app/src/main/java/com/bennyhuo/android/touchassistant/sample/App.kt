package com.bennyhuo.android.touchassistant.sample

import android.app.Activity
import android.app.Application
import com.bennyhuo.android.activitystack.TaskManager
import com.bennyhuo.android.touchassistant.TouchAssistant

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        TaskManager.setup(this)

        TaskManager.addOnApplicationStateChangedListener(object: TaskManager.OnApplicationStateChangedListener{
            val touchAssistant = TouchAssistant(this@App, MainPage::class.java)

            override fun onBackground() {
                touchAssistant.dismiss()
            }

            override fun onForeground() {
                touchAssistant.show()
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