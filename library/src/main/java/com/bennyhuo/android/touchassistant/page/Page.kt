package com.bennyhuo.android.touchassistant.page

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import kotlin.reflect.KClass

/**
 * Created by benny on 5/24/17.
 */
abstract class Page(protected val pageContext: PageContext) : ContextWrapper(pageContext.context), PageManager by pageContext.pageManager{

    abstract val view: View

    val context: Context = pageContext.context

    open fun onEnter() = Unit

    open fun onExit() = Unit

    open fun onRelease() = Unit

}