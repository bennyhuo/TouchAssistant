package com.bennyhuo.android.touchassistant.content

import android.animation.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import com.bennyhuo.android.touchassistant.R
import com.bennyhuo.android.touchassistant.page.Page
import com.bennyhuo.android.touchassistant.page.PageContext
import com.bennyhuo.android.touchassistant.page.PageManager
import kotlinx.android.synthetic.main.assistant_popup.view.*
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by benny on 5/17/17.
 */
class AssistantPopup(private val context: Context, private val mainPageClass: KClass<Page>) :
    PageManager {

    private var isAdded = false
    private val container by lazy { contentView.container }
    private val contentView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.assistant_popup, null)
    }
    private val windowManager: WindowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    private var animatorSet: AnimatorSet? = null

    private var anchorX: Int = 0
    private var anchorY: Int = 0
    private val pages = Stack<Page>()
    private val pageContext = PageContext(context, this)

    init {
        contentView.setOnClickListener { dismiss() }
        contentView.decorView.onBackPressed {
            goBack()
        }
        initPage()
    }

    //region pages

    val currentPage: Page
        get() = pages.lastElement()

    private fun initPage() {
        mainPageClass.create(pageContext).apply {
            doShowPage(this)
        }.let(pages::push).onEnter()
    }

    private fun KClass<out Page>.create(pageContext: PageContext) =
        java.getDeclaredConstructor(PageContext::class.java).newInstance(pageContext)

    override fun showPage(clazz: KClass<out Page>, clearTop: Boolean) {
        var existsPage: Page? = null
        if (clearTop) {
            for (page in pages) {
                if (page::class == clazz) {
                    existsPage = page
                    break
                }
            }
            if (existsPage != null) {
                while (pages.peek() != existsPage) {
                    pages.pop().onRelease()
                }
            }
        }

        val page = existsPage ?: clazz.create(pageContext)
        showPage(page)
    }

    override fun showPage(page: Page) {
        if (currentPage == page) return
        currentPage.onExit()
        doShowPage(page)
        pages.push(page)
    }

    private fun doShowPage(page: Page) {
        val scaleIn = AnimatorInflater.loadAnimator(pageContext.context, R.animator.scale_in)
        val scaleOut = AnimatorInflater.loadAnimator(pageContext.context, R.animator.scale_out)
        scaleOut.setTarget(container)
        scaleIn.setTarget(container)
        scaleOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                container.removeAllViews()
                container.addView(page.view, -1, -1)
                scaleIn.start()

                page.onEnter()
            }
        })
        scaleOut.start()
    }

    override fun goBack() {
        if (pages.size > 1) {
            currentPage.onExit()
            pages.pop().onRelease()
            doShowPage(pages.peek())
        } else {
            dismiss()
        }

    }
    //endregion

    //region show&dismiss
    fun setAnchor(x: Int, y: Int) {
        this.anchorX = x
        this.anchorY = y
    }

    fun show() {
        try {
            if (isAdded) return
            val params = WindowManager.LayoutParams()
            params.height = -1
            params.width = -1
            //必须使用这个窗口类型，否则无法与输入法联动
            params.type = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            //输入法模式必须在窗口类型为 TYPE_PHONE 时有效，TOAST 无效
            params.softInputMode =
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
            params.format = PixelFormat.TRANSLUCENT
            //不需要输入法时设置下面的 flag
            //params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
            windowManager.addView(contentView, params)
            contentView.clearFocus()
            playShowAnimation()
            isAdded = true
            onPopupStateChangedListener?.onShow()
            currentPage.onEnter()
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse("package:${context.packageName}")
            pageContext.context.startActivity(intent)
        }

    }

    private fun playShowAnimation() {
        container.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {

                cancelAnimator()
                val dx = anchorX - container.left - container.width / 2
                val dy = anchorY - container.top - container.height / 2

                container.pivotX = (container.width / 2).toFloat()
                container.pivotY = (container.height / 2).toFloat()

                val translationXAnimator =
                    ObjectAnimator.ofFloat(container, "translationX", dx.toFloat(), 0f)
                val translationYAnimator =
                    ObjectAnimator.ofFloat(container, "translationY", dy.toFloat(), 0f)

                val scaleXAnimator = ObjectAnimator.ofFloat(container, "scaleX", 0f, 1f)
                val scaleYAnimator = ObjectAnimator.ofFloat(container, "scaleY", 0f, 1f)

                val animatorSet = AnimatorSet()
                animatorSet.playTogether(
                    translationXAnimator,
                    translationYAnimator,
                    scaleXAnimator,
                    scaleYAnimator
                )
                animatorSet.duration = DURATION.toLong()
                animatorSet.start()

                this@AssistantPopup.animatorSet = animatorSet

                container.removeOnLayoutChangeListener(this)
            }
        })
    }

    override fun dismiss() {
        try {
            if (!isAdded) return
            currentPage.onExit()
            playDismissAnimation()
            isAdded = false
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun playDismissAnimation() {
        cancelAnimator()
        val dx = anchorX - container.left - container.width / 2
        val dy = anchorY - container.top - container.height / 2

        container.pivotX = (container.width / 2).toFloat()
        container.pivotY = (container.height / 2).toFloat()

        val translationXAnimator =
            ObjectAnimator.ofFloat(container, "translationX", 0f, dx.toFloat())
        val translationYAnimator =
            ObjectAnimator.ofFloat(container, "translationY", 0f, dy.toFloat())

        val scaleXAnimator = ObjectAnimator.ofFloat(container, "scaleX", 1f, 0f)
        val scaleYAnimator = ObjectAnimator.ofFloat(container, "scaleY", 1f, 0f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            translationXAnimator,
            translationYAnimator,
            scaleXAnimator,
            scaleYAnimator
        )
        animatorSet.duration = DURATION.toLong()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                windowManager.removeView(contentView)
                animation.removeAllListeners()
                onPopupStateChangedListener?.onDismiss()
            }
        })
        animatorSet.start()
        this@AssistantPopup.animatorSet = animatorSet
    }

    private fun cancelAnimator() {
        if (animatorSet != null) {
            animatorSet!!.cancel()
            animatorSet = null
        }
    }
    //endregion

    interface OnPopupStateChangedListener {
        fun onShow()

        fun onDismiss()
    }

    private var onPopupStateChangedListener: OnPopupStateChangedListener? = null

    fun setOnPopupStateChangedListener(onPopupStateChangedListener: OnPopupStateChangedListener) {
        this.onPopupStateChangedListener = onPopupStateChangedListener
    }

    companion object {
        private val DURATION = 200
    }
}

