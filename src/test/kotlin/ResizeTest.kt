package org.example

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ResizeTest {

    @Test
    fun `resizing to same dimensions preserves content and cursor`() {
        val buf = TerminalBuffer(10, 5, 100)
        buf.setCursor(2, 3); buf.writeText("Hello")
        buf.resize(10, 5)

        assertThat(buf.cursor).isEqualTo(CursorPosition(2, 8))
        assertThat(buf.getScreenLine(2).trim()).startsWith("Hello")
    }

    @Test
    fun `growing width preserves existing content`() {
        val buf = TerminalBuffer(5, 3, 100)
        buf.setCursor(0, 0); buf.writeText("ABCDE")
        buf.resize(10, 3)

        assertThat(buf.width).isEqualTo(10)
        assertThat(buf.getScreenLine(0)).startsWith("ABCDE")
    }

    @Test
    fun `shrinking width truncates content on the right`() {
        val buf = TerminalBuffer(10, 3, 100)
        buf.setCursor(0, 0); buf.writeText("ABCDEFGHIJ")
        buf.resize(5, 3)

        assertThat(buf.width).isEqualTo(5)
        assertThat(buf.getScreenLine(0)).isEqualTo("ABCDE")
    }

    @Test
    fun `growing height pulls lines back from scrollback`() {
        val buf = TerminalBuffer(5, 2, 100)
        buf.setCursor(0, 0); buf.writeText("SB0"); buf.insertLineAtBottom()
        buf.setCursor(0, 0); buf.writeText("SCR")

        assertThat(buf.scrollbackSize).isEqualTo(1)
        buf.resize(5, 3)

        assertThat(buf.height).isEqualTo(3)
        assertThat(buf.scrollbackSize).isZero()
        assertThat(buf.getScreenLine(0)).startsWith("SB0")
        assertThat(buf.getScreenLine(1)).startsWith("SCR")
    }

    @Test
    fun `shrinking height pushes surplus rows into scrollback`() {
        val buf = TerminalBuffer(5, 4, 100)
        buf.setCursor(0, 0); buf.writeText("R0")
        buf.setCursor(1, 0); buf.writeText("R1")
        buf.setCursor(2, 0); buf.writeText("R2")
        buf.setCursor(3, 0); buf.writeText("R3")

        buf.resize(5, 2)

        assertThat(buf.height).isEqualTo(2)
        assertThat(buf.scrollbackSize).isEqualTo(2)
        assertThat(buf.getScrollbackLine(0)).startsWith("R0")
        assertThat(buf.getScrollbackLine(1)).startsWith("R1")
        assertThat(buf.getScreenLine(0)).startsWith("R2")
        assertThat(buf.getScreenLine(1)).startsWith("R3")
    }

    @Test
    fun `resize clamps cursor to new bounds`() {
        val buf = TerminalBuffer(20, 10, 100)
        buf.setCursor(8, 15)
        buf.resize(5, 3)

        assertThat(buf.cursor.row).isEqualTo(2)
        assertThat(buf.cursor.col).isEqualTo(4)
    }

    @Test
    fun `resize with zero width or height throws`() {
        val buf = TerminalBuffer(5, 5, 100)
        assertThatThrownBy { buf.resize(0, 5) }
            .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { buf.resize(5, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `repeated resizes do not corrupt content`() {
        val buf = TerminalBuffer(10, 5, 100)
        buf.setCursor(0, 0); buf.writeText("HELLO")

        buf.resize(20, 5)
        buf.resize(10, 5)
        buf.resize(10, 10)
        buf.resize(5, 5)

        assertThat(buf.getScreenLine(0)).startsWith("HELLO")
    }
}