package com.rijal.salesandcredit.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridView

class WrappingGridView : GridView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(View.MEASURED_SIZE_MASK, MeasureSpec.AT_MOST)
        )
        layoutParams.height = measuredHeight
    }
}