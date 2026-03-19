package org.example

data class TextAttributes(
    val foreground: Color = Color.DEFAULT,
    val background: Color = Color.DEFAULT,
    val styleMask: Int = 0,
) {
    val isBold:      Boolean get() = styleMask and STYLE_BOLD      != 0
    val isItalic:    Boolean get() = styleMask and STYLE_ITALIC    != 0
    val isUnderline: Boolean get() = styleMask and STYLE_UNDERLINE != 0

    fun withStyle(flag: Int)    = copy(styleMask = styleMask or  flag)
    fun withoutStyle(flag: Int) = copy(styleMask = styleMask and flag.inv())

    companion object {
        const val STYLE_BOLD      = 1
        const val STYLE_ITALIC    = 2
        const val STYLE_UNDERLINE = 4

        val DEFAULT = TextAttributes()
    }
}