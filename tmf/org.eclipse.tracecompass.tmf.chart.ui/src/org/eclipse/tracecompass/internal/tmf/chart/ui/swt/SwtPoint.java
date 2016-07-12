/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.ui.swt;

import org.eclipse.jdt.annotation.Nullable;
import org.swtchart.ISeries;

/**
 * This class is used for storing informations about a point inside a SWT
 * series. Rather than using coordinates, it uses a reference to the series
 * itself and an index in order to decrease selection of multiple points that
 * have the same position.
 * <p>
 * The methods {@link #equals(Object)} and {@link #hashCode()} have been
 * overridden in order to allow two different objects that represent the same
 * selection to look like they are the same. It is useful when storing them
 * inside an hash data structure.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SwtPoint {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private ISeries fSeries;
    private int fIndex;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param series
     *            The series that owns the point
     * @param index
     *            The index of the point in the series
     */
    public SwtPoint(ISeries series, int index) {
        fSeries = series;
        fIndex = index;
    }

    /**
     * Copy contructor.
     *
     * @param selection
     *            The selection to copy
     */
    public SwtPoint(SwtPoint selection) {
        fSeries = selection.fSeries;
        fIndex = selection.fIndex;
    }

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj == null ? false : this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return fIndex ^ fSeries.hashCode();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the series who owns the selection.
     *
     * @return The SWT series of the selection
     */
    public ISeries getSeries() {
        return fSeries;
    }

    /**
     * Accessor that returns the index of the selection in the series.
     *
     * @return The index of the selection
     */
    public int getIndex() {
        return fIndex;
    }

    // ------------------------------------------------------------------------
    // Mutators
    // ------------------------------------------------------------------------

    /**
     * Mutator that sets the series of the selection.
     *
     * @param series
     *            The new series of the selection
     */
    public void setSeries(ISeries series) {
        fSeries = series;
    }

    /**
     * Mutator that sets the index of the selection.
     *
     * @param index
     *            The new index of the selection
     */
    public void setIndex(int index) {
        fIndex = index;
    }

}
