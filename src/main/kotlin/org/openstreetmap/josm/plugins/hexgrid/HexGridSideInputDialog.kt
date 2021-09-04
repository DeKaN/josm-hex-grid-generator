package org.openstreetmap.josm.plugins.hexgrid

import org.openstreetmap.josm.gui.ExtendedDialog
import org.openstreetmap.josm.gui.MainApplication
import org.openstreetmap.josm.gui.util.WindowGeometry
import org.openstreetmap.josm.tools.GBC
import org.openstreetmap.josm.tools.I18n.tr
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class HexGridSideInputDialog(initialCellSize: Double) : ExtendedDialog(
    MainApplication.getMainFrame(),
    tr("Enter hexagon cell side length"),
    tr("OK"),
    tr("Cancel")
) {
    val cellSideInput = JSpinner(SpinnerNumberModel(initialCellSize, 1.0, 5000.0, 1.0))

    init {
        val panel = JPanel(GridBagLayout()).apply {
            add(JLabel(tr("Cell side:")), GBC.std())
            add(cellSideInput, GBC.std().insets(5))
            add(JLabel(tr("kilometers")), GBC.eol())
        }
        setContent(panel)
        setButtonIcons("ok", "cancel")
        setDefaultButton(1)
        setRememberWindowGeometry(
            "${javaClass.name}.geometry",
            WindowGeometry.centerInWindow(MainApplication.getMainFrame(), preferredSize)
        )
        pack()
    }
}
