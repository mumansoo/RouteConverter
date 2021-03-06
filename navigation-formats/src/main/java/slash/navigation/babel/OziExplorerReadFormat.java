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

package slash.navigation.babel;

import slash.common.io.Transfer;
import slash.navigation.base.MultipleRoutesFormat;
import slash.common.io.CompactCalendar;
import slash.navigation.gpx.GpxRoute;
import slash.navigation.gpx.GpxPosition;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads OziExplorer Route (.rte), Track (.plt) and Waypoint (.wpt) files.
 *
 * @author Christian Pesch
 */

public class OziExplorerReadFormat extends BabelFormat implements MultipleRoutesFormat<GpxRoute> {
    public String getExtension() {
        return ".plt/.rte/.wpt";
    }

    public String getName() {
        return "OziExplorer (*" + getExtension() + ")";
    }

    protected String getFormatName() {
        return "ozi";
    }

    public boolean isSupportsReading() {
        return true;
    }

    public boolean isSupportsWriting() {
        return false;
    }

    public boolean isSupportsMultipleRoutes() {
        return true;
    }

    protected boolean isStreamingCapable() {
        return true;
    }

    private boolean isValidRoute(List<GpxPosition> positions) {
        int count = 0;
        for (GpxPosition position : positions) {
            if ((!position.hasCoordinates() ||
                    (Transfer.isEmpty(position.getLongitude()) && Transfer.isEmpty(position.getLatitude())) && Transfer.isEmpty(position.getElevation())))
                count++;
        }
        return count != positions.size();
    }

    public List<GpxRoute> read(InputStream source, CompactCalendar startDate) throws IOException {
        List<GpxRoute> routes = super.read(source, startDate);
        if (routes == null)
            return null;

        List<GpxRoute> result = new ArrayList<GpxRoute>();
        for (GpxRoute route : routes) {
            // has lots of zero element routes and routes with only one 0.0/0.0 waypoint
            if (isValidRoute(route.getPositions()))
                result.add(route);
        }
        return result.size() > 0 ? result : null;
    }
}