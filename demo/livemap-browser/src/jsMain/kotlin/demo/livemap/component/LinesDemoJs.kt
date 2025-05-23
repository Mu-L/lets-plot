/*
 * Copyright (c) 2019. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package demo.livemap.component

import demo.livemap.common.component.LinesDemoModel

@JsExport
fun linesDemo() {
    DemoBaseJs(::LinesDemoModel).show()
}
