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

package slash.navigation.bcr;

import slash.navigation.base.MercatorPosition;
import slash.navigation.gopal.GoPalPosition;
import slash.navigation.tour.TourPosition;
import slash.common.io.CompactCalendar;
import slash.navigation.util.Conversion;
import slash.common.io.Transfer;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Represents a position in a Map&Guide Tourenplaner Route (.bcr) file.
 * <p>Currently, the metrics of the altitude field is unclear.
 * Numbers range in the area of 210 billion plus something.
 *
 * @author Christian Pesch
 */

public class BcrPosition extends MercatorPosition {
    public static final int NO_ALTITUDE_DEFINED = 999999999;
    static final String STREET_DEFINES_CENTER_SYMBOL = "@";
    static final String STREET_DEFINES_CENTER_NAME = "Zentrum";
    static final String ZIPCODE_DEFINES_NOTHING = "WP";
    private static final DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN);

    static {
        decimalFormat.applyPattern("###,##0.00");
    }

    private long altitude;
    private String zipCode, street, type; // comment = city

    public BcrPosition(Double longitude, Double latitude, Double elevation, Double speed, CompactCalendar time, String comment) {
        super(longitude, latitude, elevation, speed, time, comment);
        this.altitude = asAltitude(elevation);
    }

    public BcrPosition(long x, long y, Double elevation, String comment) {
        this(x, y, asAltitude(elevation), comment);
    }

    public BcrPosition(long x, long y, long altitude, String comment) {
        super(x, y, null, null, null, comment);
        this.altitude = altitude;
    }

    private static long asAltitude(Double elevation) {
        return elevation != null ? Conversion.elevationMetersToBcrAltitude(elevation) : NO_ALTITUDE_DEFINED;
    }


    public Double getElevation() {
        return altitude != NO_ALTITUDE_DEFINED ? Conversion.bcrAltitudeToElevationMeters(getAltitude()) : null;
    }

    public void setElevation(Double elevation) {
        this.altitude = asAltitude(elevation);
    }

    public boolean isUnstructured() {
        return getZipCode() == null && getStreet() == null && getType() == null;
    }

    public String getComment() {
        String result = (getZipCode() != null ? getZipCode() + " " : "") +
                (getCity() != null ? getCity() : "") +
                (getStreet() != null ? ", " + getStreet() : "");
        return result.length() > 0 ? result : null;
    }

    public void setComment(String comment) {
        this.zipCode = null;
        this.comment = comment;
        this.street = null;
        this.type = null;

        if (comment == null)
            return;

        Matcher matcher = MTP0809Format.DESCRIPTION_PATTERN.matcher(comment);
        if (matcher.matches()) {
            zipCode = Transfer.trim(matcher.group(1));
            if (ZIPCODE_DEFINES_NOTHING.equals(zipCode)) {
                zipCode = null;
            }
            this.comment = Transfer.trim(matcher.group(2));
            if (zipCode != null && this.comment == null) {
                this.comment = zipCode;
                zipCode = null;
            }
            street = Transfer.trim(matcher.group(3));
            if (street != null && STREET_DEFINES_CENTER_SYMBOL.equals(street))
                street = STREET_DEFINES_CENTER_NAME;
            this.type = Transfer.trim(matcher.group(4));
        }
    }

    public long getAltitude() {
        return altitude;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return comment;
    }

    public String getStreet() {
        return street;
    }

    public String getType() {
        return type;
    }


    public BcrPosition asMTPPosition() {
        return this;
    }

    public GoPalPosition asGoPalRoutePosition() {
        return new GoPalPosition(getX(), getY(), null, null, getZipCode(), getCity(), null, getStreet(), null, null);
    }

    public TourPosition asTourPosition() {
        return new TourPosition(getX(), getY(), getZipCode(), getCity(), getStreet(), null, null, false, new HashMap<String, String>());
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BcrPosition that = (BcrPosition) o;

        return altitude == that.altitude &&
                !(x != null ? !x.equals(that.x) : that.x != null) &&
                !(y != null ? !y.equals(that.y) : that.y != null) &&
                !(comment != null ? !comment.equals(that.comment) : that.comment != null) &&
                !(street != null ? !street.equals(that.street) : that.street != null) &&
                !(type != null ? !type.equals(that.type) : that.type != null) &&
                !(zipCode != null ? !zipCode.equals(that.zipCode) : that.zipCode != null);
    }

    public int hashCode() {
        int result;
        result = (x != null ? x.hashCode() : 0);
        result = 31 * result + (y != null ? y.hashCode() : 0);
        result = 31 * result + (int) (altitude ^ (altitude >>> 32));
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (street != null ? street.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
