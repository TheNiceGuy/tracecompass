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

/**
 * Base class for LAMI table aspects.
 *
 * @author Alexandre Montplaisir
 */
public class GenericAspect extends AbstractAspect {

    private final boolean fIsContinuous;
    private final boolean fIsTimeStamp;

    /**
     * Constructor
     *
     * @param aspectName
     *            Name of the aspect
     * @param units
     *            The units of this column
     * @param colIndex
     *            Index of this column
     * @param isContinuous
     *            If the contents of this column are numbers or not
     * @param isTimeStamp
     *            If the contents of this column are numerical timestamp or not
     */
    public GenericAspect(String aspectName, @Nullable String units, int colIndex, boolean isContinuous, boolean isTimeStamp) {
        super(aspectName, units, colIndex);
        fIsContinuous = isContinuous;
        fIsTimeStamp = isTimeStamp;
    }

    @Override
    public boolean isContinuous() {
        return fIsContinuous;
    }

    @Override
    public boolean isTimeStamp() {
        return fIsTimeStamp;
    }

    @Override
    public @Nullable String resolveString(TableEntry entry) {
        return entry.getValue(getColumnIndex()).toString();
    }

    @Override
    public @Nullable Number resolveNumber(TableEntry entry) {
        if (fIsContinuous) {
            try {
                if (entry.getValue(getColumnIndex()).toString() != null) {
                    return Double.parseDouble(entry.getValue(getColumnIndex()).toString());
                }
            } catch (NumberFormatException e) {
                // Fallback to default value below
            }
        }
        return null;
    }

    @Override
    public @NonNull Comparator<@NonNull TableEntry> getComparator() {
        if (isContinuous()) {
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

                return Double.compare(d1.doubleValue(), d2.doubleValue());
            };
        }

        /* Use regular string comparison */
        return (o1, o2) -> {
            String s1 = resolveString(o1);
            String s2 = resolveString(o2);

            if (s1 == null && s2 == null) {
                return 0;
            }
            if (s1 == null) {
                return 1;
            }

            if (s2 == null) {
                return -1;
            }

            return s1.compareTo(s2);
        };
    }
}
