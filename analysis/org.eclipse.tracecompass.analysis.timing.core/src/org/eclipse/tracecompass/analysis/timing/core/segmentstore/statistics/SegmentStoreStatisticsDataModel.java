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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartDurationAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartNumberAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.GenericDataChartStringAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.tracecompass.tmf.chart.core.source.AbstractDoubleSource;
import org.eclipse.tracecompass.tmf.chart.core.source.AbstractLongSource;
import org.eclipse.tracecompass.tmf.chart.core.source.IStringSource;

/**
 * This is a simple {@link IDataChartModel} implementation for a segment store
 * statistics.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 * @since 1.1.0
 */
public class SegmentStoreStatisticsDataModel implements IDataChartModel {
    /**
     * Name of the segment store statistics
     */
    private String fName;
    /**
     * Map to read data from
     */
    private Map<String, SegmentStoreStatistics> fSegmentStats;
    /**
     * List of data descriptors
     */
    private List<DataDescriptor> fDescriptors;

    private final class NameSource implements IStringSource {
        @Override
        public Stream<@Nullable String> getStream() {
            return checkNotNull(fSegmentStats.keySet().stream().map(obj -> checkNotNull(obj)));
        }
    }

    private final class MinimumSource extends AbstractLongSource {
        @Override
        public Stream<@Nullable Long> getStream() {
            Stream<@Nullable Long> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getMinSegment().getLength());
            return checkNotNull(stream);
        }
    }

    private final class MaximumSource extends AbstractLongSource {
        @Override
        public Stream<@Nullable Long> getStream() {
            Stream<@Nullable Long> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getMaxSegment().getLength());
            return checkNotNull(stream);
        }
    }

    private final class AverageSource extends AbstractDoubleSource {
        @Override
        public Stream<@Nullable Double> getStream() {
            Stream<@Nullable Double> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getAverage());
            return checkNotNull(stream);
        }
    }

    private final class StandardDeviationSource extends AbstractDoubleSource {
        @Override
        public Stream<@Nullable Double> getStream() {
            Stream<@Nullable Double> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getStdDev())
                    .map(num -> {
                        if (Double.isNaN(num)) {
                            return null;
                        }
                        return num;
                    });
            return checkNotNull(stream);
        }
    }

    private final class CountSource extends AbstractLongSource {
        @Override
        public Stream<@Nullable Long> getStream() {
            Stream<@Nullable Long> stream = fSegmentStats.values().stream()
                    .map(segment -> segment.getNbSegments());
            return checkNotNull(stream);
        }
    }

    /**
     * Constructor.
     *
     * @param title
     *            Name of the analysis
     * @param name
     *            Name of a segment
     * @param segmentStats
     *            Statistics concerning the analysis
     */
    public SegmentStoreStatisticsDataModel(String title, String name, Map<String, SegmentStoreStatistics> segmentStats) {
        fName = title;
        fSegmentStats = segmentStats;
        fDescriptors = new ArrayList<>();

        IDataChartAspect aspectName = new GenericDataChartStringAspect(name);
        IDataChartAspect aspectMinimum = new GenericDataChartDurationAspect(checkNotNull(Messages.SegStoreStatisticsDataModel_Minimum));
        IDataChartAspect aspectMaximum = new GenericDataChartDurationAspect(checkNotNull(Messages.SegStoreStatisticsDataModel_Maximum));
        IDataChartAspect aspectAverage = new GenericDataChartDurationAspect(checkNotNull(Messages.SegStoreStatisticsDataModel_Average));
        IDataChartAspect aspectStdDev = new GenericDataChartDurationAspect(checkNotNull(Messages.SegStoreStatisticsDataModel_StandardDeviation));
        IDataChartAspect aspectCount = new GenericDataChartNumberAspect(checkNotNull(Messages.SegStoreStatisticsDataModel_Count));

        fDescriptors.add(new DataDescriptor(aspectName, new NameSource()));
        fDescriptors.add(new DataDescriptor(aspectMinimum, new MinimumSource()));
        fDescriptors.add(new DataDescriptor(aspectMaximum, new MaximumSource()));
        fDescriptors.add(new DataDescriptor(aspectAverage, new AverageSource()));
        fDescriptors.add(new DataDescriptor(aspectStdDev, new StandardDeviationSource()));
        fDescriptors.add(new DataDescriptor(aspectCount, new CountSource()));

        IDataChartModel.getInstances().add(this);
    }

    @Override
    public @NonNull String getTitle() {
        return fName;
    }

    @Override
    public List<DataDescriptor> getDataDescriptors() {
        return fDescriptors;
    }
}
