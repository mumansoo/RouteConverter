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

package slash.navigation.converter.gui.renderer;

import slash.common.io.Transfer;
import slash.navigation.base.BaseNavigationPosition;

import javax.swing.*;

/**
 * Renders the speed column of the positions table.
 *
 * @author Christian Pesch
 */

public class SpeedColumnTableCellEditor extends PositionsTableCellEditor {
    private String formatSpeed(Double speed) {
        if (Transfer.isEmpty(speed))
            return "";
        String speedStr;
        if (Math.abs(speed) < 10.0)
            speedStr = Double.toString(Transfer.roundFraction(speed, 1));
        else
            speedStr = Long.toString(Math.round(speed));
        return speedStr + " Km/h";
    }

    public SpeedColumnTableCellEditor() {
        super(RIGHT);
    }

    protected void formatCell(JLabel label, BaseNavigationPosition position) {
        label.setText(extractValue(position));
    }

    protected String extractValue(BaseNavigationPosition position) {
        return formatSpeed(position.getSpeed());
    }
}
