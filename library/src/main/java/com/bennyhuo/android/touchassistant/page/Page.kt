package com.bennyhuo.android.touchassistant.page

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import kotlin.reflect.KClass

/**
 * Created by benny on 5/24/17.
 */
abstract class Page : ContextWrapper(null), PageManager{

    /**
     * Available after attachContext, e.g. in onCreate.
     */
    lateinit var pageContext: PageContext

    abstract val view: View

    val context: Context by lazy {
        pageContext.context
    }

    open fun onCreate() = Unit

    open fun onEnter() = Unit

    open fun onExit() = Unit

    open fun onRelease() = Unit

    internal fun performCreate(pageContext: PageContext) {
        if (!this::pageContext.isInitialized) {
            attachContext(pageContext)
            onCreate()
        }
    }

    private fun attachContext(pageContext: PageContext) {
        this.pageContext = pageContext
        super.attachBaseContext(pageContext.context)
    }

    override fun showPage(clazz: KClass<out Page>, clearTop: Boolean) {
        pageContext.pageManager.showPage(clazz, clearTop)
    }

    override fun showPage(page: Page) {
        pageContext.pageManager.showPage(page)
    }

    override fun goBack() {
        pageContext.pageManager.goBack()
    }

    override fun dismiss() {
        pageContext.pageManager.dismiss()
    }

    fun View.setDismissOnClickListener(onClickListener: View.OnClickListener) {
        setOnClickListener {
            onClickListener.onClick(it)
            dismiss()
        }
    }
}
