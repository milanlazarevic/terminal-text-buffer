package org.example

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WriteTest {

    private lateinit var buf: TerminalBuffer

    @BeforeEach
    fun setUp() {
        buf = TerminalBuffer(10, 5, 100)
    }

    @Test
    fun `writeText stores characters starting at cursor`() {
        buf.writeText("Hello")
        assertThat(buf.getScreenLine(0)).startsWith("Hello")
    }

    @Test
    fun `writeText advances cursor by character count`() {
        buf.setCursor(0, 0)
        buf.writeText("Hi")
        assertThat(buf.cursor).isEqualTo(CursorPosition(0, 2))
    }

    @Test
    fun `writeText overwrites existing content`() {
        buf.writeText("AAAAAAAAAA")
        buf.setCursor(0, 2)
        buf.writeText("BB")

        val line = buf.getScreenLine(0)
        assertThat(line[0]).isEqualTo('A')
        assertThat(line[1]).isEqualTo('A')
        assertThat(line[2]).isEqualTo('B')
        assertThat(line[3]).isEqualTo('B')
        assertThat(line[4]).isEqualTo('A')
    }
    @Test
    fun `writeText does not wrap past right edge`() {
        buf.setCursor(0, 8)
        buf.writeText("XYZ")
        assertThat(buf.getCharAt(0, 8)).isEqualTo('X'.code)
        assertThat(buf.getCharAt(0, 9)).isEqualTo('Y'.code)
        assertThat(buf.getScreenLine(1).trim()).isEmpty()
    }

    @Test
    fun `writeText applies current attributes to each cell`() {
        buf.setForeground(Color.RED)
        buf.writeText("A")
        assertThat(buf.getAttributesAt(0, 0).foreground).isEqualTo(Color.RED)
    }

    @Test
    fun `writeText on a middle row and column`() {
        buf.setCursor(2, 3)
        buf.writeText("XY")
        assertThat(buf.getCharAt(2, 3)).isEqualTo('X'.code)
        assertThat(buf.getCharAt(2, 4)).isEqualTo('Y'.code)
    }

    @Test
    fun `writeText with empty string does not move cursor`() {
        buf.setCursor(1, 1)
        buf.writeText("")
        assertThat(buf.cursor).isEqualTo(CursorPosition(1, 1))
    }

    @Test
    fun `writeText with null does not throw`() {
        assertThatCode { buf.writeText(null) }.doesNotThrowAnyException()
    }


    @Test
    fun `insertText shifts existing content right`() {
        buf.writeText("BCDE")
        buf.setCursor(0, 0)
        buf.insertText("A")

        val line = buf.getScreenLine(0)
        assertThat(line[0]).isEqualTo('A')
        assertThat(line[1]).isEqualTo('B')
        assertThat(line[2]).isEqualTo('C')
        assertThat(line[3]).isEqualTo('D')
        assertThat(line[4]).isEqualTo('E')
    }

    @Test
    fun `insertText drops last cell when row is full`() {
        buf.writeText("ABCDEFGHIJ")
        buf.setCursor(0, 0)
        buf.insertText("X")

        assertThat(buf.getCharAt(0, 0)).isEqualTo('X'.code)
        assertThat(buf.getCharAt(0, 1)).isEqualTo('A'.code)
        assertThat(buf.getCharAt(0, 9)).isEqualTo('I'.code)
    }

    @Test
    fun `insertText advances cursor`() {
        buf.setCursor(0, 2)
        buf.insertText("AB")
        assertThat(buf.cursor.col).isEqualTo(4)
    }

    @Test
    fun `insertText wraps to next row when cursor reaches right edge`() {
        buf.setCursor(0, 9)
        buf.insertText("XY")

        assertThat(buf.getCharAt(0, 9)).isEqualTo('X'.code)
        assertThat(buf.getCharAt(1, 0)).isEqualTo('Y'.code)
    }

    @Test
    fun `insertText applies current attributes`() {
        buf.setBackground(Color.GREEN)
        buf.setCursor(0, 0)
        buf.insertText("Z")
        assertThat(buf.getAttributesAt(0, 0).background).isEqualTo(Color.GREEN)
    }


    @Test
    fun `fillLine fills the entire row with the given character`() {
        buf.fillLine(2, '-'.code)
        assertThat(buf.getScreenLine(2)).isEqualTo("----------")
    }

    @Test
    fun `fillLine with EMPTY clears the row`() {
        buf.writeText("Hello")
        buf.fillLine(0, Cell.EMPTY)
        assertThat(buf.getScreenLine(0).trim()).isEmpty()
    }

    @Test
    fun `fillLine does not affect adjacent rows`() {
        buf.setCursor(0, 0)
        buf.writeText("Row0")
        buf.fillLine(1, '*'.code)
        assertThat(buf.getScreenLine(0)).startsWith("Row0")
    }

    @Test
    fun `fillLine applies current attributes to every cell`() {
        buf.setForeground(Color.CYAN)
        buf.fillLine(0, '='.code)
        for (c in 0 until 10) {
            assertThat(buf.getAttributesAt(0, c).foreground).isEqualTo(Color.CYAN)
        }
    }
}