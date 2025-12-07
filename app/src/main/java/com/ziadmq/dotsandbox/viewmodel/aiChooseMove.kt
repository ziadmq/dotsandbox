package com.ziadmq.dotsandbox.viewmodel

import com.ziadmq.dotsandbox.model.*

fun aiChooseMove(state: GameState): Line? {

    val possible = state.lines.filter { !it.isSelected }

    for (line in possible) {
        val temp = updateGameAfterLine(state, line)
        val added = temp.boxes.count { it.owner != null } - state.boxes.count { it.owner != null }
        if (added > 0) return line
    }

    return possible.randomOrNull()
}
