/*
 * Copyright (c) 2023. JetBrains s.r.o. 
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.jfx.component

import demo.common.utils.jfx.SvgViewerDemoWindowJfx
import demo.plot.shared.model.component.PlotSpecLabelSizesDemo

fun main() {
    with(PlotSpecLabelSizesDemo()) {
        SvgViewerDemoWindowJfx(
            "Label sizes",
            createSvgRoots(createModels()),
            maxCol = 3
        ).open()
    }
}