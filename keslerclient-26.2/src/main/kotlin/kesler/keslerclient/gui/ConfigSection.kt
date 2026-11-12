package kesler.keslerclient.gui

import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder

interface ConfigSection {
    val title: String
    fun build(category: ConfigCategory, entries: ConfigEntryBuilder)
}