# Terminal Buffer

A terminal text buffer implementation in Kotlin. This is the core data structure
that terminal emulators use to store and manipulate displayed text — the layer
between raw byte output and the rendered screen.

## Structure

```
src/
  main/kotlin/com/terminal/
    Color.kt            — enum of 16 ANSI colors plus DEFAULT
    TextAttributes.kt   — immutable value object: foreground, background, style flags
    Cell.kt             — one mutable cell in the grid (code point + attributes + wide flags)
    TerminalRow.kt      — a fixed-width array of cells representing one line
    CursorPosition.kt   — immutable snapshot of cursor coordinates
    TerminalBuffer.kt   — the main buffer: screen, scrollback, cursor, all editing operations

  test/kotlin/com/terminal/
    TextAttributesTest.kt   — attribute immutability, style flag logic
    CellTest.kt             — cell state, copy, clear
    CursorTest.kt           — cursor movement and clamping
    WriteTest.kt            — writeText, insertText, fillLine
    ScrollTest.kt           — insertLineAtBottom, clearScreen, clearAll, scrollback
    ContentAccessTest.kt    — getScreenContent, getAllContent, bounds checking
    WideCharTest.kt         — double-width character handling (CJK, emoji)
    ResizeTest.kt           — resize with content preservation and scrollback interaction
```

## Building and running tests

```
./gradlew test
```

## What it does

The buffer maintains two logical regions:

- **Screen** — the last N lines visible to the user, stored as a circular array for O(1) scrolling.
- **Scrollback** — lines that have scrolled off the top, stored in a bounded deque. Read-only.

Supported operations: set attributes, move cursor, write text (overwrite), insert text (shift right with wrap),
fill a line, insert blank line at bottom, clear screen, clear all, and resize.
Wide character support and resize are implemented as bonus features.
