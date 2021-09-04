package org.openstreetmap.josm.plugins.hexgrid

import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.gui.MainMenu
import org.openstreetmap.josm.plugins.Plugin
import org.openstreetmap.josm.plugins.PluginInformation

class HexGridPlugin(info: PluginInformation) : Plugin(info) {
    init {
        MainMenu.add(MainApplication.getMenu().moreToolsMenu, GenerateHexGridAction())
    }
}
