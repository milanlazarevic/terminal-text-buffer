package org.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CellTest {

    @Test
    fun `new cell is empty with default attributes`() {
        val cell = Cell()
        assertThat(cell.isEmpty).isTrue()
        assertThat(cell.codePoint).isEqualTo(Cell.EMPTY)
        assertThat(cell.attributes).isEqualTo(TextAttributes.DEFAULT)
        assertThat(cell.isWide).isFalse()
        assertThat(cell.isPlaceholder).isFalse()
    }

    @Test
    fun `cell with code point is not empty`() {
        val cell = Cell('A'.code, TextAttributes.DEFAULT)
        assertThat(cell.isEmpty).isFalse()
        assertThat(cell.codePoint).isEqualTo('A'.code)
    }

    @Test
    fun `clear resets all fields to defaults`() {
        val cell = Cell('Z'.code, TextAttributes.DEFAULT.copy(foreground = Color.RED))
        cell.setWide(true)
        cell.clear()

        assertThat(cell.isEmpty).isTrue()
        assertThat(cell.attributes).isEqualTo(TextAttributes.DEFAULT)
        assertThat(cell.isWide).isFalse()
    }

    @Test
    fun `clear with attributes retains supplied attributes`() {
        val cell   = Cell('A'.code, TextAttributes.DEFAULT)
        val bgBlue = TextAttributes.DEFAULT.copy(background = Color.BLUE)
        cell.clear(bgBlue)

        assertThat(cell.codePoint).isEqualTo(Cell.EMPTY)
        assertThat(cell.attributes.background).isEqualTo(Color.BLUE)
    }

    @Test
    fun `deep copy creates an independent cell`() {
        val original = Cell('X'.code, TextAttributes.DEFAULT.copy(foreground = Color.GREEN))
        original.setWide(true)

        val copy = Cell(original)
        original.setCodePoint('Y'.code)

        assertThat(copy.codePoint).isEqualTo('X'.code)
        assertThat(copy.isWide).isTrue()
    }

    @Test
    fun `toDisplayString returns space for empty cell`() {
        assertThat(Cell().toDisplayString()).isEqualTo(" ")
    }

    @Test
    fun `toDisplayString returns space for placeholder cell`() {
        val cell = Cell()
        cell.setPlaceholder(true)
        assertThat(cell.toDisplayString()).isEqualTo(" ")
    }

    @Test
    fun `toDisplayString returns the character for a normal cell`() {
        val cell = Cell('H'.code, TextAttributes.DEFAULT)
        assertThat(cell.toDisplayString()).isEqualTo("H")
    }
}