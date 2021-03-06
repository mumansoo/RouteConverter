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

package slash.navigation.simple;

import slash.common.io.CompactCalendar;
import slash.common.io.Transfer;
import slash.navigation.base.*;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads and writes i-Blue 747 (.csv) files.
 *
 * Header: INDEX,RCR,DATE,TIME,VALID,LATITUDE,N/S,LONGITUDE,E/W,HEIGHT,SPEED,HEADING,DISTANCE,<br/>
 * Format: 3656,T,2010/12/09,10:59:05,SPS,28.649061,N,17.896196,W,513.863 M,15.862 km/h,178.240250,34.60 M,
 *
 * @author Christian Pesch
 */

public class iBlue747Format extends SimpleLineBasedFormat<SimpleRoute> {
    protected static final Logger log = Logger.getLogger(iBlue747Format.class.getName());

    private static final String HEADER_LINE = "INDEX,RCR,DATE,TIME,VALID,LATITUDE,N/S,LONGITUDE,E/W,HEIGHT,SPEED,HEADING,DISTANCE,";
    private static final char SEPARATOR_CHAR = ',';
    private static final String SPACE = "\\s*";

    private static final Pattern LINE_PATTERN = Pattern.
            compile(BEGIN_OF_LINE +
                    SPACE + "(\\d+)" + SPACE + SEPARATOR_CHAR +
                    SPACE + "(\\p{Upper}+)" + SPACE + SEPARATOR_CHAR +
                    SPACE + "(\\d{4}/\\d{2}/\\d{2})" + SPACE + SEPARATOR_CHAR +
                    SPACE + "(\\d{2}:\\d{2}:\\d{2})" + SPACE + SEPARATOR_CHAR +
                    SPACE + "(.+)" + SPACE + SEPARATOR_CHAR +

                    SPACE + "([\\d\\.]+)" + SPACE + SEPARATOR_CHAR +
                    SPACE + "([NS])" + SPACE + SEPARATOR_CHAR +
                    SPACE + "([\\d\\.]+)" + SPACE + SEPARATOR_CHAR +
                    SPACE + "([WE])" + SPACE + SEPARATOR_CHAR +

                    SPACE + "([-\\d\\.]+)" + "[^" + SEPARATOR_CHAR + "]*" + SEPARATOR_CHAR +
                    SPACE + "([\\d\\.]+)" + "[^" + SEPARATOR_CHAR + "]*" + SEPARATOR_CHAR +
                    SPACE + "([\\d\\.]+)" + SPACE + SEPARATOR_CHAR +
                    SPACE + "([\\d\\.]+)" + "[^" + SEPARATOR_CHAR + "]*" + SEPARATOR_CHAR +
                    END_OF_LINE);

    private static final DateFormat DATE_AND_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        DATE_AND_TIME_FORMAT.setTimeZone(CompactCalendar.UTC);
        DATE_FORMAT.setTimeZone(CompactCalendar.UTC);
        TIME_FORMAT.setTimeZone(CompactCalendar.UTC);
    }

    public String getName() {
        return "i-Blue 747 (*" + getExtension() + ")";
    }

    public String getExtension() {
        return ".csv";
    }

    @SuppressWarnings("unchecked")
    public <P extends BaseNavigationPosition> SimpleRoute createRoute(RouteCharacteristics characteristics, String name, List<P> positions) {
        return new Wgs84Route(this, characteristics, (List<Wgs84Position>) positions);
    }

    protected RouteCharacteristics getRouteCharacteristics() {
        return RouteCharacteristics.Track;
    }

    protected boolean isValidLine(String line) {
        if(line == null)
            return false;
        if(line.startsWith(HEADER_LINE))
            return true;
        Matcher matcher = LINE_PATTERN.matcher(line);
        return matcher.matches();
    }

    protected boolean isPosition(String line) {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if(!matcher.matches())
            return false;
        String fix = matcher.group(5);
        return "SPS".equals(fix) || "DGPS".equals(fix);
     }

    private CompactCalendar parseDateAndTime(String date, String time) {
        date = Transfer.trim(date);
        time = Transfer.trim(time);
        if(date == null || time == null)
            return null;
        String dateAndTime = date + " " + time;
        try {
            Date parsed = DATE_AND_TIME_FORMAT.parse(dateAndTime);
            return CompactCalendar.fromDate(parsed);
        } catch (ParseException e) {
            log.severe("Could not parse date and time '" + dateAndTime + "'");
        }
        return null;
    }

    protected Wgs84Position parsePosition(String line, CompactCalendar startDate) {
        Matcher lineMatcher = LINE_PATTERN.matcher(line);
        if (!lineMatcher.matches())
            throw new IllegalArgumentException("'" + line + "' does not match");
        String date = lineMatcher.group(3);
        String time = lineMatcher.group(4);
        Double latitude = Transfer.parseDouble(lineMatcher.group(6));
        String northOrSouth = lineMatcher.group(7);
        if ("S".equals(northOrSouth) && latitude != null)
            latitude = -latitude;
        Double longitude = Transfer.parseDouble(lineMatcher.group(8));
        String westOrEasth = lineMatcher.group(9);
        if ("W".equals(westOrEasth) && longitude != null)
            longitude = -longitude;
        String height = lineMatcher.group(10);
        String speed = lineMatcher.group(11);
        String heading = lineMatcher.group(12);

        Wgs84Position position = new Wgs84Position(longitude, latitude, Transfer.parseDouble(height), Transfer.parseDouble(speed),
                parseDateAndTime(date, time), null);
        position.setHeading(Transfer.parseDouble(heading));
        return position;
    }

    protected void writeHeader(PrintWriter writer) {
        writer.println(HEADER_LINE);
    }

    private String formatTime(CompactCalendar time) {
        if (time == null)
            return "";
        return TIME_FORMAT.format(time.getTime());
    }

    private String formatDate(CompactCalendar date) {
        if (date == null)
            return "";
        return DATE_FORMAT.format(date.getTime());
    }

    private Wgs84Position previousPosition = null;

    protected void writePosition(Wgs84Position position, PrintWriter writer, int index, boolean firstPosition) {
        String date = formatDate(position.getTime());
        String time = formatTime(position.getTime());
        String latitude = Transfer.formatDoubleAsString(Math.abs(position.getLatitude()), 6);
        String northOrSouth = position.getLatitude() != null && position.getLatitude() < 0.0 ? "S" : "N";
        String longitude = Transfer.formatDoubleAsString(Math.abs(position.getLongitude()), 6);
        String westOrEast = position.getLongitude() != null && position.getLongitude() < 0.0 ? "W" : "E";
        String height = position.getElevation() != null ? Transfer.formatElevationAsString(position.getElevation()) : "0.0";
        String speed = position.getSpeed() != null ? Transfer.formatSpeedAsString(position.getSpeed()) : "0.0";
        String heading = position.getHeading() != null ? Transfer.formatHeadingAsString(position.getHeading()) : "0.0";

        if (firstPosition)
            previousPosition = null;
        String distance = previousPosition != null ? Transfer.formatElevationAsString(position.calculateDistance(previousPosition)) : "0.0";
        previousPosition = position;

        writer.println(Integer.toString(index + 1) + SEPARATOR_CHAR + "T" + SEPARATOR_CHAR +
                date + SEPARATOR_CHAR + time + SEPARATOR_CHAR + "SPS" + SEPARATOR_CHAR +
                latitude + SEPARATOR_CHAR + northOrSouth + SEPARATOR_CHAR +
                longitude + SEPARATOR_CHAR + westOrEast + SEPARATOR_CHAR +
                height + " M" + SEPARATOR_CHAR +
                speed + " km/h" + SEPARATOR_CHAR +
                heading + SEPARATOR_CHAR +
                distance + " M" + SEPARATOR_CHAR);
    }
}