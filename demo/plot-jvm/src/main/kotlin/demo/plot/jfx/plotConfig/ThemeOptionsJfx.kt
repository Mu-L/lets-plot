/*
 * Copyright (c) 2023. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.jfx.plotConfig

import demo.common.utils.jfx.PlotSpecsDemoWindowJfx
import demo.plot.common.model.plotConfig.ThemeOptions

fun main() {
    with(ThemeOptions()) {
        PlotSpecsDemoWindowJfx(
            "Theme options (JFX)",
            plotSpecList()
        ).open()
    }
}
