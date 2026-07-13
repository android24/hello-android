package com.helloandroid.input

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView

class TouchLoggingParentView(context: Context) : FrameLayout(context) {
    var interceptMove: Boolean = false

    init {
        setBackgroundColor(Color.rgb(234, 243, 241))
        isClickable = true
        val child = TouchLoggingChildView(context)
        addView(
            child,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                260
            ).apply {
                leftMargin = 28
                topMargin = 28
                rightMargin = 28
                gravity = Gravity.TOP
            }
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        InputLabStore.recordParentEvent("dispatchTouchEvent", event)
        return super.dispatchTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val shouldIntercept = interceptMove && event.actionMasked == MotionEvent.ACTION_MOVE
        InputLabStore.recordParentEvent("onInterceptTouchEvent", event, intercepted = shouldIntercept)
        return shouldIntercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        InputLabStore.recordParentEvent("onTouchEvent", event)
        return true
    }
}

class TouchLoggingChildView(context: Context) : TextView(context) {
    init {
        text = "原生 Child View\n点按或滑动这里，观察父容器是否拦截 MOVE"
        textSize = 16f
        gravity = Gravity.CENTER
        setTextColor(Color.rgb(29, 43, 39))
        setBackgroundColor(Color.rgb(255, 247, 214))
        isClickable = true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        InputLabStore.recordChildEvent("dispatchTouchEvent", event)
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        InputLabStore.recordChildEvent("onTouchEvent", event)
        return true
    }
}
