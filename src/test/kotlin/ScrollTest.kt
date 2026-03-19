package org.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ScrollTest {
    @Test
    fun `insertLineAtBottom pushes top row to scrollback`() {
        val buf = TerminalBuffer(10, 3, 100)
        buf.setCursor(0, 0); buf.writeText("Row0")
        buf.setCursor(1, 0); buf.writeText("Row1")
        buf.setCursor(2, 0); buf.writeText("Row2")

        buf.insertLineAtBottom()

        assertThat(buf.scrollbackSize).isEqualTo(1)
        assertThat(buf.getScrollbackLine(0).trim()).isEqualTo("Row0")

        assertThat(buf.getScreenLine(0).trim()).isEqualTo("Row1")
        assertThat(buf.getScreenLine(1).trim()).isEqualTo("Row2")
        assertThat(buf.getScreenLine(2).trim()).isEmpty()
    }

    @Test
    fun `new bottom row after insertLineAtBottom is blank`() {
        val buf = TerminalBuffer(5, 3, 100)
        buf.insertLineAtBottom()
        assertThat(buf.getScreenLine(2).trim()).isEmpty()
    }

    @Test
    fun `two insertLineAtBottom calls accumulate two scrollback lines`() {
        val buf = TerminalBuffer(5, 2, 100)
        buf.setCursor(0, 0); buf.writeText("A")
        buf.setCursor(1, 0); buf.writeText("B")

        buf.insertLineAtBottom()
        buf.insertLineAtBottom()

        assertThat(buf.scrollbackSize).isEqualTo(2)
        assertThat(buf.getScrollbackLine(0).trim()).isEqualTo("A")
        assertThat(buf.getScrollbackLine(1).trim()).isEqualTo("B")
    }

    @Test
    fun `scrollback cap evicts oldest line when limit reached`() {
        val buf = TerminalBuffer(5, 2, 2)
        buf.setCursor(0, 0); buf.writeText("A"); buf.insertLineAtBottom()
        buf.setCursor(0, 0); buf.writeText("B"); buf.insertLineAtBottom()
        buf.setCursor(0, 0); buf.writeText("C"); buf.insertLineAtBottom()

        assertThat(buf.scrollbackSize).isEqualTo(2)
        assertThat(buf.getScrollbackLine(0).trim()).isEqualTo("B")
        assertThat(buf.getScrollbackLine(1).trim()).isEqualTo("C")
    }

    @Test
    fun `zero maxScrollback discards every evicted row`() {
        val buf = TerminalBuffer(5, 2, 0)
        buf.setCursor(0, 0); buf.writeText("X")
        buf.insertLineAtBottom()
        assertThat(buf.scrollbackSize).isZero()
    }

    @Test
    fun `insertLineAtBottom moves cursor up by one`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.setCursor(2, 3)
        buf.insertLineAtBottom()
        assertThat(buf.cursor.row).isEqualTo(1)
    }

    @Test
    fun `insertLineAtBottom with cursor on row 0 keeps cursor at 0`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.setCursor(0, 2)
        buf.insertLineAtBottom()
        assertThat(buf.cursor.row).isZero()
    }

    @Test
    fun `clearScreen blanks every screen cell`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.setCursor(1, 0); buf.writeText("Hello")
        buf.clearScreen()

        for (r in 0 until 3) assertThat(buf.getScreenLine(r).trim()).isEmpty()
    }

    @Test
    fun `clearScreen resets cursor to origin`() {
        val buf = TerminalBuffer(5, 3, 10)
        buf.setCursor(2, 4)
        buf.clearScreen()
        assertThat(buf.cursor).isEqualTo(CursorPosition(0, 0))
    }

    @Test
    fun `clearScreen does not affect scrollback`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.setCursor(0, 0); buf.writeText("X"); buf.insertLineAtBottom()
        assertThat(buf.scrollbackSize).isEqualTo(1)

        buf.clearScreen()
        assertThat(buf.scrollbackSize).isEqualTo(1)
    }

    @Test
    fun `clearAll erases scrollback too`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.setCursor(0, 0); buf.writeText("X"); buf.insertLineAtBottom()
        assertThat(buf.scrollbackSize).isEqualTo(1)
        assertThat(buf.getScrollbackLine(0).trim()).isEqualTo("X")

        buf.clearAll()
        assertThat(buf.scrollbackSize).isZero()
        assertThat(buf.getScreenLine(0).trim()).isEmpty()
    }

    @Test
    fun `getScrollbackCharAt returns the evicted code point`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.setCursor(0, 0); buf.writeText("AB"); buf.insertLineAtBottom()

        assertThat(buf.getScrollbackCharAt(0, 0)).isEqualTo('A'.code)
        assertThat(buf.getScrollbackCharAt(0, 1)).isEqualTo('B'.code)
    }

    @Test
    fun `getScrollbackAttributesAt returns the colour used during write`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.setForeground(Color.YELLOW)
        buf.setCursor(0, 0); buf.writeText("Z"); buf.insertLineAtBottom()

        assertThat(buf.getScrollbackAttributesAt(0, 0).foreground).isEqualTo(Color.YELLOW)
    }

    @Test
    fun `getAllContent places scrollback lines before screen lines`() {
        val buf = TerminalBuffer(5, 2, 10)
        buf.setCursor(0, 0); buf.writeText("ROW0"); buf.insertLineAtBottom()
        buf.setCursor(0, 0); buf.writeText("ROW1")

        val all   = buf.getAllContent()
        val lines = all.split("\n")
        assertThat(lines[0].trim()).isEqualTo("ROW0")
        assertThat(lines[1].trim()).isEqualTo("ROW1")
    }

    @Test
    fun `circular buffer handles many scrolls correctly`() {
        val buf = TerminalBuffer(5, 3, 50)
        for (i in 0 until 10) {
            buf.setCursor(0, 0); buf.writeText("$i"); buf.insertLineAtBottom()
        }

        assertThat(buf.scrollbackSize).isEqualTo(10)
        assertThat(buf.getScrollbackCharAt(0, 0)).isEqualTo('0'.code)
        assertThat(buf.getScrollbackCharAt(9, 0)).isEqualTo('9'.code)
    }
}
