/*
 * Copyright (c) 2026. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

@file:OptIn(ExperimentalWasmJsInterop::class)

package org.jetbrains.letsPlot.platf.w3c.dom

import kotlinx.browser.document
import org.jetbrains.letsPlot.commons.registration.Registration
import org.w3c.dom.MutationObserver
import org.w3c.dom.MutationObserverInit
import org.w3c.dom.Node
import kotlin.js.ExperimentalWasmJsInterop

object DomDetachObserver {
    fun onDetach(node: Node, onDetached: () -> Unit): Registration {
        return onDetach(
            isConnected = node::isConnected,
            onMutationObserved = { checkDetached ->
                observeDomMutations(checkDetached)
            },
            onDetached = onDetached
        )
    }

    fun onDetach(
        isConnected: () -> Boolean,
        onMutationObserved: ((() -> Unit) -> Registration),
        onDetached: () -> Unit
    ): Registration {
        var active = true
        lateinit var mutationRegistration: Registration

        mutationRegistration = onMutationObserved {
            if (active && !isConnected()) {
                active = false
                mutationRegistration.dispose()
                onDetached()
            }
        }

        return Registration.onRemove {
            if (active) {
                active = false
                mutationRegistration.dispose()
            }
        }
    }

    private fun observeDomMutations(onMutation: () -> Unit): Registration {
        val observer = MutationObserver { _, _ ->
            onMutation()
        }
        observer.observe(
            document,
            MutationObserverInit(
                childList = true,
                subtree = true
            )
        )
        return Registration.onRemove {
            observer.disconnect()
        }
    }
}