/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.stat

internal fun emitRemovedNonFiniteValuesMessage(
    droppedCount: Int,
    totalCount: Int,
    messageConsumer: (String) -> Unit
) {
    if (droppedCount <= 0) {
        return
    }

    val points = if (droppedCount == 1) "data point" else "data points"
    messageConsumer("Removed $droppedCount $points out of $totalCount containing non-finite values while computing stat.")
}

internal fun emitRemovedBySamplingMessage(
    droppedCount: Int,
    totalCount: Int,
    samplingExpression: String,
    messageConsumer: (String) -> Unit
) {
    if (droppedCount <= 0) {
        return
    }

    val points = if (droppedCount == 1) "data point" else "data points"
    messageConsumer("Removed $droppedCount $points out of $totalCount by $samplingExpression while computing stat.")
}
