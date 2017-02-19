package com.incentive.yellowpages.misc.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewOutlineProvider
import android.widget.RelativeLayout
import com.incentive.yellowpages.R

/**
 * An extension to [RelativeLayout] which has a foreground drawable.

 * @author  Nick Butcher
 */
class ForegroundRelativeLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var foreground: Drawable? = null

    init {

        val a = context.obtainStyledAttributes(attrs, R.styleable.ForegroundRelativeLayout)

        val d = a.getDrawable(R.styleable.ForegroundRelativeLayout_android_foreground)
        if (d != null) {
            setForeground(d)
        }
        a.recycle()
        outlineProvider = ViewOutlineProvider.BOUNDS
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (foreground != null) {
            foreground!!.setBounds(0, 0, w, h)
        }
    }

    override fun hasOverlappingRendering(): Boolean {
        return false
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === foreground
    }

    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        if (foreground != null) foreground!!.jumpToCurrentState()
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (foreground != null && foreground!!.isStateful) {
            foreground!!.state = drawableState
        }
    }

    /**
     * Returns the drawable used as the foreground of this view. The
     * foreground drawable, if non-null, is always drawn on top of the children.

     * @return A Drawable or null if no foreground was set.
     */
    override fun getForeground(): Drawable? {
        return foreground
    }

    /**
     * Supply a Drawable that is to be rendered on top of all of the child
     * views within this layout.  Any padding in the Drawable will be taken
     * into account by ensuring that the children are inset to be placed
     * inside of the padding area.

     * @param drawable The Drawable to be drawn on top of the children.
     */
    override fun setForeground(drawable: Drawable) {
        if (foreground !== drawable) {
            if (foreground != null) {
                foreground!!.callback = null
                unscheduleDrawable(foreground)
            }

            foreground = drawable

            if (foreground != null) {
                foreground!!.setBounds(left, top, right, bottom)
                setWillNotDraw(false)
                foreground!!.callback = this
                if (foreground!!.isStateful) {
                    foreground!!.state = drawableState
                }
            } else {
                setWillNotDraw(true)
            }
            invalidate()
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (foreground != null) {
            foreground!!.draw(canvas)
        }
    }

    override fun drawableHotspotChanged(x: Float, y: Float) {
        super.drawableHotspotChanged(x, y)
        if (foreground != null) {
            foreground!!.setHotspot(x, y)
        }
    }

}
