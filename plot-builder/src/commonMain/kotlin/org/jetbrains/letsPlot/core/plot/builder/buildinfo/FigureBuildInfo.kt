/*
 * Copyright (c) 2025. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.builder.buildinfo

import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.core.plot.builder.FigureSvgRoot
import org.jetbrains.letsPlot.core.plot.builder.GeomLayer
import org.jetbrains.letsPlot.core.plot.builder.layout.figure.FigureLayoutInfo

interface FigureBuildInfo {
    val isComposite: Boolean
    val bounds: DoubleRectangle
    val computationMessages: List<String>
    val containsLiveMap: Boolean
    val layoutInfo: FigureLayoutInfo

    fun createSvgRoot(): FigureSvgRoot

    fun injectLiveMapProvider(f: (tiles: List<List<GeomLayer>>, spec: Map<String, Any>) -> Any)

    fun withBounds(bounds: DoubleRectangle): FigureBuildInfo

    fun layoutedByOuterSize(): FigureBuildInfo

    fun layoutedByGeomBounds(geomBounds: DoubleRectangle): FigureBuildInfo

    fun withPreferredSize(size: DoubleVector): FigureBuildInfo
}