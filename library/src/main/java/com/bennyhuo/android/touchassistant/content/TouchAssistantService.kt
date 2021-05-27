package com.bennyhuo.android.touchassistant.content

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.*
import com.bennyhuo.android.touchassistant.R
import com.bennyhuo.android.touchassistant.page.Page
import com.bennyhuo.android.touchassistant.utils.canDrawOverlays
import com.bennyhuo.android.touchassistant.utils.dip
import com.bennyhuo.android.touchassistant.utils.requestDrawOverlays
import kotlinx.android.synthetic.main.assistive_touch.view.*
import java.lang.IllegalArgumentException
import kotlin.math.abs

class TouchAssistantService : Service(), View.OnTouchListener {
    private var animator: ValueAnimator? = null

    // 窗口的管理者
    private val windowManager by lazy {
        getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private val mSharedPreferences by lazy {
        getSharedPreferences("com.bennyhuo.android.touchassistant", MODE_PRIVATE)
    }

    private lateinit var popup: AssistantPopup

    // 界面布局对象
    private val view by lazy {
        View.inflate(this, R.layout.assistive_touch, null)
    }

    private var isShowing = false

    override fun onBind(intent: Intent): IBinder {
        val mainPageClass = intent.getSerializableExtra(EXTRA_ENTRY_PAGE) as? Class<Page>
            ?: throw IllegalArgumentException("MainPageClass cannot be null!")
        popup = AssistantPopup(this, mainPageClass.kotlin)
        popup.setOnPopupStateChangedListener(object : AssistantPopup.OnPopupStateChangedListener {
            override fun onShow() {
                hideTouchAssistant()
            }

            override fun onDismiss() {
                popup.dismiss()
                showTouchAssistant()
            }
        })
        return AssistantBinder()
    }

    inner class AssistantBinder : Binder() {

        fun showMiniMode() {
            showTouchAssistant()
        }

        fun hideMiniMode() {
            hideTouchAssistant()
        }
    }

    override fun onCreate() {
        super.onCreate()
        touchOffset = dip(touchOffset)
        view.setOnTouchListener(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val outMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(outMetrics)
        widthPixels = outMetrics.widthPixels
        heightPixels = outMetrics.heightPixels
        animateToEdge()
    }

    fun showTouchAssistant() {
        if(!canDrawOverlays()) {
            requestDrawOverlays()
            return
        }

        //if (!TaskManager.isForeground()) return
        if (isShowing) return
        try {
            val outMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(outMetrics)
            widthPixels = outMetrics.widthPixels
            heightPixels = outMetrics.heightPixels
            val params: WindowManager.LayoutParams = WindowManager.LayoutParams()
            params.gravity = Gravity.LEFT + Gravity.TOP
            params.x = mSharedPreferences.getInt("Left", 0)
            params.y = mSharedPreferences.getInt("Top", 0)
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            params.format = PixelFormat.TRANSLUCENT
            params.type = if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }

            windowManager.addView(view, params)
            isShowing = true
            animateToEdge()
        } catch (e: SecurityException) {
            e.printStackTrace()
            showApplicationSettings()
        } catch (e: WindowManager.BadTokenException) {
            e.printStackTrace()
            showApplicationSettings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showApplicationSettings() {
        val intent = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
        intent.data = Uri.parse("package:$packageName")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        view.context.applicationContext.startActivity(intent)
    }

    /**
     * 关闭自定义Toast
     */
    fun hideTouchAssistant() {
        if (!isShowing) return
        try {
            cancelAnimator()
            windowManager.removeView(view)
            isShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 手指按下屏幕的坐标
    private var rawX = 0f
    private var rawY = 0f
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var isStartDragging = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        cancelAnimator()
        try {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 获取按下时的左边
                    rawX = event.rawX
                    rawY = event.rawY
                    startX = event.rawX
                    startY = event.rawY
                    view.touchView.isPressed = true
                }
                MotionEvent.ACTION_UP -> {
                    endX = event.rawX
                    endY = event.rawY
                    if (abs(startX - endX) < touchOffset && Math.abs(startY - endY) < touchOffset) {
                        onClick()
                    }
                    view.touchView.isPressed = false
                    animateToEdge()
                    isStartDragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    // 获取手指新的位置
                    val newRawX: Float = event.rawX
                    val newRawY: Float = event.rawY
                    var moveX = newRawX - rawX
                    var moveY = newRawY - rawY
                    if (!isStartDragging) {
                        val totalMoveX = newRawX - startX
                        val totalMoveY = newRawY - startY
                        if (abs(totalMoveX) > touchOffset || abs(totalMoveY) > touchOffset) {
                            isStartDragging = true
                            moveX = totalMoveX
                            moveY = totalMoveY
                        } else {
                            //否则什么都不做
                            // todo
                            return true
                        }
                    }

                    // 重新设置控件的显示位置
                    val layoutParams = view.layoutParams as WindowManager.LayoutParams
                    layoutParams.x += moveX.toInt()
                    layoutParams.y += moveY.toInt()

                    // 处理手机屏幕移动出界问题
                    if (layoutParams.x < 0) {
                        layoutParams.x = 0
                    }
                    if (layoutParams.y < 0) {
                        layoutParams.y = 0
                    }
                    if (layoutParams.x + view.width > widthPixels) {
                        layoutParams.x = widthPixels - view.width
                    }
                    if (layoutParams.y + view.height > heightPixels - STATUS_BAR_HEIGHT) {
                        layoutParams.y = heightPixels - STATUS_BAR_HEIGHT - view.height
                    }
                    windowManager.updateViewLayout(view, layoutParams)

                    // 将手指的位置更新为新的位置
                    rawX = newRawX
                    rawY = newRawY
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun onClick() {
        val layoutParams: WindowManager.LayoutParams =
            view.layoutParams as WindowManager.LayoutParams
        popup.setAnchor(layoutParams.x + view.width / 2, layoutParams.y + view.height / 2)
        popup.show()
    }

    private fun cancelAnimator() {
        animator?.cancel()
        animator = null
    }

    private fun startAnimator(left0: Int, left1: Int, top0: Int, top1: Int) {
        cancelAnimator()
        val animator = ValueAnimator()
        animator.duration = DURATION.toLong()
        animator.setFloatValues(0f, 100f)
        animator.addUpdateListener { animation ->
            val current: Float = animation.animatedFraction
            val params = view.layoutParams as WindowManager.LayoutParams
            params.x = ((left1 - left0) * current + left0).toInt()
            params.y = ((top1 - top0) * current + top0).toInt()
            windowManager.updateViewLayout(view, params)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                val params: WindowManager.LayoutParams =
                    view.layoutParams as WindowManager.LayoutParams
                mSharedPreferences.edit().putInt("Left", params.x).putInt("Top", params.y).apply()
            }
        })
        animator.start()
        this.animator = animator
    }

    private fun animateToEdge() {
        if (!isShowing) return
        val params: WindowManager.LayoutParams = view.layoutParams as WindowManager.LayoutParams
        var left: Int = params.x
        var top: Int = params.y
        left = if (left > widthPixels / 2) {
            widthPixels
        } else {
            0
        }
        top = if (top > (heightPixels - STATUS_BAR_HEIGHT) / 2) {
            heightPixels - STATUS_BAR_HEIGHT
        } else {
            0
        }
        if (abs(params.x - left) < abs(params.y - top)) {
            top = params.y
        } else {
            left = params.x
        }
        startAnimator(params.x, left, params.y, top)
    }

    companion object {
        private var touchOffset = 10
        private const val DURATION = 200
        private const val STATUS_BAR_HEIGHT = 30

        const val EXTRA_ENTRY_PAGE = "AssistiveTouchService.EntryPage"

        private var widthPixels = 0
        private var heightPixels = 0

    }
}