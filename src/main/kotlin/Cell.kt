package org.example


class Cell(
    codePoint: Int = EMPTY,
    attributes: TextAttributes = TextAttributes.DEFAULT,
) {
    var codePoint:  Int            = codePoint;  private set
    var attributes: TextAttributes = attributes; private set
    var isWide:        Boolean = false;          private set
    var isPlaceholder: Boolean = false;          private set

    val isEmpty: Boolean get() = codePoint == EMPTY && !isPlaceholder

    constructor(source: Cell) : this(source.codePoint, source.attributes) {
        isWide        = source.isWide
        isPlaceholder = source.isPlaceholder
    }

    internal fun setCodePoint(cp: Int)               { codePoint  = cp   }
    internal fun setAttributes(a: TextAttributes)    { attributes = a    }
    internal fun setWide(wide: Boolean)              { isWide     = wide }
    internal fun setPlaceholder(ph: Boolean)         { isPlaceholder = ph }

    internal fun clear() {
        codePoint   = EMPTY
        attributes  = TextAttributes.DEFAULT
        isWide      = false
        isPlaceholder = false
    }

    internal fun clear(fillAttrs: TextAttributes) {
        codePoint   = EMPTY
        attributes  = fillAttrs
        isWide      = false
        isPlaceholder = false
    }


    fun toDisplayString(): String =
        if (codePoint == EMPTY || isPlaceholder) " "
        else String(Character.toChars(codePoint))

    companion object {
        const val EMPTY = 0
    }
}