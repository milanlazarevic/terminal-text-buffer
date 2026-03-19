package org.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TextAttributeTest {
    @Test
    fun `DEFAULT has no colours and no style flags`() {
        val a = TextAttributes.DEFAULT
        assertThat(a.foreground).isEqualTo(Color.DEFAULT)
        assertThat(a.background).isEqualTo(Color.DEFAULT)
        assertThat(a.isBold).isFalse()
        assertThat(a.isItalic).isFalse()
        assertThat(a.isUnderline).isFalse()
    }

    @Test
    fun `copy with new foreground leaves original unchanged`() {
        val original = TextAttributes.DEFAULT
        val updated  = original.copy(foreground = Color.RED)

        assertThat(updated.foreground).isEqualTo(Color.RED)
        assertThat(updated.background).isEqualTo(Color.DEFAULT)
        assertThat(original.foreground).isEqualTo(Color.DEFAULT)
    }

    @Test
    fun `withStyle sets the bold flag`() {
        val a = TextAttributes.DEFAULT.withStyle(TextAttributes.STYLE_BOLD)
        assertThat(a.isBold).isTrue()
        assertThat(a.isItalic).isFalse()
    }

    @Test
    fun `withStyle can combine multiple flags`() {
        val a = TextAttributes.DEFAULT
            .withStyle(TextAttributes.STYLE_BOLD)
            .withStyle(TextAttributes.STYLE_ITALIC)
        assertThat(a.isBold).isTrue()
        assertThat(a.isItalic).isTrue()
        assertThat(a.isUnderline).isFalse()
    }

    @Test
    fun `withoutStyle removes only the targeted flag`() {
        val a = TextAttributes.DEFAULT
            .withStyle(TextAttributes.STYLE_BOLD)
            .withStyle(TextAttributes.STYLE_ITALIC)
            .withoutStyle(TextAttributes.STYLE_BOLD)
        assertThat(a.isBold).isFalse()
        assertThat(a.isItalic).isTrue()
    }

    @Test
    fun `data class equality is field-based`() {
        val a = TextAttributes(Color.RED, Color.BLUE, TextAttributes.STYLE_BOLD)
        val b = TextAttributes(Color.RED, Color.BLUE, TextAttributes.STYLE_BOLD)
        val c = TextAttributes(Color.GREEN, Color.BLUE, TextAttributes.STYLE_BOLD)

        assertThat(a).isEqualTo(b)
        assertThat(a).isNotEqualTo(c)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `chained copies are independent`() {
        val base = TextAttributes.DEFAULT
        val a    = base.copy(foreground = Color.RED)
        val b    = base.copy(background = Color.BLUE)

        assertThat(base.foreground).isEqualTo(Color.DEFAULT)
        assertThat(base.background).isEqualTo(Color.DEFAULT)
        assertThat(a.foreground).isEqualTo(Color.RED)
        assertThat(a.background).isEqualTo(Color.DEFAULT)
        assertThat(b.foreground).isEqualTo(Color.DEFAULT)
        assertThat(b.background).isEqualTo(Color.BLUE)
    }
}