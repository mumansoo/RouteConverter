/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/

package slash.navigation.converter.gui.actions;

import slash.navigation.BaseNavigationPosition;
import slash.navigation.BaseRoute;
import slash.navigation.converter.gui.models.FormatAndRoutesModel;
import slash.navigation.converter.gui.models.PositionsModel;
import slash.navigation.gui.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ActionListener} that inserts the position list of a {@link PositionsModel} at
 * the selected row of a {@link JTable} and removes it from the {@link FormatAndRoutesModel}.
 *
 * @author Christian Pesch
 */

public class MergePositionList extends AbstractAction {
    private JFrame frame;
    private JTable table;
    private JComboBox combobox;
    private BaseRoute sourceRoute;
    private PositionsModel positionsModel;
    private FormatAndRoutesModel formatAndRoutesModel;

    public MergePositionList(JFrame frame, JTable table, JComboBox combobox, BaseRoute sourceRoute, PositionsModel positionsModel, FormatAndRoutesModel formatAndRoutesModel) {
        this.frame = frame;
        this.table = table;
        this.combobox = combobox;
        this.sourceRoute = sourceRoute;
        this.positionsModel = positionsModel;
        this.formatAndRoutesModel = formatAndRoutesModel;
        initialize();
    }

    protected void initialize() {
        // TODO disable same menu item (cannot merge me into myself)
        // TODO enable if existsMoreThanOnePosition

        // private void handleRoutesUpdate() {
        // TODO check this later buttonSplitPositionList.setEnabled(supportsMultipleRoutes && existsARoute);

        // private void handlePositionsUpdate() {
        // TODO check this later buttonSplitPositionList.setEnabled(supportsMultipleRoutes && existsMoreThanOnePosition);

        /*
        combobox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean supportsMultipleRoutes = getFormat() instanceof MultipleRoutesFormat;
                boolean existsMoreThanOnePosition = positionsModel.getRowCount() > 1;
                buttonSplitPositionList.setEnabled(supportsMultipleRoutes && existsMoreThanOnePosition);
            }
        });
        */

        setEnabled(!sourceRoute.equals(combobox.getSelectedItem()));
        combobox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                BaseRoute route = (BaseRoute) e.getItem();
                setEnabled(!sourceRoute.equals(route));
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Constants.startWaitCursor(frame.getRootPane());

            try {
                int insertRow = Math.min(selectedRow + 1, table.getRowCount());
                positionsModel.add(insertRow, sourceRoute.getPositions());
                formatAndRoutesModel.removeRoute(sourceRoute);
            }
            finally {
                Constants.stopWaitCursor(frame.getRootPane());
            }
        }
    }
}