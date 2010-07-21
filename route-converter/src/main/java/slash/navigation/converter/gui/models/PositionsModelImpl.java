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

package slash.navigation.converter.gui.models;

import slash.common.io.*;
import slash.navigation.base.BaseNavigationFormat;
import slash.navigation.base.BaseNavigationPosition;
import slash.navigation.base.BaseRoute;
import slash.navigation.base.NavigationFormats;

import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Implements the {@link PositionsModel} for the positions of a {@link BaseRoute}.
 *
 * @author Christian Pesch
 */

public class PositionsModelImpl extends AbstractTableModel implements PositionsModel {
    private static final DateFormat TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    private BaseRoute<BaseNavigationPosition, BaseNavigationFormat> route;

    public BaseRoute<BaseNavigationPosition, BaseNavigationFormat> getRoute() {
        return route;
    }

    public void setRoute(BaseRoute<BaseNavigationPosition, BaseNavigationFormat> route) {
        this.route = route;
        fireTableDataChanged();
    }

    public int getRowCount() {
        return getRoute() != null ? getRoute().getPositionCount() : 0;
    }

    public int getColumnCount() {
        throw new IllegalArgumentException("This is determined by the PositionsTableColumnModel");
    }

    private String formatElevation(Double elevation) {
        return elevation != null ? Math.round(elevation) + " m" : "";
    }

    private String formatSpeed(Double speed) {
        if (speed == null || speed == 0.0)
            return "";
        String speedStr;
        if (Math.abs(speed) < 10.0)
            speedStr = Double.toString(Transfer.roundFraction(speed, 1));
        else
            speedStr = Long.toString(Math.round(speed));
        return speedStr + " Km/h";
    }

    private String formatLongitudeOrLatitude(Double longitudeOrLatitude) {
        if (longitudeOrLatitude == null)
            return "";
        String result = Double.toString(longitudeOrLatitude) + " ";
        if (Math.abs(longitudeOrLatitude) < 10.0)
            result = " " + result;
        if (Math.abs(longitudeOrLatitude) < 100.0)
            result = " " + result;
        if (result.length() > 12)
            result = result.substring(0, 12 - 1);
        return result;
    }

    private String formatDistance(double distance) {
        if (distance <= 0.0)
            return "";
        if (Math.abs(distance) < 10000.0)
            return Math.round(distance) + " m";
        if (Math.abs(distance) < 200000.0)
            return Transfer.roundFraction(distance / 1000.0, 1) + " Km";
        return Transfer.roundFraction(distance / 1000.0, 0) + " Km";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        BaseNavigationPosition position = getPosition(rowIndex);
        switch (columnIndex) {
            case PositionColumns.DESCRIPTION_COLUMN_INDEX:
                return position.getComment();
            case PositionColumns.TIME_COLUMN_INDEX:
                CompactCalendar time = position.getTime();
                return time != null ? TIME_FORMAT.format(time.getTime()) : "";
            case PositionColumns.LONGITUDE_COLUMN_INDEX:
                return formatLongitudeOrLatitude(position.getLongitude());
            case PositionColumns.LATITUDE_COLUMN_INDEX:
                return formatLongitudeOrLatitude(position.getLatitude());
            case PositionColumns.ELEVATION_COLUMN_INDEX:
                return formatElevation(position.getElevation());
            case PositionColumns.SPEED_COLUMN_INDEX:
                return formatSpeed(position.getSpeed());
            case PositionColumns.DISTANCE_COLUMN_INDEX:
                return formatDistance(getRoute().getDistance(0, rowIndex));
            case PositionColumns.ELEVATION_ASCEND_COLUMN_INDEX:
                return formatElevation(getRoute().getElevationAscend(0, rowIndex));
            case PositionColumns.ELEVATION_DESCEND_COLUMN_INDEX:
                return formatElevation(getRoute().getElevationDescend(0, rowIndex));
            default:
                throw new IllegalArgumentException("Row " + rowIndex + ", column " + columnIndex + " does not exist");
        }
    }

    public BaseNavigationPosition getPredecessor(BaseNavigationPosition position) {
        return getRoute().getPredecessor(position);
    }

    public BaseNavigationPosition getPosition(int rowIndex) {
        return getRoute().getPosition(rowIndex);
    }

    public int getIndex(BaseNavigationPosition position) {
        return getRoute().getIndex(position);
    }

    public List<BaseNavigationPosition> getPositions(int[] rowIndices) {
        List<BaseNavigationPosition> result = new ArrayList<BaseNavigationPosition>(rowIndices.length);
        for (int rowIndex : rowIndices)
            result.add(getPosition(rowIndex));
        return result;
    }

    public List<BaseNavigationPosition> getPositions(int from, int to) {
        List<BaseNavigationPosition> result = new ArrayList<BaseNavigationPosition>(to - from);
        for (int i = from; i < to; i++)
            result.add(getPosition(i));
        return result;
    }

    public int[] getPositionsWithinDistanceToPredecessor(double distance) {
        return getRoute().getPositionsWithinDistanceToPredecessor(distance);
    }

    public int[] getInsignificantPositions(double threshold) {
        return getRoute().getInsignificantPositions(threshold);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case PositionColumns.DESCRIPTION_COLUMN_INDEX:
            case PositionColumns.TIME_COLUMN_INDEX:
            case PositionColumns.LONGITUDE_COLUMN_INDEX:
            case PositionColumns.LATITUDE_COLUMN_INDEX:
            case PositionColumns.ELEVATION_COLUMN_INDEX:
            case PositionColumns.SPEED_COLUMN_INDEX:
                return true;
            default:
                return false;
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex == getRowCount())
            return;

        BaseNavigationPosition position = getPosition(rowIndex);
        String value = Transfer.trim(aValue.toString());
        switch (columnIndex) {
            case PositionColumns.DESCRIPTION_COLUMN_INDEX:
                position.setComment(value);
                break;
            case PositionColumns.TIME_COLUMN_INDEX:
                try {
                    Date date = TIME_FORMAT.parse(value);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    position.setTime(CompactCalendar.fromCalendar(calendar));
                }
                catch (ParseException e) {
                    // intentionally left empty
                }
                break;
            case PositionColumns.LONGITUDE_COLUMN_INDEX:
                try {
                    position.setLongitude(Transfer.parseDouble(value));
                }
                catch (NumberFormatException e) {
                    // intentionally left empty
                }
                break;
            case PositionColumns.LATITUDE_COLUMN_INDEX:
                try {
                    position.setLatitude(Transfer.parseDouble(value));
                } catch (NumberFormatException e) {
                    // intentionally left empty
                }
                break;
            case PositionColumns.ELEVATION_COLUMN_INDEX:
                try {
                    if (value != null)
                        value = value.replaceAll("m", "");
                    position.setElevation(Transfer.parseDouble(value));
                } catch (NumberFormatException e) {
                    // intentionally left empty
                }
                break;
            case PositionColumns.SPEED_COLUMN_INDEX:
                try {
                    if (value != null)
                        value = value.replaceAll("Km/h", "");
                    position.setSpeed(Transfer.parseDouble(value));
                } catch (NumberFormatException e) {
                    // intentionally left empty
                }
                break;
            default:
                throw new IllegalArgumentException("Row " + rowIndex + ", column " + columnIndex + " does not exist");
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }

    public void add(int row, Double longitude, Double latitude, Double elevation, Double speed, CompactCalendar time, String comment) {
        BaseNavigationPosition position = getRoute().createPosition(longitude, latitude, elevation, speed, time, comment);
        add(row, Arrays.asList(position));
    }

    public List<BaseNavigationPosition> createPositions(BaseRoute<BaseNavigationPosition, BaseNavigationFormat> route) throws IOException {
        BaseNavigationFormat targetFormat = getRoute().getFormat();
        List<BaseNavigationPosition> positions = new ArrayList<BaseNavigationPosition>();
        for (BaseNavigationPosition sourcePosition : route.getPositions()) {
            BaseNavigationPosition targetPosition = NavigationFormats.asFormat(sourcePosition, targetFormat);
            positions.add(targetPosition);
        }
        return positions;
    }

    public void add(int row, BaseRoute<BaseNavigationPosition, BaseNavigationFormat> route) throws IOException {
        List<BaseNavigationPosition> positions = createPositions(route);
        add(row, positions);
    }

    public void add(int row, List<BaseNavigationPosition> positions) {
        for (int i = positions.size() - 1; i >= 0; i--) {
            BaseNavigationPosition position = positions.get(i);
            getRoute().add(row, position);
        }
        fireTableRowsInserted(row, row - 1 + positions.size());
    }

    public int[] createRowIndices(int from, int to) {
        int[] rows = new int[to - from];
        int count = 0;
        for (int i = to - 1; i >= from; i--)
            rows[count++] = i;
        return rows;
    }

    public void remove(int from, int to) {
        remove(createRowIndices(from, to));
    }

    public void remove(int[] rows) {
        remove(rows, true);
    }

    public void remove(int[] rows, final boolean fireEvent) {
        new ContinousRange(rows, new RangeOperation() {
            public void performOnIndex(int index) {
                getRoute().remove(index);
            }
            public void performOnRange(int firstIndex, int lastIndex) {
                if (fireEvent)
                    fireTableRowsDeleted(firstIndex, lastIndex);
            }
        }).performMonotonicallyDecreasing();
    }

    public void revert() {
        getRoute().revert();
        // since fireTableDataChanged(); is ignored in FormatAndRoutesModel#setModified(true) logic
        fireTableRowsUpdated(-1, -1);
    }

    public void top(int[] rows) {
        Arrays.sort(rows);

        for (int i = 0; i < rows.length; i++) {
            getRoute().top(rows[i], i);
        }
        fireTableRowsUpdated(0, rows[rows.length - 1]);
    }

    public void topDown(int[] rows) {
        int[] reverted = Range.revert(rows);

        for (int i = 0; i < reverted.length; i++) {
            getRoute().down(reverted.length - i - 1, reverted[i]);
        }
        fireTableRowsUpdated(0, reverted[0]);
    }

    public void up(int[] rows) {
        Arrays.sort(rows);

        for (int row : rows) {
            getRoute().up(row, row - 1);
            fireTableRowsUpdated(row - 1, row);
        }
    }

    public void down(int[] rows) {
        int[] reverted = Range.revert(rows);

        for (int row : reverted) {
            getRoute().down(row, row + 1);
            fireTableRowsUpdated(row, row + 1);
        }
    }

    public void bottom(int[] rows) {
        int[] reverted = Range.revert(rows);

        for (int i = 0; i < reverted.length; i++) {
            getRoute().bottom(reverted[i], i);
            fireTableRowsUpdated(reverted[i], getRowCount() - 1 - i);
        }
    }

    public void bottomUp(int[] rows) {
        Arrays.sort(rows);

        for (int i = 0; i < rows.length; i++) {
            getRoute().up(getRowCount() - rows.length + i, rows[i]);
        }
        fireTableRowsUpdated(rows[0], getRowCount() - 1);
    }
}