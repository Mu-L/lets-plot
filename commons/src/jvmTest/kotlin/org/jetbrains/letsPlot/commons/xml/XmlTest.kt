/*
 * Copyright (c) 2025. JetBrains s.r.o.
 * Use of this source code is governed by the MIT license that can be found in the LICENSE file.
 */

package org.jetbrains.letsPlot.commons.xml

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.letsPlot.commons.xml.Xml.XmlNode
import org.junit.Test
import kotlin.test.assertEquals

class XmlTest {
    @Test
    fun simple() {
        val xml = """<p>Hi</p>""".trimIndent()

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                children = listOf(XmlNode.Text("Hi"))
            ),
            actual = parsed
        )
    }

    @Test
    fun `hello, World!`() {
        val xml = """<p>Hello, world!</p>"""

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                children = listOf(XmlNode.Text("Hello, world!"))
            ),
            actual = parsed
        )
    }

    @Test
    fun contentWithTokens() {
        val xml = """<p>foo / = > bar</p>"""

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                children = listOf(XmlNode.Text("foo / = > bar"))
            ),
            actual = parsed
        )
    }

    @Test
    fun selfClosing() {
        val xml = """<br/>""".trimIndent()

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element("br"),
            actual = parsed
        )
    }

    @Test
    fun selfClosingWithSpaces() {
        val xml = """<br  />""".trimIndent()

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element("br"),
            actual = parsed
        )
    }

    @Test
    fun selfClosingWithAttributeAndSpace() {
        val xml = """<img src="foobar.png"   />""".trimIndent()

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "img",
                attributes = mapOf("src" to "foobar.png")
            ),
            actual = parsed
        )
    }

    @Test
    fun anchor() {
        val xml = """<a href="https://example.com">Example</a>""".trimIndent()

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "a",
                attributes = mapOf("href" to "https://example.com"),
                children = listOf(XmlNode.Text("Example"))
            ),
            actual = parsed
        )
    }

    @Test
    fun theMoreThanBracketInAttributeValue() {
        val xml = """<formula expression="a > 0" />""".trimIndent()

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "formula",
                attributes = mapOf("expression" to "a > 0")
            ),
            actual = parsed
        )
    }

    @Test
    fun withSpanStyle() {
        val xml = """<p>Hello <span style="color:red">world</span></p>"""
        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                attributes = emptyMap(),
                children = listOf(
                    XmlNode.Text("Hello "),
                    XmlNode.Element(
                        name = "span",
                        attributes = mapOf("style" to "color:red"),
                        children = listOf(XmlNode.Text("world"))
                    )
                )
            ),
            actual = parsed
        )
    }

    @Test
    fun attrValueWithoutQuotes() {
        val xml = """<p style=color:red>Hello</p>"""

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                attributes = mapOf("style" to "color:red"),
                children = listOf(XmlNode.Text("Hello"))
            ),
            actual = parsed
        )
    }

    @Test
    fun attrValueWithSingleQuotes() {
        val xml = """<p style='color:red'>Hello</p>"""

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                attributes = mapOf("style" to "color:red"),
                children = listOf(XmlNode.Text("Hello"))
            ),
            actual = parsed
        )
    }

    @Test
    fun twoAttributes() {
        val xml = """<p style="color:red" class="foo">Hello</p>"""

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                attributes = mapOf("style" to "color:red", "class" to "foo"),
                children = listOf(XmlNode.Text("Hello"))
            ),
            actual = parsed
        )
    }

    @Test
    fun extraSpacesEverywhere() {
        val xml = """<p    style  = "color:red"  >  Hello  </p >""".trimIndent()

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                attributes = mapOf("style" to "color:red"),
                children = listOf(XmlNode.Text("  Hello  "))
            ),
            actual = parsed
        )
    }

    @Test
    fun quotesInText() {
        val xml = """<p>He said: 'Hello' and then "Goodbye"</p>"""

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                children = listOf(XmlNode.Text("""He said: 'Hello' and then "Goodbye""""))
            ),
            actual = parsed
        )
    }

    @Test
    fun nested() {
        val xml = """<p>press <button>send<img src="send.png"/></button> button</p>"""
        val (rootNode, nodeLocations) = Xml.parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                children = listOf(
                    XmlNode.Text("press "),
                    XmlNode.Element(
                        name = "button",
                        children = listOf(
                            XmlNode.Text("send"),
                            XmlNode.Element(name = "img", attributes = mapOf("src" to "send.png"),)
                        )
                    ),
                    XmlNode.Text(" button")
                )
            ),
            actual = rootNode
        )

        require(rootNode is XmlNode.Element)

        assertEquals(0..61, nodeLocations[rootNode])
        assertEquals(3..8, nodeLocations[rootNode.children[0]]) // "press "
        assertEquals(9..50, nodeLocations[rootNode.children[1]]) // <button>...</button>
        assertEquals(51..57, nodeLocations[rootNode.children[2]]) // " button"

        val buttonNode = rootNode.children[1] as XmlNode.Element
        assertEquals(17..20, nodeLocations[buttonNode.children[0]]) // "send"
        assertEquals(21..41, nodeLocations[buttonNode.children[1]]) // <img .../>
    }


    @Test
    fun newLine() {
        val xml = "<p>foo\nbar</p>"

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                children = listOf(
                    XmlNode.Text("foo\nbar")
                )
            ),
            actual = parsed
        )
    }

    @Test
    fun malformed() {
        val xml = """<p> foo <b style="color:red"/> bar <p =foobar</p>"""

        val parsed = parse(xml)
        assertEquals(
            expected = XmlNode.Element(
                name = "p",
                children = listOf(
                    XmlNode.Text(" foo "),
                    XmlNode.Element(name = "b", attributes = mapOf("style" to "color:red")),
                    XmlNode.Text(" bar "),
                    XmlNode.Text("<p ="),
                    XmlNode.Text("foobar"),
                )
            ),
            actual = parsed
        )
    }

    @Test
    fun `malformed - not closed`() {
        val xml = """<br"""

        val res = Xml.parse(xml)
        val parsed = res.root
        assertEquals(
            expected = XmlNode.Text("<br"),
            actual = parsed
        )

        assertThat(res.errorPos).isEqualTo(0)
    }

    @Test
    fun `malformed - text after closing root tag`() {
        val xml = """<p>Hello</p> world"""

        val res = Xml.parse(xml)
        val parsed = res.root
        assertEquals(
            expected = XmlNode.Element(name = "p", children = listOf(XmlNode.Text("Hello"))),
            actual = parsed
        )

        // No error report just because it was easy to implement. Could be changed if needed.
        // Useful for relaxed parsing - like plaintext with some tags inside.
        assertThat(res.errorPos).isNull()
    }

    @Test
    fun `malformed - special symbols`() {
        val xml = """<p>< & ' \" \\ / > ®</p>"""
        val res = Xml.parse(xml)
        assertEquals(
            expected = XmlNode.Text("""<p>< & ' \" \\ / > ®</p>"""),
            actual = res.root
        )

        assertThat(res.errorPos).isEqualTo(0)
    }

    internal fun parse(xml: String): XmlNode {
        return Xml.parse(xml).root
    }
}