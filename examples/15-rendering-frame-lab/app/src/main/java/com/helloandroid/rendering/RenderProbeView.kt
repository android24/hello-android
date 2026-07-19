package com.helloandroid.rendering

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlin.math.roundToInt

class RenderProbeView(context: Context) : View(context) {
    private val colors = intArrayOf(
        0xFF315F72.toInt(),
        0xFF8A6A25.toInt(),
        0xFF5D6B4C.toInt(),
        0xFF7A4C63.toInt()
    )
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 16f * resources.displayMetrics.scaledDensity
    }
    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFF2CC.toInt()
    }
    private val rect = RectF()
    private var currentState = ProbeState()

    fun applyState(nextState: ProbeState) {
        if (nextState.colorIndex != currentState.colorIndex) {
            currentState = nextState
            RenderingLabStore.recordProbeEvent("invalidate", "colorIndex=${nextState.colorIndex}")
            invalidate()
            return
        }
        if (nextState.longText != currentState.longText || nextState.largeSize != currentState.largeSize) {
            currentState = nextState
            RenderingLabStore.recordProbeEvent(
                phase = "requestLayout",
                detail = "longText=${nextState.longText}, largeSize=${nextState.largeSize}"
            )
            requestLayout()
            invalidate()
            return
        }
        currentState = nextState
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = if (currentState.largeSize) 230.dp else 152.dp
        val measuredWidth = resolveSize(320.dp, widthMeasureSpec)
        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
        RenderingLabStore.recordProbeEvent(
            phase = "onMeasure",
            detail = "w=$measuredWidth, h=$measuredHeight"
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        RenderingLabStore.recordProbeEvent(
            phase = "onLayout",
            detail = "changed=$changed, size=${right - left}x${bottom - top}"
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        fillPaint.color = colors[currentState.colorIndex % colors.size]
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 24f, 24f, fillPaint)

        canvas.drawCircle(width - 42f, 42f, 22f, badgePaint)
        textPaint.color = 0xFF1E2A2F.toInt()
        canvas.drawText("draw", width - 62f, 48f, textPaint)

        textPaint.color = 0xFFFFFFFF.toInt()
        val title = if (currentState.longText) {
            "长文本：这次变化可能影响测量和布局"
        } else {
            "短文本：更容易只重画"
        }
        canvas.drawText(title, 24f, 48f, textPaint)
        canvas.drawText("color=${currentState.colorIndex % colors.size}", 24f, 84f, textPaint)
        canvas.drawText("largeSize=${currentState.largeSize}", 24f, 120f, textPaint)

        RenderingLabStore.recordProbeEvent(
            phase = "onDraw",
            detail = "color=${currentState.colorIndex % colors.size}, longText=${currentState.longText}"
        )
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).roundToInt()
}
