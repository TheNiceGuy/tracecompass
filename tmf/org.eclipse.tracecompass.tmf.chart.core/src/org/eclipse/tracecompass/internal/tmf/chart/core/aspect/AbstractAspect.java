/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.aspect;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.TableEntry;


public abstract class AbstractAspect {

    private final String fName;
    private final @Nullable String fUnits;
    private final int fColIndex;

    /**
     * Constructor
     *
     * @param name
     *            Aspect name, will be used as column name in the UI
     * @param units
     *            The units of the value in this column
     * @param colIndex
     *            The column index
     */
    protected AbstractAspect(String name, @Nullable String units, int colIndex) {
        fUnits = units;
        fName = name;
        fColIndex = colIndex;
    }

    /**
     * Get the label of this aspect.
     *
     * The label is composed of the name followed by the units in parentheses.
     *
     * @return The label
     */
    public String getLabel() {
        if (getUnits() == null) {
            return getName();
        }
        return (getName() + " (" + getUnits() + ')'); //$NON-NLS-1$
    }

    /**
     * Get the name of this aspect
     *
     * @return The name
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the units of this aspect.
     *
     * @return The units
     */
    public @Nullable String getUnits() {
        return fUnits;
    }

    /**
     * Get the column index of this aspect.
     *
     * @return The column index
     */
    public int getColumnIndex() {
        return fColIndex;
    }

    /**
     * Indicate if this aspect is numerical or not. This is used, among other
     * things, to align the text in the table cells.
     *
     * @return If this aspect is numerical or not
     */
    public abstract boolean isContinuous();


    /**
     * Indicate if this aspect represent timestamp or not. This can be used in chart
     * for axis labeling etc.
     * @return  If this aspect represent a timestamp or not
     */
    public abstract boolean isTimeStamp();

    /**
     * Indicate if this aspect represent a time duration or not. This can be used in
     * chart for axis labeling etc.
     * @return  If this aspect represent a time duration or not
     */
    public boolean isTimeDuration() {
        return false;
    }

    /**
     * Resolve this aspect for the given entry.
     *
     * @param entry
     *            The table row
     * @return The string to display for the given cell
     */
    public abstract @Nullable String resolveString(TableEntry entry);

    /**
     * Resolve this aspect double representation for the given entry
     *
     * Returned value does not matter if isNumerical() is false.
     *
     * @param entry
     *            The table row
     * @return The double value for the given cell
     */
    public abstract @Nullable Number resolveNumber(TableEntry entry);

    /**
     * Get the comparator that should be used to compare this entry (or table
     * row) with the other rows in the table. This will be passed on to the
     * table's content provider.
     *
     * @return The entry comparator
     */
    public abstract @NonNull Comparator<@NonNull TableEntry> getComparator();

    /**
     * Check if an aspect have the same properties.
     *
     * FIXME:Might want to compare the units if necessary.
     *
     * @param aspect
     *            The aspect to compare to
     * @return If all aspect's properties are equal
     */
    public boolean arePropertiesEqual(AbstractAspect aspect) {
        boolean timestamp = (this.isTimeStamp() == aspect.isTimeStamp());
        boolean numerical = (this.isContinuous() == aspect.isContinuous());
        return (timestamp && numerical);
    }
}
