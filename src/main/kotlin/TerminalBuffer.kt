package org.example

import java.util.ArrayDeque

class TerminalBuffer(
    width: Int,
    height: Int,
    val maxScrollbackLines: Int,
) {
    var width:  Int = width;  private set
    var height: Int = height; private set

    init {
        require(width  > 0) { "width must be > 0" }
        require(height > 0) { "height must be > 0" }
        require(maxScrollbackLines >= 0) { "maxScrollbackLines must be >= 0" }
    }

    private var screen: Array<TerminalRow> = Array(height) { TerminalRow(width) }

    private var firstRow: Int = 0

    private val scrollback: ArrayDeque<TerminalRow> = ArrayDeque()

    val scrollbackSize: Int get() = scrollback.size

    private var cursorRow: Int = 0
    private var cursorCol: Int = 0

    val cursor: CursorPosition get() = CursorPosition(cursorRow, cursorCol)

    fun setCursor(row: Int, col: Int) {
        cursorRow = row.coerceIn(0, height - 1)
        cursorCol = col.coerceIn(0, width  - 1)
    }

    fun moveCursorUp(n: Int)    { cursorRow = (cursorRow - n).coerceIn(0, height - 1) }
    fun moveCursorDown(n: Int)  { cursorRow = (cursorRow + n).coerceIn(0, height - 1) }
    fun moveCursorLeft(n: Int)  { cursorCol = (cursorCol - n).coerceIn(0, width  - 1) }
    fun moveCursorRight(n: Int) { cursorCol = (cursorCol + n).coerceIn(0, width  - 1) }

    var currentAttributes: TextAttributes = TextAttributes.DEFAULT

    fun setForeground(color: Color) {
        currentAttributes = currentAttributes.copy(foreground = color)
    }

    fun setBackground(color: Color) {
        currentAttributes = currentAttributes.copy(background = color)
    }

    fun setStyle(flag: Int, enabled: Boolean) {
        currentAttributes = if (enabled) currentAttributes.withStyle(flag)
        else         currentAttributes.withoutStyle(flag)
    }

    fun writeText(text: String?) {
        if (text.isNullOrEmpty()) return
        val row = screenRow(cursorRow)
        text.codePoints().forEach { cp ->
            if (cursorCol >= width) return@forEach
            if (isWideCharacter(cp)) {
                if (cursorCol + 1 >= width) return@forEach
                clearWideAt(row, cursorCol + 1)
                row.cellAt(cursorCol).apply {
                    setCodePoint(cp)
                    setAttributes(currentAttributes)
                    setWide(true)
                    setPlaceholder(false)
                }
                row.cellAt(cursorCol + 1).apply {
                    setCodePoint(Cell.EMPTY)
                    setAttributes(currentAttributes)
                    setWide(false)
                    setPlaceholder(true)
                }
                cursorCol += 2
            } else {
                clearWideAt(row, cursorCol)
                row.cellAt(cursorCol).apply {
                    setCodePoint(cp)
                    setAttributes(currentAttributes)
                    setWide(false)
                    setPlaceholder(false)
                }
                cursorCol++
            }
        }
    }

    fun insertText(text: String?) {
        if (text.isNullOrEmpty()) return
        text.codePoints().forEach { cp -> insertCodePoint(cp) }
    }


    fun fillLine(row: Int, codePoint: Int) {
        screenRow(row).fill(codePoint, currentAttributes)
    }

    fun insertLineAtBottom() {
        pushToScrollback(TerminalRow(screenRow(0)))
        firstRow = (firstRow + 1) % height
        screenRow(height - 1).clearAll()
        if (cursorRow > 0) cursorRow--
    }

    fun clearScreen() {
        for (r in 0 until height) screenRow(r).clearAll()
        cursorRow = 0
        cursorCol = 0
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }



    fun getCharAt(row: Int, col: Int): Int =
        screenRow(row).cellAt(col).codePoint

    fun getAttributesAt(row: Int, col: Int): TextAttributes =
        screenRow(row).cellAt(col).attributes

    fun getScreenLine(row: Int): String = screenRow(row).toRawString()


    fun getScreenContent(): String = buildString {
        for (r in 0 until height) {
            if (r > 0) append('\n')
            append(screenRow(r).toRawString())
        }
    }

    fun getScrollbackCharAt(scrollbackRow: Int, col: Int): Int =
        scrollbackRow(scrollbackRow).cellAt(col).codePoint

    fun getScrollbackAttributesAt(scrollbackRow: Int, col: Int): TextAttributes =
        scrollbackRow(scrollbackRow).cellAt(col).attributes

    fun getScrollbackLine(scrollbackRow: Int): String =
        scrollbackRow(scrollbackRow).toRawString()

    fun getAllContent(): String = buildString {
        var first = true
        scrollback.forEach { row ->
            if (!first) append('\n')
            append(row.toRawString())
            first = false
        }
        for (r in 0 until height) {
            if (!first) append('\n')
            append(screenRow(r).toRawString())
            first = false
        }
    }

    fun resize(newWidth: Int, newHeight: Int) {
        require(newWidth  > 0) { "width must be > 0"  }
        require(newHeight > 0) { "height must be > 0" }

        val oldRows: MutableList<TerminalRow> = (0 until height).mapTo(mutableListOf()) { screenRow(it) }

        val heightDiff = newHeight - height
        when {
            heightDiff > 0 -> {
                val toRestore = minOf(heightDiff, scrollback.size)
                repeat(toRestore) { oldRows.add(0, scrollback.removeLast()) }
                while (oldRows.size < newHeight) oldRows.add(0, TerminalRow(newWidth))
            }
            heightDiff < 0 -> {
                repeat(-heightDiff) { if (oldRows.isNotEmpty()) pushToScrollback(oldRows.removeAt(0)) }
            }
        }

        val newScreen = Array(newHeight) { r ->
            TerminalRow(newWidth).also { newRow ->
                if (r < oldRows.size) {
                    val old = oldRows[r]
                    val copyWidth = minOf(old.width, newWidth)
                    for (c in 0 until copyWidth) {
                        val src  = old.cellAt(c)
                        val dest = newRow.cellAt(c)
                        dest.setCodePoint(src.codePoint)
                        dest.setAttributes(src.attributes)
                        dest.setWide(src.isWide)
                        dest.setPlaceholder(src.isPlaceholder)
                    }
                    if (copyWidth < old.width && newRow.cellAt(copyWidth - 1).isWide) {
                        newRow.cellAt(copyWidth - 1).setWide(false)
                    }
                }
            }
        }

        screen   = newScreen
        firstRow = 0
        width    = newWidth
        height   = newHeight

        cursorRow = cursorRow.coerceIn(0, height - 1)
        cursorCol = cursorCol.coerceIn(0, width  - 1)
    }

    private fun physicalRow(logicalRow: Int): Int = (firstRow + logicalRow) % height

    private fun screenRow(logicalRow: Int): TerminalRow {
        require(logicalRow in 0 until height) {
            "Screen row $logicalRow out of bounds [0, $height)"
        }
        return screen[physicalRow(logicalRow)]
    }

    private fun scrollbackRow(index: Int): TerminalRow {
        require(index in 0 until scrollback.size) {
            "Scrollback row $index out of bounds [0, ${scrollback.size})"
        }
        return scrollback.toList()[index]
    }

    private fun pushToScrollback(row: TerminalRow) {
        if (maxScrollbackLines == 0) return
        if (scrollback.size >= maxScrollbackLines) scrollback.removeFirst()
        scrollback.addLast(row)
    }

    private fun insertCodePoint(cp: Int) {
        if (isWideCharacter(cp)) { insertWideCodePoint(cp); return }

        val row = screenRow(cursorRow)
        for (c in width - 1 downTo cursorCol + 1) {
            val src  = row.cellAt(c - 1)
            val dest = row.cellAt(c)
            dest.setCodePoint(src.codePoint)
            dest.setAttributes(src.attributes)
            dest.setWide(src.isWide)
            dest.setPlaceholder(src.isPlaceholder)
        }
        row.cellAt(cursorCol).apply {
            setCodePoint(cp)
            setAttributes(currentAttributes)
            setWide(false)
            setPlaceholder(false)
        }
        cursorCol++
        if (cursorCol >= width) {
            cursorCol = 0
            if (cursorRow < height - 1) cursorRow++
        }
    }

    private fun insertWideCodePoint(cp: Int) {
        if (cursorCol + 1 >= width) return

        val row = screenRow(cursorRow)
        for (c in width - 1 downTo cursorCol + 2) {
            val src  = row.cellAt(c - 2)
            val dest = row.cellAt(c)
            dest.setCodePoint(src.codePoint)
            dest.setAttributes(src.attributes)
            dest.setWide(src.isWide)
            dest.setPlaceholder(src.isPlaceholder)
        }
        row.cellAt(cursorCol).apply {
            setCodePoint(cp)
            setAttributes(currentAttributes)
            setWide(true)
            setPlaceholder(false)
        }
        row.cellAt(cursorCol + 1).apply {
            setCodePoint(Cell.EMPTY)
            setAttributes(currentAttributes)
            setWide(false)
            setPlaceholder(true)
        }
        cursorCol += 2
        if (cursorCol >= width) {
            cursorCol = 0
            if (cursorRow < height - 1) cursorRow++
        }
    }
    private fun clearWideAt(row: TerminalRow, col: Int) {
        val cell = row.cellAt(col)
        when {
            cell.isPlaceholder && col > 0 -> {
                row.cellAt(col - 1).apply { setWide(false); setCodePoint(Cell.EMPTY) }
            }
            cell.isWide && col + 1 < width -> {
                row.cellAt(col + 1).apply { setPlaceholder(false); setCodePoint(Cell.EMPTY) }
            }
        }
    }

    internal fun screenCellAt(row: Int, col: Int): Cell = screenRow(row).cellAt(col)

    companion object {
        fun isWideCharacter(cp: Int): Boolean {
            if (cp < 0x1100) return false
            return when {
                cp <= 0x115F              -> true   // Hangul Jamo
                cp  < 0x2E80             -> false
                cp <= 0x303E             -> true    // CJK Radicals & Kangxi
                cp in 0x3041..0x33FF     -> true    // Hiragana … CJK Compat
                cp in 0x3400..0x4DBF     -> true    // CJK Ext A
                cp in 0x4E00..0x9FFF     -> true    // CJK Unified Ideographs
                cp in 0xA000..0xA4CF     -> true    // Yi
                cp in 0xAC00..0xD7AF     -> true    // Hangul Syllables
                cp in 0xF900..0xFAFF     -> true    // CJK Compat Ideographs
                cp in 0xFE10..0xFE19     -> true    // Vertical forms
                cp in 0xFE30..0xFE6F     -> true    // CJK Compat Forms
                cp in 0xFF01..0xFF60     -> true    // Fullwidth
                cp in 0xFFE0..0xFFE6     -> true    // Fullwidth Signs
                cp in 0x1B000..0x1B0FF   -> true    // Kana Supplement
                cp in 0x1F004..0x1F0CF   -> true    // Mahjong / Playing cards
                cp in 0x1F300..0x1F9FF   -> true    // Misc symbols & Emoji
                cp in 0x20000..0x2FFFD   -> true    // CJK Ext B-F
                cp in 0x30000..0x3FFFD   -> true    // CJK Ext G+
                else                     -> false
            }
        }
    }
}