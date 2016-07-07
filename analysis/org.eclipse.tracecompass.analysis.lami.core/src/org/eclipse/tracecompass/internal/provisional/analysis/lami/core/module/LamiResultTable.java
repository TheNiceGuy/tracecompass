/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartDurationAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartNumberAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartStringAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartTimestampAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.tracecompass.tmf.chart.core.source.AbstractLongSource;
import org.eclipse.tracecompass.tmf.chart.core.source.IDataSource;
import org.eclipse.tracecompass.tmf.chart.core.source.IStringSource;

import com.google.common.collect.ImmutableList;

/**
 * Class holding the results contained in one table outputted by a LAMI analysis.
 *
 * @author Alexandre Montplaisir
 */
public class LamiResultTable implements IDataChartModel {

    private final LamiTimeRange fTimeRange;
    private final LamiTableClass fTableClass;
    private final List<LamiTableEntry> fEntries;
    private List<DataDescriptor> fDescriptors;

    private final class LamiDataNumberSource extends AbstractLongSource {
        LamiTableEntryAspect fAspect;

        public LamiDataNumberSource(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public @NonNull Stream<@Nullable Long> getStream() {
            Stream<@Nullable Long> stream = getEntries().stream()
                    .map(entry -> checkNotNull(fAspect.resolveNumber(entry)).longValue());
            return checkNotNull(stream);
        }
    }

    private final class LamiDataStringSource implements IStringSource {
        LamiTableEntryAspect fAspect;

        public LamiDataStringSource(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public @NonNull Stream<@Nullable String> getStream() {
            Stream<@Nullable String> stream = getEntries().stream()
                    .map(entry -> fAspect.resolveString(entry));
            return checkNotNull(stream);
        }
    }

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
    public LamiResultTable(LamiTimeRange timeRange, LamiTableClass tableClass,
            Iterable<LamiTableEntry> entries) {
        fTimeRange = timeRange;
        fTableClass = tableClass;
        fEntries = ImmutableList.copyOf(entries);
        fDescriptors = new ArrayList<>();

        for(LamiTableEntryAspect aspect : getTableClass().getAspects()) {
            if(aspect instanceof LamiEmptyAspect) {
                continue;
            }

            IDataChartAspect newAspect;
            IDataSource newSource;

            if(aspect.isContinuous()) {
                if(aspect.isTimeStamp()) {
                    /* Create descriptors for timestamps */
                    newAspect = new GenericDataChartTimestampAspect(aspect.getName());
                    newSource = new LamiDataNumberSource(aspect);
                } else if(aspect.isTimeDuration()) {
                    /* Create descriptors for time durations */
                    newAspect = new GenericDataChartDurationAspect(aspect.getName());
                    newSource = new LamiDataNumberSource(aspect);
                } else {
                    /* Create descriptors for general numbers */
                    newAspect = new GenericDataChartNumberAspect(aspect.getLabel());
                    newSource = new LamiDataNumberSource(aspect);
                }
            } else {
                /* Create descriptors for general strings */
                newAspect = new GenericDataChartStringAspect(aspect.getLabel());
                newSource = new LamiDataStringSource(aspect);
            }

            fDescriptors.add(new DataDescriptor(newAspect, newSource));
        }
    }

    /**
     * Get the time range of this table.
     *
     * @return The time range
     */
    public LamiTimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Get the class of this table.
     *
     * @return The table class
     */
    public LamiTableClass getTableClass() {
        return fTableClass;
    }

    /**
     * Get the list of entries contained in this table.
     *
     * @return The table entries
     */
    public List<LamiTableEntry> getEntries() {
        return fEntries;
    }

    @Override
    public @NonNull String getTitle() {
        return getTableClass().getTableTitle();
    }

    @Override
    public List<DataDescriptor> getDataDescriptors() {
        return fDescriptors;
    }
}
