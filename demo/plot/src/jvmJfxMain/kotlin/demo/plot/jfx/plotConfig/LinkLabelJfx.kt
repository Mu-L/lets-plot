/*
 * Copyright (c) 2024. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.plot.jfx.plotConfig

import demo.common.jfx.demoUtils.PlotSpecsDemoWindowJfx
import demo.plot.common.model.plotConfig.LinkLabel

fun main() {
    with(LinkLabel()) {
        PlotSpecsDemoWindowJfx(
            "Link label demo",
            plotSpecList()
        ).open()
    }
}

/*
class HyperlinkBoundsExample : Application() {
    val delayed = false
    override fun start(primaryStage: Stage) {
        // Create a Pane for manual layout
        val pane = Pane()

        // Create a Hyperlink
        val hyperlink = Hyperlink("Click me")

        // Set its position (manual layout)
        hyperlink.layoutX = 100.0
        hyperlink.layoutY = 100.0

        // Add the hyperlink to the Pane
        pane.children.add(hyperlink)

        // Force a layout pass if needed
        if (delayed)
            pane.layout() // Forces the layout pass

        // Use Platform.runLater to ensure layout has finished
        if (delayed) {
            Platform.runLater {
                val bounds = hyperlink.boundsInLocal
                println("Bounds after layout: $bounds")
            }
        } else {
            val bounds = hyperlink.boundsInLocal
            println("Bounds after layout: $bounds")
        }

        // Create a Scene and add it to the Stage
        val scene = Scene(pane, 400.0, 300.0)
        primaryStage.scene = scene
        primaryStage.title = "Hyperlink Bounds Example"
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun run(args: Array<String>) {
            launch(HyperlinkBoundsExample::class.java, *args)
        }
    }

}

fun main() {
    HyperlinkBoundsExample.run(emptyArray())
}
*/