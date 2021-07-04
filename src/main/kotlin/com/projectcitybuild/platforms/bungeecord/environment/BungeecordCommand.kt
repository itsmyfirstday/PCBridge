package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.core.entities.CommandResult
import net.md_5.bungee.api.connection.ProxiedPlayer

data class BungeecordCommandInput(
        val sender: ProxiedPlayer?,
        val args: List<String>,
        val isSenderConsole: Boolean
)

interface BungeecordCommand {

    // String used to execute the command in-game (eg. "ban", "unban")
    val label: String

    // Alternative Strings to execute the command in-game
    val aliases: Array<String>
        get() = arrayOf()

    // Permission node required to execute the command
    val permission: String

    fun execute(input: BungeecordCommandInput): CommandResult
}