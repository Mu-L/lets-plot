/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.builder

import org.jetbrains.letsPlot.commons.event.MouseEventPeer
import org.jetbrains.letsPlot.commons.geometry.DoubleRectangle
import org.jetbrains.letsPlot.commons.geometry.DoubleVector
import org.jetbrains.letsPlot.commons.registration.CompositeRegistration
import org.jetbrains.letsPlot.commons.registration.Disposable
import org.jetbrains.letsPlot.commons.registration.Registration
import org.jetbrains.letsPlot.core.interact.EventsManager
import org.jetbrains.letsPlot.core.interact.ToolFeedback
import org.jetbrains.letsPlot.core.interact.ToolInteractor
import org.jetbrains.letsPlot.core.interact.feedback.DrawRectFeedback
import org.jetbrains.letsPlot.core.interact.feedback.PanGeomFeedback
import org.jetbrains.letsPlot.core.interact.feedback.RollbackAllChangesFeedback
import org.jetbrains.letsPlot.core.interact.feedback.WheelZoomFeedback
import org.jetbrains.letsPlot.core.plot.base.PlotContext
import org.jetbrains.letsPlot.core.plot.base.theme.Theme
import org.jetbrains.letsPlot.core.plot.base.tooltip.GeomTargetLocator
import org.jetbrains.letsPlot.core.plot.builder.interact.context.MouseDragSelectionStrategy
import org.jetbrains.letsPlot.core.plot.builder.interact.context.MouseWheelSelectionStrategy
import org.jetbrains.letsPlot.core.plot.builder.interact.context.NoneSelectionStrategy
import org.jetbrains.letsPlot.core.plot.builder.interact.context.PlotTilesInteractionContext
import org.jetbrains.letsPlot.core.plot.builder.tooltip.HorizontalAxisTooltipPosition
import org.jetbrains.letsPlot.core.plot.builder.tooltip.TooltipRenderer
import org.jetbrains.letsPlot.core.plot.builder.tooltip.VerticalAxisTooltipPosition
import org.jetbrains.letsPlot.datamodel.svg.dom.SvgNode
import org.jetbrains.letsPlot.datamodel.svg.style.StyleSheet

internal class PlotInteractor(
    val decorationLayer: SvgNode,
    mouseEventPeer: MouseEventPeer,
    val plotSize: DoubleVector,
    flippedAxis: Boolean,
    theme: Theme,
    styleSheet: StyleSheet,
    plotContext: PlotContext
) : ToolInteractor, Disposable {
    val eventsManager: EventsManager = EventsManager()

    private val reg = CompositeRegistration()
    private val tooltipRenderer: TooltipRenderer

    private val tiles = ArrayList<Pair<DoubleRectangle, PlotTile>>()

    init {
        reg.add(Registration.from(eventsManager))
        eventsManager.setEventSource(mouseEventPeer)

        tooltipRenderer = TooltipRenderer(
            decorationLayer,
            flippedAxis,
            plotSize,
            theme.horizontalAxis(flippedAxis),
            theme.verticalAxis(flippedAxis),
            theme.tooltips(),
            theme.plot().backgroundFill(),
            styleSheet,
            plotContext,
            mouseEventPeer
        )
        reg.add(Registration.from(tooltipRenderer))
    }

    fun onTileAdded(
        plotTile: PlotTile,
        geomBounds: DoubleRectangle,
        targetLocators: List<GeomTargetLocator>,
        layerYOrientations: List<Boolean>,
        axisOrigin: DoubleVector,
        hAxisTooltipPosition: HorizontalAxisTooltipPosition,
        vAxisTooltipPosition: VerticalAxisTooltipPosition
    ) {
        tooltipRenderer.addTileInfo(
            geomBounds,
            targetLocators,
            layerYOrientations,
            axisOrigin,
            hAxisTooltipPosition,
            vAxisTooltipPosition
        )
        tiles.add(geomBounds to plotTile)
    }

    override fun startToolFeedback(toolFeedback: ToolFeedback): Registration {
        var dataSelectionStrategy = when (toolFeedback) {
            is PanGeomFeedback,
            is DrawRectFeedback -> MouseDragSelectionStrategy()

            is WheelZoomFeedback -> MouseWheelSelectionStrategy()
            is RollbackAllChangesFeedback -> NoneSelectionStrategy()
            else -> throw IllegalArgumentException("Unexpected feedback object: ${toolFeedback::class.simpleName}")
        }

        val disposable: Disposable = toolFeedback.start(
            PlotTilesInteractionContext(
                decorationLayer,
                eventsManager,
                tiles,
                dataSelectionStrategy
            )
        )
        return Registration.from(disposable)
    }

    override fun reset() {
        tiles.forEach { (_, tile) -> tile.transientState.reset() }
    }

    override fun dispose() {
        reg.dispose()
    }
}
