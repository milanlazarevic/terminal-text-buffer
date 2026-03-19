package org.example

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CursorTest {

    private lateinit var buf: TerminalBuffer

    @BeforeEach
    fun setUp() {
        buf = TerminalBuffer(80, 24, 1000)
    }

    @Test
    fun `cursor starts at origin`() {
        assertThat(buf.cursor).isEqualTo(CursorPosition(0, 0))
    }

    @Test
    fun `setCursor moves to a valid position`() {
        buf.setCursor(5, 10)
        assertThat(buf.cursor).isEqualTo(CursorPosition(5, 10))
    }

    @Test
    fun `setCursor clamps row above bounds`() {
        buf.setCursor(100, 0)
        assertThat(buf.cursor.row).isEqualTo(23)
    }

    @Test
    fun `setCursor clamps col above bounds`() {
        buf.setCursor(0, 200)
        assertThat(buf.cursor.col).isEqualTo(79)
    }

    @Test
    fun `setCursor clamps negative row`() {
        buf.setCursor(-5, 0)
        assertThat(buf.cursor.row).isZero()
    }

    @Test
    fun `setCursor clamps negative col`() {
        buf.setCursor(0, -5)
        assertThat(buf.cursor.col).isZero()
    }

    @Test
    fun `moveCursorDown advances row`() {
        buf.setCursor(0, 0)
        buf.moveCursorDown(5)
        assertThat(buf.cursor.row).isEqualTo(5)
    }

    @Test
    fun `moveCursorDown clamps at last row`() {
        buf.setCursor(20, 0)
        buf.moveCursorDown(100)
        assertThat(buf.cursor.row).isEqualTo(23)
    }

    @Test
    fun `moveCursorUp decrements row`() {
        buf.setCursor(10, 0)
        buf.moveCursorUp(3)
        assertThat(buf.cursor.row).isEqualTo(7)
    }

    @Test
    fun `moveCursorUp clamps at row 0`() {
        buf.setCursor(2, 0)
        buf.moveCursorUp(100)
        assertThat(buf.cursor.row).isZero()
    }

    @Test
    fun `moveCursorRight advances col`() {
        buf.setCursor(0, 0)
        buf.moveCursorRight(15)
        assertThat(buf.cursor.col).isEqualTo(15)
    }

    @Test
    fun `moveCursorRight clamps at last col`() {
        buf.setCursor(0, 70)
        buf.moveCursorRight(100)
        assertThat(buf.cursor.col).isEqualTo(79)
    }

    @Test
    fun `moveCursorLeft decrements col`() {
        buf.setCursor(0, 20)
        buf.moveCursorLeft(5)
        assertThat(buf.cursor.col).isEqualTo(15)
    }

    @Test
    fun `moveCursorLeft clamps at col 0`() {
        buf.setCursor(0, 3)
        buf.moveCursorLeft(100)
        assertThat(buf.cursor.col).isZero()
    }

    @Test
    fun `moving by zero does not change position`() {
        buf.setCursor(5, 5)
        buf.moveCursorUp(0)
        buf.moveCursorDown(0)
        buf.moveCursorLeft(0)
        buf.moveCursorRight(0)
        assertThat(buf.cursor).isEqualTo(CursorPosition(5, 5))
    }

    @Test
    fun `CursorPosition rejects negative row`() {
        assertThatThrownBy { CursorPosition(-1, 0) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `CursorPosition rejects negative col`() {
        assertThatThrownBy { CursorPosition(0, -1) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}