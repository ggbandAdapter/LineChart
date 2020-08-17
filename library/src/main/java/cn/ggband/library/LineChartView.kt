package cn.ggband.library

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View


/**
 * author: ggband
 * date：2020/5/30
 * email：pxb@ggband.cn
 * desc: 线统计图
 */
class LineChartView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var maxLeftValue = 500L
    var leftStepValue = 100L
    private var maxRightValue = 5L
    private var rightStepValue = 1L
    var maxBottomValue = 24L
    var minBottomValue = 0L
    var bottomStepValue = 4L
    private var gridStepHeight = 0f
    private var gridStepWidth = 0f
    private val mGridRectF = RectF()
    private var mGridPaintWidth = 1f
    private var mGridAxisMargin = 0
    private var mGridColor = Color.parseColor("#3a3c3c43")
    private var mAxises = ArrayList<Axis>()
    private var mPathLines = ArrayList<PathLine>()
    private lateinit var mNotDataPaint: Paint
    private val mPath = Path()
    private lateinit var mGridPaint: Paint
    private var leftAxisWidth = 0f
    private var rightAxisWidth = 0f
    private var bottomAxisHeight = 0f
    private var topAxisHeight = 0f
    //没有数据的描述提示
    private var emptyDesc = "No Chart data available"

    init {
        initView()
        initPaint()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mPathLines.isEmpty()) {
            canvas.drawText(
                emptyDesc,
                (width - mNotDataPaint.measureText(emptyDesc)) / 2,
                (height - (mNotDataPaint.ascent() + mNotDataPaint.descent())) / 2,
                mNotDataPaint
            )
        } else {
            drawGrid(canvas)
            drawMainPath(canvas)
            drawAxises(canvas)
        }
    }

    private fun initView() {
        mGridAxisMargin = 5.dp2Px()
    }

    private fun initPaint() {
        mGridPaint = Paint()
        mGridPaint.color = mGridColor
        mGridPaint.strokeWidth = mGridPaintWidth
        mGridPaint.isAntiAlias = true
        mNotDataPaint = Paint()
        mNotDataPaint.textSize = 16.sp2px()
        mNotDataPaint.isAntiAlias = true
        mNotDataPaint.color = Color.parseColor("#ff000000")
    }

    private fun drawGrid(canvas: Canvas) {
        val fYCount = maxLeftValue / leftStepValue
        for (i in 0..fYCount) {
            val y = when (i) {
                0L -> {
                    mGridPaintWidth / 2 + mGridRectF.top
                }
                fYCount -> {
                    mGridRectF.bottom - mGridPaintWidth / 2
                }
                else -> {
                    gridStepHeight * i + mGridRectF.top
                }
            }
            canvas.drawLine(mGridRectF.left, y, mGridRectF.right, y, mGridPaint)
        }
        val fXCount = (maxBottomValue - minBottomValue) / bottomStepValue
        for (i in 0..fXCount) {
            val x = when (i) {
                0L -> {
                    mGridPaintWidth / 2 + leftAxisWidth
                }
                fXCount -> {
                    mGridRectF.right - mGridPaintWidth / 2
                }
                else -> {
                    i * gridStepWidth + mGridRectF.left
                }
            }
            canvas.drawLine(
                x,
                mGridRectF.top,
                x,
                mGridRectF.bottom,
                mGridPaint
            )
        }
    }

    private fun drawMainPath(canvas: Canvas) {
        mPathLines.forEach { pathLine ->
            mPath.reset()
            val points = pathLine.getPathPoints()
            if (points.isNullOrEmpty()) return
            val firstPoint = points.first()
            mPath.moveTo(firstPoint.x, firstPoint.y)
            //折线
            // points.forEach { mPath.lineTo(it.x, it.y) }
            //曲线
            var startPoint = PointF()
            var endPoint = PointF()
            val p3 = PointF()
            val p4 = PointF()
            for (i in 0 until points.size - 1) {
                startPoint = points[i]
                endPoint = points[i + 1]
                val wt = (startPoint.x + endPoint.x) / 2
                p3.y = startPoint.y
                p3.x = wt
                p4.y = endPoint.y
                p4.x = wt
                mPath.cubicTo(p3.x, p3.y, p4.x, p4.y, endPoint.x, endPoint.y)
            }
            canvas.drawPath(mPath, pathLine.getPathPaint())
            //填充渐变色
            pathLine.getShaderPaint()?.let {
                val lastPoint = points.last()
                mPath.lineTo(lastPoint.x, mGridRectF.bottom)
                mPath.lineTo(points.first().x, mGridRectF.bottom)
                canvas.drawPath(mPath, it)
            }
        }
    }

    /**
     * 绘制坐标
     */
    private fun drawAxises(canvas: Canvas) {
        mAxises.forEach { axis ->
            axis.cDrawParameter().forEach {
                canvas.drawText(it.axisText, it.x, it.y, axis.getAxisPaint())
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refreshParams()
    }

    fun addLine(line: PathLineParameter) {
        mPathLines.add(PathLine(line))
        refreshParams()
        invalidate()
    }

    fun clearLine() {
        mPathLines.clear()
        invalidate()
    }

    private fun refreshParams() {
        mAxises.forEach { axis ->
            when (axis.parameter.axisType) {
                AxisType.AxisLeft -> {
                    topAxisHeight = axis.getTextHeight()
                    leftAxisWidth = axis.cTextWidths().maxBy { it } ?: 0f
                    if (leftAxisWidth != 0f) leftAxisWidth += mGridAxisMargin
                }
                AxisType.AxisRight -> {
                    rightAxisWidth = axis.cTextWidths().maxBy { it } ?: 0f
                    if (rightAxisWidth != 0f) rightAxisWidth += mGridAxisMargin
                }
                else -> {
                    bottomAxisHeight = axis.getTextHeight() + mGridAxisMargin
                }
            }
        }
        val lastBottomAxiseWidth =
            mAxises.find { it.parameter.axisType == AxisType.AxisBottom }?.cTextWidths()?.last()
                ?: 0f
        val firstBottomAxiseWidth =
            mAxises.find { it.parameter.axisType == AxisType.AxisBottom }?.cTextWidths()?.first()
                ?: 0f
        rightAxisWidth = Math.max(lastBottomAxiseWidth / 2, rightAxisWidth)
        leftAxisWidth = Math.max(firstBottomAxiseWidth / 2, leftAxisWidth)
        val gridRight = width.toFloat() - rightAxisWidth
        val gridBottom = height.toFloat() - bottomAxisHeight
        val gridLeft = leftAxisWidth
        val gridTop = topAxisHeight / 2
        mGridRectF.run {
            left = gridLeft
            right = gridRight
            top = gridTop
            bottom = gridBottom
        }
        gridStepHeight = mGridRectF.height() / (maxLeftValue / leftStepValue)
        gridStepWidth = mGridRectF.width() / ((maxBottomValue - minBottomValue) / bottomStepValue)
    }

    fun addAxises(axisParameter: AxisParameter) {
        mAxises.add(Axis(axisParameter))
    }

    class AxisParameter {
        var axisType = AxisType.AxisLeft
        var axisColor: Int = 0
        var axisTextSize = 12f
        var axisStepValue = 100f
        var axisDisPlay: IAxisDisPlay = object : IAxisDisPlay {}
    }

    inner class Axis(val parameter: AxisParameter) {
        private var axisPaint: Paint? = null
        private val drawParameters = ArrayList<DrawParameter>()

        fun getAxisPaint(): Paint {
            if (axisPaint != null) return axisPaint!!
            axisPaint = Paint().apply {
                color = parameter.axisColor
                textSize = parameter.axisTextSize
                isAntiAlias = true
            }
            return axisPaint!!
        }

        fun getTextHeight(): Float {
            val fontMetrics = getAxisPaint().fontMetrics
            return fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
        }

        fun cTextWidths(): List<Float> {
            val textWidths = ArrayList<Float>()
            when (parameter.axisType) {
                AxisType.AxisLeft -> {
                    val fYCount = maxLeftValue / leftStepValue
                    for (i in 0..fYCount) {
                        val value = maxLeftValue - (i * leftStepValue)
                        val axisText = parameter.axisDisPlay.onDisPlayFormat(i.toInt(), value)
                        textWidths.add(getAxisPaint().measureText(axisText))
                    }
                }
                AxisType.AxisBottom -> {
                    val fXCount = (maxBottomValue - minBottomValue) / bottomStepValue
                    for (i in 0..fXCount) {
                        val value = i * bottomStepValue + minBottomValue
                        val axisText = parameter.axisDisPlay.onDisPlayFormat(i.toInt(), value)
                        textWidths.add(getAxisPaint().measureText(axisText))
                    }
                }
                else -> {
                    val fYCount = maxRightValue / rightStepValue
                    for (i in 0..fYCount) {
                        val value = maxRightValue - (i * rightStepValue)
                        val axisText = parameter.axisDisPlay.onDisPlayFormat(i.toInt(), value)
                        textWidths.add(getAxisPaint().measureText(axisText))
                    }

                }
            }
            return textWidths
        }

        fun cDrawParameter(): List<DrawParameter> {
            if (drawParameters.isNotEmpty()) return drawParameters
            when (parameter.axisType) {
                AxisType.AxisLeft -> {
                    val fYCount = maxLeftValue / leftStepValue
                    for (i in 0..fYCount) {
                        val value = maxLeftValue - (i * leftStepValue)
                        val axisText = parameter.axisDisPlay.onDisPlayFormat(i.toInt(), value)
                        val y = (i * gridStepHeight) + getTextHeight() / 4 + mGridRectF.top
                        val x =
                            leftAxisWidth - getAxisPaint().measureText(axisText) - mGridAxisMargin
                        drawParameters.add(DrawParameter(axisText, x, y))
                    }
                }
                AxisType.AxisBottom -> {
                    val fXCount = (maxBottomValue - minBottomValue) / bottomStepValue
                    for (i in 0..fXCount) {
                        val value = i * bottomStepValue + minBottomValue
                        val axisText = parameter.axisDisPlay.onDisPlayFormat(i.toInt(), value)
                        val x = (i * gridStepWidth) + mGridRectF.left - getAxisPaint()
                            .measureText(axisText) / 2
                        val y = height.toFloat() - getAxisPaint().descent()
                        drawParameters.add(DrawParameter(axisText, x, y))
                    }
                }
                else -> {
                    val fYCount = maxRightValue / rightStepValue
                    for (i in 0..fYCount) {
                        val value = maxRightValue - (i * rightStepValue)
                        val axisText = parameter.axisDisPlay.onDisPlayFormat(i.toInt(), value)
                        val x = mGridRectF.right + mGridAxisMargin
                        val y = (i * gridStepHeight) + getTextHeight() / 4 + mGridRectF.top
                        drawParameters.add(DrawParameter(axisText, x, y))
                    }
                }
            }
            return drawParameters;
        }

        init {
            axisPaint = null
            drawParameters.clear()
        }

        inner class DrawParameter(var axisText: String, var x: Float, var y: Float)
    }

    class PathLineParameter {
        //值
        var values: List<Pair<Float, Float>>? = null
        //坐标值
        var pathPoints: List<PointF>? = null
        //线条颜色
        var pColor = 0
        //背景
        var shader: Shader? = null
        //曲线配置
        var pathEffect: PathEffect? = null
        //线宽
        var strokeWidth = 2f
    }


    inner class PathLine(private val parameter: PathLineParameter) {

        private var pathPaint: Paint? = null

        fun getPathPaint(): Paint {
            if (pathPaint == null) {
                pathPaint = Paint().apply {
                    strokeWidth = parameter.strokeWidth
                    color = parameter.pColor
                    pathEffect = parameter.pathEffect
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                }
            }
            return pathPaint!!
        }

        fun getShaderPaint(): Paint? {
            if (parameter.shader == null) return null
            return Paint().apply {
                style = Paint.Style.FILL
                isAntiAlias = true
                shader = parameter.shader
            }
        }

        fun getPathPoints(): List<PointF>? {
            if (parameter.values.isNullOrEmpty()) return null
            if (parameter.pathPoints == null) {
                parameter.pathPoints = parameter.values?.map { calculationPoint(it) }
            }
            return parameter.pathPoints
        }

        private fun calculationPoint(xy: Pair<Float, Float>): PointF {
            val xP =
                (mGridRectF.width() * (xy.first - minBottomValue) / (maxBottomValue - minBottomValue)) + mGridRectF.left
            val yP =
                mGridRectF.height() - ((mGridRectF.height() * xy.second / maxLeftValue)) + mGridRectF.top
            return PointF(xP, yP)
        }
    }

    /**
     * 坐标刻度类型
     */
    enum class AxisType {
        AxisLeft, AxisBottom, AxisRight
    }

    /**
     * 坐标刻度值格式化显示
     */
    interface IAxisDisPlay {
        fun onDisPlayFormat(index: Int, value: Long): String {
            return value.toString()
        }
    }

    private fun Number.dp2Px(): Int {
        val scale = context.resources.displayMetrics.density
        return (this.toFloat() * scale + 0.5f).toInt()
    }

    private fun Number.sp2px(): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return this.toFloat() * scale + 0.5f
    }

}






