package kesler.keslerclient

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import kesler.keslerclient.gui.KeslerConfigScreen

class KeslerModMenu : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory { parent -> KeslerConfigScreen.create(parent) }
}