/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.geom

import org.jetbrains.letsPlot.core.plot.base.*
import org.jetbrains.letsPlot.core.plot.base.geom.util.GeomUtil
import org.jetbrains.letsPlot.core.plot.base.geom.util.LinesHelper
import org.jetbrains.letsPlot.core.plot.base.geom.util.PathData
import org.jetbrains.letsPlot.core.plot.base.geom.util.TargetCollectorHelper
import org.jetbrains.letsPlot.core.plot.base.render.LegendKeyElementFactory
import org.jetbrains.letsPlot.core.plot.base.render.SvgRoot

open class PathGeom : GeomBase() {

    var animation: Any? = null
    var flat: Boolean = false
    var geodesic: Boolean = false

    override val legendKeyElementFactory: LegendKeyElementFactory
        get() = HLineGeom.LEGEND_KEY_ELEMENT_FACTORY

    protected open fun dataPoints(aesthetics: Aesthetics): Iterable<DataPointAesthetics> {
        return aesthetics.dataPoints()
    }

    override fun buildIntern(
        root: SvgRoot,
        aesthetics: Aesthetics,
        pos: PositionAdjustment,
        coord: CoordinateSystem,
        ctx: GeomContext
    ) {

        val dataPoints = dataPoints(aesthetics)
        val linesHelper = LinesHelper(pos, coord, ctx)
        linesHelper.setResamplingEnabled(!coord.isLinear && !flat)

        val closePath = linesHelper.meetsRadarPlotReq()
        val pathDataMap: Map<Int, List<PathData>> = linesHelper.createPathData(dataPoints, GeomUtil.TO_LOCATION_X_Y, closePath)

        pathDataMap.forEach { (key, pathDataList) ->
            pathDataList.forEach { pathData ->
                val targetCollectorHelper = TargetCollectorHelper(GeomKind.PATH, ctx)
                val curPath = mapOf(key to listOf(pathData))
                targetCollectorHelper.addVariadicPaths(curPath)

                val svgPath = linesHelper.renderPaths(curPath, filled = false)
                root.appendNodes(svgPath)
            }
        }
    }


    companion object {
        const val HANDLES_GROUPS = true
    }
}
