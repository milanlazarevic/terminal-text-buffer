package org.example

class TerminalRow(val width: Int) {

    private val cells: Array<Cell> = Array(width) { Cell() }

    var softWrapped: Boolean = false

    constructor(source: TerminalRow) : this(source.width) {
        for (i in cells.indices) cells[i] = Cell(source.cells[i])
        softWrapped = source.softWrapped
    }

    fun cellAt(col: Int): Cell {
        require(col in 0 until width) { "Column $col out of bounds for width $width" }
        return cells[col]
    }

    fun clearAll() {
        cells.forEach { it.clear() }
        softWrapped = false
    }

    fun fill(codePoint: Int, attributes: TextAttributes) {
        cells.forEach { cell ->
            cell.setCodePoint(codePoint)
            cell.setAttributes(attributes)
            cell.setWide(false)
            cell.setPlaceholder(false)
        }
    }

    fun toRawString(): String = buildString(width) {
        cells.forEach { append(it.toDisplayString()) }
    }

    fun toTrimmedString(): String = toRawString().trimEnd()
}