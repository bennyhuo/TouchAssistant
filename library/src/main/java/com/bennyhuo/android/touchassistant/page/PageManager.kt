package com.bennyhuo.android.touchassistant.page

import kotlin.reflect.KClass

/**
 * Created by benny on 5/24/17.
 */
interface PageManager {
    fun showPage(clazz: KClass<out Page>)

    fun goBack()

    fun dismiss()
}
