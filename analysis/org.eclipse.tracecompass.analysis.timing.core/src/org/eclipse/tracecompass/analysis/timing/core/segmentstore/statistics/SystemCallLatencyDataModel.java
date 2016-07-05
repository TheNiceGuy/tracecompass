/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.allowNonNullContents;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.DurationAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.IntAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.StringAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.INumericalSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IStringSource;

/**
 * This is a simple {@link AbstractDataModel} implementation for the system call
 * latency statistics analysis.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class SystemCallLatencyDataModel extends AbstractDataModel {

    Map<String, SegmentStoreStatistics> fSegmentStats;

    private final class NameSource implements IStringSource {
        @Override
        public @NonNull Stream<@Nullable String> getStreamString() {
            Stream<String> stream = fSegmentStats.keySet().stream();
            return allowNonNullContents(stream);
        }
    }

    private final class MinimumSource implements INumericalSource {
        @Override
        public @NonNull Stream<@Nullable Number> getStreamNumber() {
            Stream<@Nullable Number> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getMinSegment().getLength());
            return checkNotNull(stream);
        }
    }

    private final class MaximumSource implements INumericalSource {
        @Override
        public @NonNull Stream<@Nullable Number> getStreamNumber() {
            Stream<@Nullable Number> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getMaxSegment().getLength());
            return checkNotNull(stream);
        }
    }

    private final class AverageSource implements INumericalSource {
        @Override
        public @NonNull Stream<@Nullable Number> getStreamNumber() {
            Stream<@Nullable Number> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getAverage());
            return checkNotNull(stream);
        }
    }

    private final class StandardDeviationSource implements INumericalSource {
        @Override
        public @NonNull Stream<@Nullable Number> getStreamNumber() {
            Stream<@Nullable Number> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getStdDev());
            return checkNotNull(stream);
        }
    }

    private final class CountSource implements INumericalSource {
        @Override
        public @NonNull Stream<@Nullable Number> getStreamNumber() {
            Stream<@Nullable Number> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getNbSegments());
            return checkNotNull(stream);
        }
    }

    /**
     * Constructor.
     *
     * @param name name of the analysis
     * @param segmentStats statistics concerning the analysis
     */
    public SystemCallLatencyDataModel(String name, @Nullable Map<String, SegmentStoreStatistics> segmentStats) {
        super(name);

        if(segmentStats == null) {
            fSegmentStats = new HashMap<>();
            return;
        }

        fSegmentStats = segmentStats;

        getDataDescriptors().add(new DataDescriptor(new StringAspect(Messages.SystemCallLatencyDataModel_SystemCall), new NameSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect(Messages.SystemCallLatencyDataModel_Minimum), new MinimumSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect(Messages.SystemCallLatencyDataModel_Maximum), new MaximumSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect(Messages.SystemCallLatencyDataModel_Average), new AverageSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect(Messages.SystemCallLatencyDataModel_StandardDeviation), new StandardDeviationSource()));
        getDataDescriptors().add(new DataDescriptor(new IntAspect(Messages.SystemCallLatencyDataModel_Count), new CountSource()));
    }
}
