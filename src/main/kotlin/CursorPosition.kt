package org.example

data class CursorPosition(val row: Int, val col: Int){
    init {
        require(row >= 0) {"row must be >= 0, got $row"}
        require(col >= 0) {"col must be >= 0, got $col"}
    }
}
