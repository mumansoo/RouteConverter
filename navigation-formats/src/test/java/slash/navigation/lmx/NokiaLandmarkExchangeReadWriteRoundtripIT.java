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

package slash.navigation.lmx;

import slash.navigation.base.NavigationFileParser;
import slash.navigation.base.ReadWriteBase;
import slash.navigation.base.RouteCharacteristics;
import slash.navigation.gpx.GpxPosition;
import slash.navigation.gpx.GpxRoute;
import slash.navigation.lmx.binding.LandmarkType;
import slash.navigation.lmx.binding.Lmx;
import slash.navigation.lmx.binding.MediaLinkType;

import java.io.IOException;
import java.util.List;

public class NokiaLandmarkExchangeReadWriteRoundtripIT extends ReadWriteBase {

    private void checkUnprocessed(Lmx lmx) {
        assertNotNull(lmx);
        assertNotNull(lmx.getLandmark());
        assertNotNull(lmx.getLandmarkCollection());
    }

    private void checkUnprocessed(LandmarkType type) {
        assertNotNull(type);
        assertEquals("Waypoint1 Name", type.getName());
        assertEquals("Description", type.getDescription());
        assertEquals(2.0f, type.getCoverageRadius());
        List<MediaLinkType> linkTypes = type.getMediaLink();
        assertNotNull(linkTypes);
        MediaLinkType mediaLinkType = linkTypes.get(0);
        assertEquals("URL", mediaLinkType.getUrl());
        assertEquals("URLName", mediaLinkType.getName());
        assertEquals("URLMime", mediaLinkType.getMime());
    }

    public void testNokiaLandmarkExchangeRoundtrip() throws IOException {
        readWriteRoundtrip(TEST_PATH + "from.lmx", new NavigationFileParserCallback() {
            public void test(NavigationFileParser source, NavigationFileParser target) {
                GpxRoute sourceWaypoints = (GpxRoute) source.getAllRoutes().get(0);
                assertEquals(RouteCharacteristics.Waypoints, sourceWaypoints.getCharacteristics());
                assertNotNull(sourceWaypoints.getOrigins());
                assertEquals(1, sourceWaypoints.getOrigins().size());
                checkUnprocessed(sourceWaypoints.getOrigin(Lmx.class));
                GpxPosition sourceWaypoint = sourceWaypoints.getPosition(0);
                assertNotNull(sourceWaypoint.getOrigin());
                checkUnprocessed(sourceWaypoint.getOrigin(LandmarkType.class));

                GpxRoute targetWaypoints = (GpxRoute) source.getAllRoutes().get(0);
                assertEquals(RouteCharacteristics.Waypoints, targetWaypoints.getCharacteristics());
                assertNotNull(targetWaypoints.getOrigins());
                assertEquals(1, targetWaypoints.getOrigins().size());
                checkUnprocessed(targetWaypoints.getOrigin(Lmx.class));
                GpxPosition targetWaypoint = targetWaypoints.getPosition(0);
                assertNotNull(targetWaypoint.getOrigin());
                checkUnprocessed(targetWaypoint.getOrigin(LandmarkType.class));
            }
        });
    }
}