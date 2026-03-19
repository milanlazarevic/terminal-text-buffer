package org.example

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContentAccessTest {

    private lateinit var buf: TerminalBuffer

    @BeforeEach
    fun setUp() {
        buf = TerminalBuffer(10, 3, 100)
    }

    @Test
    fun `fresh buffer screen content is all spaces`() {
        val content = buf.getScreenContent()
        assertThat(content).isEqualTo(
            "          \n" +
                    "          \n" +
                    "          "
        )
    }

    @Test
    fun `getScreenLine returns a full-width string`() {
        buf.setCursor(0, 0); buf.writeText("Hi")
        assertThat(buf.getScreenLine(0)).hasSize(10)
        assertThat(buf.getScreenLine(0).substring(0, 2)).isEqualTo("Hi")
    }

    @Test
    fun `getScreenContent joins rows with newlines`() {
        buf.setCursor(0, 0); buf.writeText("AAA")
        buf.setCursor(1, 0); buf.writeText("BBB")
        buf.setCursor(2, 0); buf.writeText("CCC")

        val lines = buf.getScreenContent().split("\n")
        assertThat(lines).hasSize(3)
        assertThat(lines[0]).startsWith("AAA")
        assertThat(lines[1]).startsWith("BBB")
        assertThat(lines[2]).startsWith("CCC")
    }

    @Test
    fun `getCharAt returns the correct code point`() {
        buf.setCursor(1, 3); buf.writeText("XYZ")
        assertThat(buf.getCharAt(1, 3)).isEqualTo('X'.code)
        assertThat(buf.getCharAt(1, 4)).isEqualTo('Y'.code)
        assertThat(buf.getCharAt(1, 5)).isEqualTo('Z'.code)
    }

    @Test
    fun `getAttributesAt returns attributes used at write time`() {
        val bold = TextAttributes.DEFAULT
            .withStyle(TextAttributes.STYLE_BOLD)
            .copy(foreground = Color.MAGENTA)

        buf.currentAttributes = bold
        buf.setCursor(0, 2); buf.writeText("B")

        val attrs = buf.getAttributesAt(0, 2)
        assertThat(attrs.isBold).isTrue()
        assertThat(attrs.foreground).isEqualTo(Color.MAGENTA)
    }

    @Test
    fun `getScrollbackLine returns the evicted line content`() {
        buf.setCursor(0, 0); buf.writeText("SCROLLED"); buf.insertLineAtBottom()
        assertThat(buf.getScrollbackLine(0)).startsWith("SCROLLED")
    }

    @Test
    fun `getAllContent shows scrollback before screen`() {
        buf.setCursor(0, 0); buf.writeText("SB0"); buf.insertLineAtBottom()
        buf.setCursor(0, 0); buf.writeText("SCR")

        val all    = buf.getAllContent()
        val sbIdx  = all.indexOf("SB0")
        val scrIdx = all.indexOf("SCR")
        assertThat(sbIdx).isLessThan(scrIdx)
    }

    @Test
    fun `getCharAt on an empty cell returns EMPTY`() {
        assertThat(buf.getCharAt(0, 0)).isEqualTo(Cell.EMPTY)
    }

    @Test
    fun `getCharAt with out-of-bounds row throws`() {
        assertThatThrownBy { buf.getCharAt(-1, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { buf.getCharAt(3, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `getScrollbackCharAt on empty scrollback throws`() {
        assertThatThrownBy { buf.getScrollbackCharAt(0, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}