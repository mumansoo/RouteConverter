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

package slash.common.io;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Provides version parsing functionality.
 *
 * @author Christian Pesch
 */

public class Version {
    private static final String ROUTECONVERTER_VERSION_KEY = "routeconverter.version";
    private static final String ROUTECONVERTER_IS_LATEST_KEY = "routeconverter.islatest";

    public static String getSystemProperty(String propertyName) {
        String propertyValue = propertyName;
        propertyValue += "=";
        try {
            propertyValue += System.getProperty(propertyName);
            if (propertyValue == null)
                propertyValue = "";
        }
        catch (Throwable t) {
            propertyValue += t.getMessage();
        }
        propertyValue += ",";
        return propertyValue;
    }

    public static String getRouteConverterVersion(String version) {
        return ROUTECONVERTER_VERSION_KEY + "=" + version + ",";
    }

    public static Map<String, String> parseParameters(String parameters) {
        StringTokenizer tokenizer = new StringTokenizer(parameters, ",");
        Map<String, String> map = new HashMap<String, String>();
        while (tokenizer.hasMoreTokens()) {
            String nv = tokenizer.nextToken();
            StringTokenizer nvTokenizer = new StringTokenizer(nv, "=");
            if (!nvTokenizer.hasMoreTokens())
                continue;
            String key = nvTokenizer.nextToken();
            if (!nvTokenizer.hasMoreTokens())
                continue;
            String value = nvTokenizer.nextToken();
            map.put(key, value);
        }
        return map;
    }

    public static String parseVersionFromParameters(String parameters) {
        Map<String, String> map = parseParameters(parameters);
        return map.get(ROUTECONVERTER_VERSION_KEY);
    }

    public static boolean isLatestVersionFromParameters(String parameters) {
        Map<String, String> map = parseParameters(parameters);
        return Boolean.parseBoolean(map.get(ROUTECONVERTER_IS_LATEST_KEY));
    }


    private static final SimpleDateFormat BUILD_DATE = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String version, date;

    public Version(String version, String date) {
        this.version = version;
        this.date = date;
    }

    public Version(String version) {
        this(version, null);
    }

    public String getMajor() {
        int dot = version.indexOf('.');
        if (dot != -1)
            return version.substring(0, dot);
        return version;
    }

    public String getMinor() {
        int dot = version.indexOf('.');
        if (dot != -1)
            version = version.substring(dot + 1);
        return version;
    }

    private String sameLength(String reference, String hasToHaveSameLength) {
        while (hasToHaveSameLength.length() < reference.length())
            hasToHaveSameLength = "0" + hasToHaveSameLength;
        return hasToHaveSameLength;
    }

    private String removeSnapshot(String string) {
        int index = string.indexOf("-");
        if (index != -1)
            string = string.substring(0, index);
        int dot = string.indexOf(".");
        if (dot != -1)
            string = string.substring(0, dot);
        return string;
    }

    public boolean isLaterVersionThan(Version other) {
        String major = getMajor();
        String otherMajor = sameLength(major, other.getMajor());
        int result = otherMajor.compareTo(major);
        if (result != 0)
            return result <= 0;

        String minor = removeSnapshot(getMinor());
        String otherMinor = sameLength(minor, removeSnapshot(other.getMinor()));
        result = otherMinor.compareTo(minor);
        return result <= 0;
    }

    public String getVersion() {
        if (version != null) {
            if (version.contains("-SNAPSHOT"))
                return version;
            int index = version.indexOf('-');
            if (index != -1)
                return version.substring(0, index);
            else
                return version;
        }
        return "?";
    }

    public String getDate() {
        if (date != null) {
            try {
                Date date = BUILD_DATE.parse(this.date);
                DateFormat format = DateFormat.getDateInstance(DateFormat.LONG);
                format.setTimeZone(CompactCalendar.UTC);
                return format.format(date);
            }
            catch (ParseException e) {
                // intentionally ignored
            }
        }
        return "?";
    }

    public static Version parseVersionFromManifest() {
        return new Version(Version.class.getPackage().getSpecificationVersion(),
                Version.class.getPackage().getImplementationVersion());
    }
}
