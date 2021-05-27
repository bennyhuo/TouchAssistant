package com.bennyhuo.android.touchassistant.widget

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.RelativeLayout

/**
 * Created by benny on 5/25/17.
 */
typealias OnBackPressedListener = () -> Unit

class AssistantDecorView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

    private var listener: OnBackPressedListener? = null

    fun onBackPressed(listener: OnBackPressedListener) {
        this.listener = listener
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        //键盘的删除和回车都是通过这个方法分发的
        return if (listener != null && event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            listener!!.invoke()
            true
        } else {
            super.dispatchKeyEvent(event)
        }
    }
}
