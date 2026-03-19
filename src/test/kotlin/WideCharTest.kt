package org.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WideCharTest {

    // '日'
    private val KANJI = 0x65E5

    private lateinit var buf: TerminalBuffer

    @BeforeEach
    fun setUp() {
        buf = TerminalBuffer(10, 5, 100)
    }

    @Test
    fun `CJK ideographs are wide`() {
        assertThat(TerminalBuffer.isWideCharacter(0x65E5)).isTrue()
        assertThat(TerminalBuffer.isWideCharacter(0x4E2D)).isTrue()
    }

    @Test
    fun `emoji are wide`() {
        assertThat(TerminalBuffer.isWideCharacter(0x1F600)).isTrue()
    }

    @Test
    fun `ASCII characters are not wide`() {
        assertThat(TerminalBuffer.isWideCharacter('A'.code)).isFalse()
        assertThat(TerminalBuffer.isWideCharacter('z'.code)).isFalse()
        assertThat(TerminalBuffer.isWideCharacter(' '.code)).isFalse()
    }


    @Test
    fun `writing a wide char marks left cell as wide and right as placeholder`() {
        buf.setCursor(0, 0)
        buf.writeText(String(Character.toChars(KANJI)))

        val left  = buf.screenCellAt(0, 0)
        val right = buf.screenCellAt(0, 1)

        assertThat(left.codePoint).isEqualTo(KANJI)
        assertThat(left.isWide).isTrue()
        assertThat(left.isPlaceholder).isFalse()
        assertThat(right.isPlaceholder).isTrue()
        assertThat(right.isWide).isFalse()
    }

    @Test
    fun `writing a wide char advances cursor by two`() {
        buf.setCursor(0, 0)
        buf.writeText(String(Character.toChars(KANJI)))
        assertThat(buf.cursor.col).isEqualTo(2)
    }

    @Test
    fun `wide char at last column is skipped`() {
        buf.setCursor(0, 9)
        buf.writeText(String(Character.toChars(KANJI)))
        assertThat(buf.getCharAt(0, 9)).isEqualTo(Cell.EMPTY)
    }

    @Test
    fun `mixed narrow and wide string is laid out correctly`() {
        buf.setCursor(0, 0)
        buf.writeText("A" + String(Character.toChars(KANJI)) + "B")

        assertThat(buf.getCharAt(0, 0)).isEqualTo('A'.code)
        assertThat(buf.getCharAt(0, 1)).isEqualTo(KANJI)
        assertThat(buf.screenCellAt(0, 2).isPlaceholder).isTrue()
        assertThat(buf.getCharAt(0, 3)).isEqualTo('B'.code)
        assertThat(buf.cursor.col).isEqualTo(4)
    }


    @Test
    fun `overwriting wide char left half with narrow char clears placeholder`() {
        buf.setCursor(0, 0); buf.writeText(String(Character.toChars(KANJI)))
        buf.setCursor(0, 0); buf.writeText("X")

        assertThat(buf.getCharAt(0, 0)).isEqualTo('X'.code)
        assertThat(buf.screenCellAt(0, 0).isWide).isFalse()
        assertThat(buf.screenCellAt(0, 1).isPlaceholder).isFalse()
    }

    @Test
    fun `overwriting wide char right half clears the owning wide cell`() {
        buf.setCursor(0, 0); buf.writeText(String(Character.toChars(KANJI)))
        buf.setCursor(0, 1); buf.writeText("Y")

        assertThat(buf.screenCellAt(0, 0).isWide).isFalse()
        assertThat(buf.getCharAt(0, 0)).isEqualTo(Cell.EMPTY)
        assertThat(buf.getCharAt(0, 1)).isEqualTo('Y'.code)
    }


    @Test
    fun `row containing wide char renders placeholder as space`() {
        buf.setCursor(0, 0)
        buf.writeText(String(Character.toChars(KANJI)))

        val raw = buf.getScreenLine(0)
        val expected = String(Character.toChars(KANJI)) + " "
        assertThat(raw).startsWith(expected)
    }
}