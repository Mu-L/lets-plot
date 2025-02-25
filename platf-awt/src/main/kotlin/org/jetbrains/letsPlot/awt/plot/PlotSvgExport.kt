/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.awt.plot

import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.core.util.PlotSvgExportCommon
import org.jetbrains.letsPlot.awt.util.RGBEncoderAwt

object PlotSvgExport {
    /**
     * @param plotSpec Raw specification of a plot.
     * @param plotSize Desired plot size.
     * @param useCssPixelatedImageRendering true for CSS style "pixelated", false for SVG style "optimizeSpeed". Used for compatibility.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun buildSvgImageFromRawSpecs(
        plotSpec: MutableMap<String, Any>,
        plotSize: DoubleVector? = null,
        useCssPixelatedImageRendering: Boolean = true
    ): String {
        return PlotSvgExportCommon.buildSvgImageFromRawSpecs(
            plotSpec = plotSpec,
            plotSize = plotSize,
            rgbEncoder = RGBEncoderAwt(),
            useCssPixelatedImageRendering
        )
    }
}
