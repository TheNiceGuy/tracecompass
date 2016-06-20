/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.DurationAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.IntAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.StringAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.IDataSource;

public class SystemCallLatencyDataModel extends AbstractDataModel {

    Map<String, SegmentStoreStatistics> fSegmentStats;

    private final class NameSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStats.keySet().stream();
        }
    }

    private final class MinimumSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStats.values().stream()
                    .map(segment -> segment.getMinSegment().getLength())
                    .map(num -> num.doubleValue());
        }
    }

    private final class MaximumSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStats.values().stream()
                    .map(segment -> segment.getMaxSegment().getLength())
                    .map(num -> num.doubleValue());
        }
    }

    private final class AverageSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStats.values().stream()
                    .map(segment -> segment.getAverage());
        }
    }

    private final class StandardDeviationSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStats.values().stream()
                    .map(segment -> segment.getStdDev());
        }
    }

    private final class CountSource implements IDataSource {
        @Override
        public @Nullable Stream<?> getStream() {
            return fSegmentStats.values().stream()
                    .map(segment -> segment.getNbSegments());
        }
    }

    public SystemCallLatencyDataModel(String name, @Nullable Map<String, SegmentStoreStatistics> segmentStats) {
        super(name);

        if(segmentStats == null) {
            fSegmentStats = new HashMap<>();
            return;
        }

        fSegmentStats = segmentStats;

        getDataDescriptors().add(new DataDescriptor(new StringAspect("System Call"), new NameSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect("Minimum"), new MinimumSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect("Maximum"), new MaximumSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect("Average"), new AverageSource()));
        getDataDescriptors().add(new DataDescriptor(new DurationAspect("Standard Deviation"), new StandardDeviationSource()));
        getDataDescriptors().add(new DataDescriptor(new IntAspect("Count"), new CountSource()));
    }
}
