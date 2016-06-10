/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.module;

import java.util.List;

import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataTimeRange;

import com.google.common.collect.ImmutableList;

/**
 * Class holding the results contained in one table outputted by a LAMI analysis.
 *
 * @author Alexandre Montplaisir
 */
public class ResultTable {

    private final DataTimeRange fTimeRange;
    private final TableClass fTableClass;
    private final List<TableEntry> fEntries;

    /**
     * Construct a new table from its components.
     *
     * @param timeRange
     *            The time range represented by this table. Some analyses
     *            separate tables by time range "buckets".
     * @param tableClass
     *            The class of this table
     * @param entries
     *            The list of entries, or rows, contained in this table
     */
    public ResultTable(DataTimeRange timeRange, TableClass tableClass,
            Iterable<TableEntry> entries) {
        fTimeRange = timeRange;
        fTableClass = tableClass;
        fEntries = ImmutableList.copyOf(entries);
    }

    /**
     * Get the time range of this table.
     *
     * @return The time range
     */
    public DataTimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Get the class of this table.
     *
     * @return The table class
     */
    public TableClass getTableClass() {
        return fTableClass;
    }

    /**
     * Get the list of entries contained in this table.
     *
     * @return The table entries
     */
    public List<TableEntry> getEntries() {
        return fEntries;
    }
}
