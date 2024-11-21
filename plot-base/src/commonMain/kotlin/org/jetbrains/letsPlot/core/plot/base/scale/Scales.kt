/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.core.plot.base.scale

import org.jetbrains.letsPlot.core.plot.base.Aes
import org.jetbrains.letsPlot.core.plot.base.DiscreteTransform
import org.jetbrains.letsPlot.core.plot.base.Scale

object Scales {

    fun continuousDomain(
        name: String,
        defaultFormatter: (Any) -> String,
        continuousRange: Boolean
    ): Scale {
        return ContinuousScale(name, defaultFormatter, continuousRange)
    }

    fun discreteDomain(
        name: String,
        defaultFormatter: (Any) -> String,
        discreteTransform: DiscreteTransform,
    ): Scale {
        return DiscreteScale(name, defaultFormatter, discreteTransform)
    }

    /**
     * Functions to be used in demos and tests only.
     */
    object DemoAndTest {
        fun discreteDomain(
            name: String,
            domainValues: List<Any>,
            domainLimits: List<Any> = emptyList(),
        ): Scale {
            return DiscreteScale(
                name,
                { it.toString() }, // TODO
                DiscreteTransform(domainValues, domainLimits),
            )
        }

        fun pureDiscrete(
            name: String,
            domainValues: List<Any>,
        ): Scale {
            val transform = DiscreteTransform(domainValues, emptyList())
            return DiscreteScale(name, { it.toString() }, transform) // TODO
        }

        fun continuousDomain(name: String, aes: Aes<*>): Scale {
            return ContinuousScale(
                name,
                { it.toString() }, // TODO
                aes.isNumeric
            )
        }

        fun continuousDomainNumericRange(name: String): Scale {
            return ContinuousScale(
                name,
                { it.toString() }, // TODO
                true
            )
        }
    }
}
