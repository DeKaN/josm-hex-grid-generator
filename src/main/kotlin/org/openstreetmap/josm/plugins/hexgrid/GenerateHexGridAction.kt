package org.openstreetmap.josm.plugins.hexgrid

import org.openstreetmap.josm.actions.JosmAction
import org.openstreetmap.josm.command.AddCommand
import org.openstreetmap.josm.command.SequenceCommand
import org.openstreetmap.josm.data.UndoRedoHandler
import org.openstreetmap.josm.data.osm.BBox
import org.openstreetmap.josm.data.preferences.DoubleProperty
import org.openstreetmap.josm.tools.Geometry
import org.openstreetmap.josm.tools.I18n.tr
import java.awt.event.ActionEvent

class GenerateHexGridAction : JosmAction(
    tr("Generate hexgrid"),
    null,
    tr("Generate hexgrid for area"),
    null,
    false
) {
    private val cellSideProperty = DoubleProperty("hexgrid.cellside", 10.0)
    override fun actionPerformed(e: ActionEvent?) {
        val dataset = layerManager.editDataSet ?: return
        val selectedWays = dataset.selectedWays.takeUnless { it.isEmpty() } ?: return

        val dialog = HexGridSideInputDialog(cellSideProperty.get())
        if (dialog.showDialog().value != 1)
            return

        val cellSide = dialog.cellSideInput.value as? Double ?: return
        cellSideProperty.put(cellSide)
        val bBox = selectedWays.map { it.bBox }.fold(BBox()) { acc, bBox -> acc.apply { add(bBox) } }
        val selectedArea = Geometry.getArea(selectedWays.flatMap { it.nodes }.distinctBy { it.uniqueId })
        val hexGridWays = HexGridGenerator.buildHexGridWays(bBox, cellSide).filter {
            Geometry.polygonIntersection(
                selectedArea,
                Geometry.getArea(it.nodes)
            ) != Geometry.PolygonIntersection.OUTSIDE
        }.onEachIndexed { index, way ->
            way.apply {
                put("building", "yes")
                put("ref", (index + 1).toString())
            }
        }.toList()
        val commands = hexGridWays.asSequence().flatMap { w ->
            w.nodes.distinctBy { it.uniqueId }.map { AddCommand(dataset, it) } + AddCommand(dataset, w)
        }.toList().takeUnless { it.isEmpty() } ?: return
        UndoRedoHandler.getInstance().add(SequenceCommand(tr("Add hexgrid"), commands))
        dataset.setSelected(hexGridWays)
    }
}
