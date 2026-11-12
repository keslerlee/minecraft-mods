package kesler.keslerclient.feature.chat

import kesler.keslerclient.config.ConfigManager
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents

object CustomCommands {
    fun init() {
        ClientSendMessageEvents.MODIFY_COMMAND.register(::replaceCommand)
    }

    private fun replaceCommand(command: String): String {
        val input = command.trimStart().removePrefix("/")
        val commandEnd = input.indexOfFirst(Char::isWhitespace).let { if (it < 0) input.length else it }
        val inputName = input.substring(0, commandEnd).trimStart('/')
        if (inputName.isEmpty()) return command

        val target = ConfigManager.get().customCommandBindings.firstNotNullOfOrNull { line ->
            val separator = line.indexOf("->")
            if (separator < 0) return@firstNotNullOfOrNull null

            val alias = line.substring(0, separator).trim().removePrefix("/")
            val mappedCommand = line.substring(separator + 2).trim().removePrefix("/")
            if (alias.isEmpty() || alias.any(Char::isWhitespace) || mappedCommand.isEmpty()) return@firstNotNullOfOrNull null
            if (!alias.equals(inputName, ignoreCase = true)) return@firstNotNullOfOrNull null

            mappedCommand
        } ?: return command

        val arguments = input.substring(commandEnd).trimStart()
        return if (arguments.isEmpty()) target else "$target $arguments"
    }
}
