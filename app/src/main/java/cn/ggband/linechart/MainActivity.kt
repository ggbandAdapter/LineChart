package cn.ggband.linechart

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.ggband.library.LineChartView
import cn.ggband.linechart.bean.DeviceApStatisticsRes
import cn.ggband.linechart.bean.DeviceScoreRes
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat

/**
 * LineChartView demo
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
        setData()
    }


    /**
     * 初始化View
     */
    private fun initView() {
        //lineChartWiFi
        lineChartWiFi.run {
            leftStepValue = 20
            maxLeftValue = 100
            addAxises(
                LineChartView.AxisParameter().apply {
                    axisType = LineChartView.AxisType.AxisLeft
                    axisColor = Color.parseColor("#ff000000")
                    axisTextSize = 12.sp2px()
                    axisStepValue = 20f
                }
            )
            addAxises(
                LineChartView.AxisParameter().apply {
                    axisType = LineChartView.AxisType.AxisBottom
                    axisColor = Color.parseColor("#993c3c43")
                    axisTextSize = 12.sp2px()
                    axisStepValue = 4f
                    axisDisPlay = object : LineChartView.IAxisDisPlay {
                        override fun onDisPlayFormat(index: Int, value: Long): String {
                            val arrDesc = arrayOf("24 Hours Ago", "12 Hours Ago", "Now")
                            return arrDesc[index]
                        }
                    }
                }
            )
        }

        //lineChartUsage
        lineChartUsage.run {
            leftStepValue = 20
            maxLeftValue = 100
            addAxises(
                LineChartView.AxisParameter().apply {
                    axisType = LineChartView.AxisType.AxisLeft
                    axisColor = Color.parseColor("#ff000000")
                    axisTextSize = 12.sp2px()
                    axisStepValue = 20f
                    axisDisPlay = object : LineChartView.IAxisDisPlay {
                        override fun onDisPlayFormat(index: Int, value: Long): String {
                            return "${value}%"
                        }
                    }
                }
            )
            addAxises(
                LineChartView.AxisParameter().apply {
                    axisType = LineChartView.AxisType.AxisBottom
                    axisColor = Color.parseColor("#993c3c43")
                    axisTextSize = 12.sp2px()
                    axisStepValue = 4f
                    axisDisPlay = object : LineChartView.IAxisDisPlay {
                        override fun onDisPlayFormat(index: Int, value: Long): String {
                            return SimpleDateFormat("HH:mm").format(value)
                        }
                    }
                }
            )
        }

    }

    /**
     * 绑定数据
     */
    private fun setData() {
        //lineChartWiFi
        lineChartWiFi.run {
            val wifiScoreStats = DeviceScoreRes.buildMoKeRes()
            bottomStepValue =
                (wifiScoreStats.last().reportTime - wifiScoreStats.first().reportTime) / 2
            maxBottomValue = wifiScoreStats.last().reportTime
            minBottomValue = wifiScoreStats.first().reportTime
            clearLine()
            addLine(LineChartView.PathLineParameter().apply {
                values =
                    wifiScoreStats.map {
                        Pair(
                            it.reportTime.toFloat(),
                            it.wirelessExperienceRating
                        )
                    }
                pColor = Color.parseColor("#5B8FF9")
                strokeWidth = 2f
                shader = LinearGradient(
                    0F,
                    0F,
                    0F,
                    179.dp2Px().toFloat(),
                    intArrayOf(
                        Color.parseColor("#7E006FFF"),
                        Color.parseColor("#00006FFF")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            })
        }


        //lineChartUsage
        lineChartUsage.run {
            val apStats = DeviceApStatisticsRes.buildMoKeRes()
            lineChartUsage.bottomStepValue =
                (apStats.last().reportTime - apStats.first().reportTime) / 6
            lineChartUsage.maxBottomValue = apStats.last().reportTime
            lineChartUsage.minBottomValue = apStats.first().reportTime
            lineChartUsage.clearLine()
            lineChartUsage.addLine(LineChartView.PathLineParameter().apply {
                values =
                    apStats.map { Pair(it.reportTime.toFloat(), it.apCpuPercent.toFloat()) }
                pColor = Color.parseColor("#007AFF")
                strokeWidth = 2f
            })

            lineChartUsage.addLine(LineChartView.PathLineParameter().apply {
                values =
                    apStats.map { Pair(it.reportTime.toFloat(), it.apMemPercent.toFloat()) }
                pColor = Color.parseColor("#34C759")
                strokeWidth = 2f
            })
        }
    }

    private fun Number.sp2px(): Float {
        val scale = resources.displayMetrics.scaledDensity
        return this.toFloat() * scale + 0.5f
    }

    private fun Number.dp2Px(): Int {
        val scale = resources.displayMetrics.density
        return (this.toFloat() * scale + 0.5f).toInt()
    }
}
