/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Michael Jeanson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.chart.core.aspect;

/**
 * Aspect for a time range duration
 *
 * @author Michael Jeanson
 */
public class DurationAspect extends AbstractAspect {

    /**
     * Constructor
     *
     * @param colName
     *            Column name
     */
    public DurationAspect(String colName) {
        super(colName, "ns"); //$NON-NLS-1$
    }

    @Override
    public boolean isContinuous() {
        return true;
    }

    @Override
    public boolean isTimeStamp() {
        return false;
    }

    @Override
    public boolean isTimeDuration() {
        return true;
    }

//    @Override
//    public @Nullable String resolveString(LamiTableEntry entry) {
//        LamiData data = entry.getValue(fColIndex);
//        if (data instanceof LamiDuration) {
//            LamiDuration duration = (LamiDuration) data;
//            return String.valueOf(duration.getValue());
//        }
//        return data.toString();
//    }
//
//    @Override
//    public @Nullable Number resolveNumber(@NonNull LamiTableEntry entry) {
//        LamiData data = entry.getValue(fColIndex);
//        if (data instanceof LamiDuration) {
//            LamiDuration range = (LamiDuration) data;
//            return range.getValue();
//        }
//        return null;
//    }
//
//    @Override
//    public @NonNull Comparator<@NonNull LamiTableEntry> getComparator() {
//        return (o1, o2) -> {
//            Number d1 = resolveNumber(o1);
//            Number d2 = resolveNumber(o2);
//
//            if (d1 == null && d2 == null) {
//                return 0;
//            }
//            if (d1 == null) {
//                return 1;
//            }
//
//            if (d2 == null) {
//                return -1;
//            }
//
//            return Long.compare(d1.longValue(), d2.longValue());
//        };
//    }
}
