/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.TableEntry;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.AbstractData;
import org.eclipse.tracecompass.internal.tmf.chart.core.type.DataInteger;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;

/**
 * Aspect for timestamps
 *
 * @author Alexandre Montplaisir
 */
public class TimestampAspect extends AbstractAspect {

    /**
     * Constructor
     *
     * @param timestampName
     *            Name of the timestamp
     * @param colIndex
     *            Column index
     */
    public TimestampAspect(String timestampName, int colIndex) {
        super(timestampName, null, colIndex);
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isTimeStamp() {
        return true;
    }

    @Override
    public @Nullable String resolveString(TableEntry entry) {
        AbstractData data = entry.getValue(getColumnIndex());
        if (data instanceof DataInteger) {
            DataInteger range = (DataInteger) data;
            return TmfTimestampFormat.getDefaulTimeFormat().format(range.getValue());
        }
        return data.toString();
    }

    @Override
    public @Nullable Number resolveNumber(TableEntry entry) {
        AbstractData data = entry.getValue(getColumnIndex());
        if (data instanceof DataInteger) {
            DataInteger range = (DataInteger) data;
            return Long.valueOf(range.getValue());
        }
        return null;
    }

/*TODO    @Override
    public Comparator<LamiTableEntry> getComparator() {
        return (o1, o2) -> {
            Number d1 = resolveNumber(o1);
            Number d2 = resolveNumber(o2);

            if (d1 == null && d2 == null) {
                return 0;
            }
            if (d1 == null) {
                return 1;
            }

            if (d2 == null) {
                return -1;
            }

            return Long.compare(d1.longValue(), d2.longValue());
        };
    }*/
}
