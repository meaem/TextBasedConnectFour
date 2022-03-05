package connectfour

fun main() {
    println("Connect Four")
    print("First player's name:\n>")
    val fPlayer = readln()

    print("Second player's name:\n>")
    val sPlayer = readln()
    var rows = 6
    var cols = 7
    var invalidInput = true
    while (invalidInput) {
        println(
            "Set the board dimensions (Rows x Columns)\n" +
                    "Press Enter for default (6 x 7)"
        )
        val rxStr = "\\s*\\d+\\s*[xX]\\s*\\d+\\s*".toRegex()
        val dims = readln()

        if (!dims.isEmpty()) {
            if (rxStr.matches(dims)) {

                val (r, c) = dims.lowercase().replace("\\s+".toRegex(), "").split("x")

                rows = r.toInt()
                cols = c.toInt()
                if (!ConnectFour.validateNumRows(rows)) {
                    println("Board rows should be from 5 to 9")
                    continue
                }
                if (!ConnectFour.validateNumCols(cols)) {
                    println("Board columns should be from 5 to 9")
                    continue
                }
                invalidInput = false

            } else {
                println("Invalid input")
            }

        } else {
            invalidInput = false
        }
    }
    invalidInput = true
    var numOfGames = 1

    while (invalidInput) {
        print(
            "Do you want to play single or multiple games?\n" +
                    "For a single game, input 1 or press Enter\n" +
                    "Input a number of games:\n>"
        )
        val gameType = readln()
        if (gameType.isEmpty()) break

        try {
            numOfGames = gameType.toInt()
            if (numOfGames <= 0) {
                println("Invalid input")
            } else {
                invalidInput = false
            }
        } catch (ex: NumberFormatException) {
            println("Invalid input")
        }
    }
    val c4 = ConnectFour(fPlayer, sPlayer, rows, cols, numOfGames)

    while (c4.playing) {

        c4.play(readln())
    }
}


class ConnectFour(
    firstPlayer: String,
    secondPlayer: String,
    rows: Int, cols: Int,
    numOfGame: Int
) {
    val players = listOf(firstPlayer, secondPlayer)
    val scores = mutableListOf(0, 0)
    val rows: Int = if (validateNumRows(rows)) rows else default_rows
    val cols: Int = if (validateNumCols(cols)) cols else default_cols
    private var numOfGame = numOfGame
    private val board = Board(rows, cols)
    private var _currentPlayer = 0

    //    private var _starterPlayer = _currentPlayer
    private var miniGame = 0
//    private var miniGameEnded = true

    var playing: Boolean = true
        private set

    init {
        println(this)
        if (numOfGame > 1) {
            println("Game #${miniGame + 1}")
        }

        draw()
        printPlayerTurn()
    }

    companion object {
        val default_rows = 6
        val default_cols = 7
        val min = 5
        val max = 9
        val firstPlayerDisc = "o"
        val secondPlayerDisc = "*"

        fun validateNumRows(rows: Int): Boolean {
//            println("validating rows: $rows")
            return rows in min..max
        }

        fun validateNumCols(cols: Int): Boolean {
            return cols in min..max
        }
    }

//    fun correctCol(col: Int): Boolean = board.colIsFull(col)

    //    if (miniGameEnded){
//        println("Game #$miniGame")
//        miniGameEnded=!miniGameEnded
//    }
    fun play(input: String) {
        if (input == "end") terminate()
        else {

            try {
                val col = input.toInt()
                if (col in 1..cols) {
                    val dickIdx = board.addDisc(col - 1, _currentPlayer)

                    draw()
                    if (dickIdx != -1) {
                        if (board.isFull()) {
                            println("It is a draw")
                            scores.replaceAll { it+1 }
                            exitGame()
                            return
                        }
                        if (IsWin(dickIdx, col - 1)) {
                            println("Player ${players[_currentPlayer]} won")
                            scores[_currentPlayer] += 2
                            exitGame()
                            return
                        }
                        _currentPlayer = ++_currentPlayer % 2
                        printPlayerTurn()

                    } else {
                        println("Column $col is full")
                        printPlayerTurn()
                    }
                } else {

                    println("The column number is out of range (1 - ${cols})")
                    printPlayerTurn()
                }
            } catch (ex: NumberFormatException) {
                println("Incorrect column number")
                printPlayerTurn()
            }
        }

    }

    private fun IsWin(lastRow: Int, lastCol: Int): Boolean {
        val row = board.getRow(lastRow)
        val col = board.getCol(lastCol)
        val diag = board.getDiags(lastRow, lastCol)
        val all = mutableListOf(row, col)
        all.addAll(diag)
        for (a in all) {
            if (consigative(a, 4)) {
                return true
            }
        }

        return false
    }

    private fun consigative(row: MutableList<Int>, howMany: Int): Boolean {
        var count = howMany - 1
        for (x in 0 until row.lastIndex)
            if (row[x] != -1) {
                if (row[x] == row[x + 1]) {
                    count--
                } else {
                    count = howMany - 1
                }
                if (count == 0) return true
            } else {
                count = howMany - 1
            }
        return false
    }

    private fun draw() {
        board.drawBoard()
    }

    private fun printPlayerTurn() {
        print("${players[_currentPlayer]}'s turn:\n> ")

    }

    private fun terminate() {
        playing = false

        println("Game over!")
    }

    private fun exitGame() {
//        miniGameEnded = true
        miniGame++
        if (miniGame == numOfGame) {
            if (numOfGame > 1) {
                printScores()
            }
            terminate()
        } else {
            board.reset()
            printScores()
//            _starterPlayer = ++_starterPlayer%2
            _currentPlayer = miniGame % 2
            println("Game #${miniGame + 1}")
            draw()
            printPlayerTurn()

        }
    }

    private fun printScores() {
        println("Score")
        println("${players[0]}: ${scores[0]} ${players[1]}: ${scores[1]}")
    }

    override fun toString(): String {
        return "${players[0]} VS ${players[1]}\n" +
                board + "\n" +
                when (numOfGame) {
                    1 -> "Single game"
                    else -> "Total $numOfGame games"
                }
    }

    private inner class Board(val rows: Int, val cols: Int) {
        private var discs = rows * cols
        private val listOfCols = MutableList(cols) {

            MutableList(rows) { -1 }
        }

        fun drawBoard() {
            var rng = 0 until cols
            rng.forEach { print(" ${it + 1}") }
            println(" ")

            for (r in 0 until rows) {
                rng.forEach { print("║${getDisc(listOfCols[it][r])}") }
                println("║")
            }
            rng = 0..cols - 2
            print("╚")
            rng.forEach { print("═╩") }
            println("═╝")

        }

        fun getDisc(player: Int) = when (player) {
            0 -> firstPlayerDisc
            1 -> secondPlayerDisc
            else -> " "
        }

        fun addDisc(col: Int, disk: Int): Int {
            val idx = listOfCols[col].indexOfLast { it == -1 }
            if (idx != -1) {
                listOfCols[col][idx] = disk
                --discs
            }
            return idx
        }

        fun isFull() = discs == 0

        fun getCol(col: Int): MutableList<Int> {
            return listOfCols[col]
        }

        fun getRow(row: Int): MutableList<Int> {
            val result = MutableList(cols) { -1 }
            for (col in listOfCols) {
                result.add(col[row])
            }
            return result
        }

        override fun toString(): String {
            return "${listOfCols[0].size} X ${listOfCols.size} board"
        }

        fun getDiags(lastRow: Int, lastCol: Int): MutableList<MutableList<Int>> {
            val diag1 = mutableListOf(listOfCols[lastCol][lastRow])
            val diag2 = mutableListOf(listOfCols[lastCol][lastRow])
            var lr = lastRow
            var lc = lastCol

            while (lc > 0 && lr > 0) {
                diag1.add(0, listOfCols[--lc][--lr])
            }
            lr = lastRow
            lc = lastCol
            while (lc < cols - 1 && lr < rows - 1) {
                diag1.add(listOfCols[++lc][++lr])
            }

            lr = lastRow
            lc = lastCol
            while (lc > 0 && lr < rows - 1) {
                diag2.add(0, listOfCols[--lc][++lr])
            }
            lr = lastRow
            lc = lastCol
            while (lc < cols - 1 && lr > 0) {
//                println("lr:$lr, lc:$lc")
                diag2.add(listOfCols[++lc][--lr])
            }
            return mutableListOf(diag1, diag2)
        }

        fun reset() {
            discs=rows * cols
            for (col in listOfCols)
                col.fill(-1)

        }
    }


}
