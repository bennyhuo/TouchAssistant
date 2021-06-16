package com.bennyhuo.android.touchassistant.sample

import android.app.Activity
import android.app.Application
import com.bennyhuo.android.activitystack.TaskManager
import com.bennyhuo.android.touchassistant.TouchAssistant

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        TaskManager.setup(this)
        TouchAssistant.init(this@App, MainPage::class.java)

        TaskManager.addOnApplicationStateChangedListener(object: TaskManager.OnApplicationStateChangedListener{

            override fun onBackground() {
                TouchAssistant.dismiss()
            }

            override fun onForeground() {
                TouchAssistant.show()
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