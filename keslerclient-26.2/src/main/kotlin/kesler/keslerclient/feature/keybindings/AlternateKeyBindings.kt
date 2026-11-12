package kesler.keslerclient.feature.keybindings

import com.mojang.blaze3d.platform.InputConstants
import kesler.keslerclient.config.ConfigManager
import kesler.keslerclient.mixin.KeyMappingAccessor
import kesler.keslerclient.mixin.KeyMappingMapAccessor
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft

object AlternateKeyBindings {
    private data class Alias(val key: InputConstants.Key, val action: KeyMapping)

    private val registeredAliases = mutableListOf<Alias>()
    @Volatile private var activeAliases: List<Alias> = emptyList()
    private val activeAliasKeys = mutableSetOf<InputConstants.Key>()
    private val heldAliasKeys = mutableSetOf<InputConstants.Key>()
    private val pendingAliasClicks = mutableSetOf<InputConstants.Key>()

    @JvmStatic
    fun init() = refreshMappings()

    @JvmStatic
    fun onVanillaMappingsReset() {
        registeredAliases.clear()
        activeAliases = emptyList()
        refreshMappings()
    }

    @JvmStatic
    fun refreshMappings() {
        activeAliases = emptyList()
        heldAliasKeys.clear()
        pendingAliasClicks.clear()
        activeAliasKeys.clear()
        val map = KeyMappingMapAccessor.getMap()
        registeredAliases.forEach { alias ->
            val mapped = map[alias.key] ?: return@forEach
            mapped.remove(alias.action)
            if (mapped.isEmpty()) map.remove(alias.key)
        }
        registeredAliases.clear()

        val options = runCatching { Minecraft.getInstance().options }.getOrNull() ?: return
        val cfg = ConfigManager.get()
        val configured = listOf(
            cfg.additionalAttackKey to options.keyAttack,
            cfg.additionalUseKey to options.keyUse,
            cfg.additionalForwardKey to options.keyUp,
            cfg.additionalBackKey to options.keyDown,
            cfg.additionalLeftKey to options.keyLeft,
            cfg.additionalRightKey to options.keyRight,
            cfg.additionalJumpKey to options.keyJump,
            cfg.additionalSneakKey to options.keyShift,
            cfg.additionalSprintKey to options.keySprint
        )

        val aliases = mutableListOf<Alias>()
        configured.forEach { (storedKey, action) ->
            val key = runCatching { InputConstants.getKey(storedKey) }.getOrDefault(InputConstants.UNKNOWN)
            val primaryKey = (action as KeyMappingAccessor).keslerGetKey()
            if (key == InputConstants.UNKNOWN || key.type != InputConstants.Type.KEYSYM || key == primaryKey) return@forEach

            val mapped = map.getOrPut(key) { mutableListOf() }
            if (!mapped.contains(action)) {
                mapped.add(action)
                val alias = Alias(key, action)
                aliases.add(alias)
                registeredAliases.add(alias)
            }
        }
        activeAliases = aliases
        activeAliasKeys.addAll(aliases.map { it.key })
    }

    @JvmStatic
    fun onKeyMappingStateChanged(key: InputConstants.Key, down: Boolean) {
        if (key !in activeAliasKeys) return
        if (down) {
            if (heldAliasKeys.add(key)) pendingAliasClicks.add(key)
        } else {
            heldAliasKeys.remove(key)
            pendingAliasClicks.remove(key)
        }
    }

    @JvmStatic
    fun shouldAllowKeyClick(key: InputConstants.Key): Boolean =
        key !in activeAliasKeys || pendingAliasClicks.remove(key)

    @JvmStatic
    fun clearSecondaryKeyStates() {
        heldAliasKeys.clear()
        pendingAliasClicks.clear()
    }

    @JvmStatic
    fun applySecondaryKeyStates() {
        val client = Minecraft.getInstance()
        if (client.gui.screen() != null) return
        val window = client.window
        activeAliases.forEach { alias ->
            if (InputConstants.isKeyDown(window, alias.key.value)) {
                alias.action.setDown(true)
            }
        }
    }
}
