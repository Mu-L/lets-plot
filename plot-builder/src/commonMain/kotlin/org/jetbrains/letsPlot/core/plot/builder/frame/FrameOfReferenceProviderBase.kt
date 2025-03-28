/*
 * Copyright (c) 2024. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.builder.frame

import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.interval.DoubleSpan
import org.jetbrains.letsPlot.commons.values.Color
import org.jetbrains.letsPlot.core.plot.base.PlotContext
import org.jetbrains.letsPlot.core.plot.base.Scale
import org.jetbrains.letsPlot.core.plot.base.theme.AxisTheme
import org.jetbrains.letsPlot.core.plot.base.theme.Theme
import org.jetbrains.letsPlot.core.plot.builder.FrameOfReference
import org.jetbrains.letsPlot.core.plot.builder.FrameOfReferenceProvider
import org.jetbrains.letsPlot.core.plot.builder.MarginSide
import org.jetbrains.letsPlot.core.plot.builder.coord.CoordProvider
import org.jetbrains.letsPlot.core.plot.builder.coord.MarginalLayerCoordProvider
import org.jetbrains.letsPlot.core.plot.builder.guide.Orientation
import org.jetbrains.letsPlot.core.plot.builder.layout.*
import org.jetbrains.letsPlot.core.plot.builder.layout.axis.AxisBreaksProviderFactory
import org.jetbrains.letsPlot.core.plot.builder.scale.AxisPosition
import kotlin.math.max

internal abstract class FrameOfReferenceProviderBase(
    protected val plotContext: PlotContext,
    private val hScaleProto: Scale,
    private val vScaleProto: Scale,
    override val flipAxis: Boolean,
    private val hAxisPosition: AxisPosition,
    private val vAxisPosition: AxisPosition,
    protected val theme: Theme,
    protected val marginsLayout: GeomMarginsLayout,
    private val domainByMargin: Map<MarginSide, DoubleSpan>,
    private val isPolar: Boolean,
) : FrameOfReferenceProvider {

    private val hAxisSpec: AxisSpec
    private val vAxisSpec: AxisSpec

    init {
        hAxisSpec = AxisSpec(
            AxisBreaksProviderFactory.forScale(hScaleProto),
            hScaleProto.name,
            theme.horizontalAxis(flipAxis)
        )

        vAxisSpec = AxisSpec(
            AxisBreaksProviderFactory.forScale(vScaleProto),
            vScaleProto.name,
            theme.verticalAxis(flipAxis)
        )
    }

    override val hAxisLabel: String? = if (hAxisSpec.theme.showTitle()) hAxisSpec.label else null
    override val vAxisLabel: String? = if (vAxisSpec.theme.showTitle()) vAxisSpec.label else null

    internal abstract fun createTileLayoutProvider(axisLayoutQuad: AxisLayoutQuad): TileLayoutProvider

    override fun createTileLayoutProvider(): TileLayoutProvider {
        fun toAxisLayout(
            orientation: Orientation,
            position: AxisPosition,
            spec: AxisSpec
        ): AxisLayout? {
            @Suppress("NAME_SHADOWING")
            val orientation: Orientation? = when (orientation) {
                Orientation.LEFT -> if (position.isLeft) orientation else null
                Orientation.RIGHT -> if (position.isRight) orientation else null
                Orientation.TOP -> if (position.isTop) orientation else null
                Orientation.BOTTOM -> if (position.isBottom) orientation else null
            }

            return orientation?.run {
                AxisLayout(
                    spec.breaksProviderFactory,
                    orientation,
                    spec.theme,
                    isPolar
                )
            }
        }

        val axisLayoutQuad = AxisLayoutQuad(
            left = toAxisLayout(Orientation.LEFT, vAxisPosition, vAxisSpec),
            right = toAxisLayout(Orientation.RIGHT, vAxisPosition, vAxisSpec),
            top = toAxisLayout(Orientation.TOP, hAxisPosition, hAxisSpec),
            bottom = toAxisLayout(Orientation.BOTTOM, hAxisPosition, hAxisSpec),
        )

        return createTileLayoutProvider(axisLayoutQuad)
    }

    override fun createMarginalFrames(
        tileLayoutInfo: TileLayoutInfo,
        coordProvider: CoordProvider,
        plotBackground: Color,
        debugDrawing: Boolean
    ): Map<MarginSide, FrameOfReference> {
        if (domainByMargin.isEmpty()) {
            return emptyMap()
        }

        check(!coordProvider.flipped) {
            "`flipped` corrdinate system is not supported on plots with marginal layers."
        }

        val inner = tileLayoutInfo.geomInnerBounds
        val outer = tileLayoutInfo.geomOuterBounds

        val origins = mapOf(
            MarginSide.LEFT to DoubleVector(outer.left, inner.top),
            MarginSide.TOP to DoubleVector(inner.left, outer.top),
            MarginSide.RIGHT to DoubleVector(inner.right, inner.top),
            MarginSide.BOTTOM to DoubleVector(inner.left, inner.bottom),
        )

        val sizes = mapOf(
            MarginSide.LEFT to DoubleVector(max(0.0, inner.left - outer.left), inner.height),
            MarginSide.TOP to DoubleVector(inner.width, max(0.0, inner.top - outer.top)),
            MarginSide.RIGHT to DoubleVector(max(0.0, outer.right - inner.right), inner.height),
            MarginSide.BOTTOM to DoubleVector(inner.width, max(0.0, outer.bottom - inner.bottom)),
        )

        val boundsByMargin = origins.mapValues { (margin, origin) ->
            DoubleRectangle(origin, sizes.getValue(margin))
        }

        // Below use any of horizontal/vertical axis info in the "quad".
        // ToDo: with non-rectangular coordinates this might not work as axis length (for example) might be different
        // for top and botto, axis.
        val hAxisLayoutInfo = tileLayoutInfo.axisInfos.bottom
            ?: tileLayoutInfo.axisInfos.top
            ?: throw IllegalStateException("No top/bottom axis info.")

        val vAxisLayoutInfo = tileLayoutInfo.axisInfos.left
            ?: tileLayoutInfo.axisInfos.right
            ?: throw IllegalStateException("No left/right axis info.")

        return domainByMargin.mapValues { (side, domain) ->
            val hDomain = when (side) {
                MarginSide.LEFT, MarginSide.RIGHT -> domain
                MarginSide.TOP, MarginSide.BOTTOM -> hAxisLayoutInfo.axisDomain
            }
            val vDomain = when (side) {
                MarginSide.LEFT, MarginSide.RIGHT -> vAxisLayoutInfo.axisDomain
                MarginSide.TOP, MarginSide.BOTTOM -> domain
            }

            val marginCoordProvider = MarginalLayerCoordProvider()
            val clientSize = sizes.getValue(side)
            val adjustedDomain = DoubleRectangle(hDomain, vDomain)
            val coord = marginCoordProvider.createCoordinateSystem(
                adjustedDomain = adjustedDomain,
                clientSize = clientSize,
            )
            MarginalFrameOfReference(
                plotContext,
                boundsByMargin.getValue(side),
                adjustedDomain = adjustedDomain,
                coord,
                plotBackground,
                debugDrawing,
            )
        }
    }


    private class AxisSpec(
        val breaksProviderFactory: AxisBreaksProviderFactory,
        val label: String?,
        val theme: AxisTheme
    )
}