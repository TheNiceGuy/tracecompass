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
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.statistics.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartDurationDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartNumericalDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartStringDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractDoubleResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractLongResolver;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.IStringResolver;

/**
 * This class provides a data model for a segment store statistics analysis. The
 * statistics are composed of a map of {@link String} and
 * {@link SegmentStoreStatistics}. We could use an entry set to resolve values
 * inside, but it is simpler and probably faster to directly use keys and keep a
 * reference to the map inside this class.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 * @since 1.1
 */
public class SegmentStoreStatisticsDataModel {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final Map<String, SegmentStoreStatistics> fSegmentStats;
    private final List<IDataChartDescriptor<String, ?>> fDescriptors;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param name
     *            Name of a segment
     * @param segmentStats
     *            Statistics concerning the analysis
     */
    public SegmentStoreStatisticsDataModel(String name, Map<String, SegmentStoreStatistics> segmentStats) {
        fSegmentStats = segmentStats;
        fDescriptors = new ArrayList<>();

        String min = checkNotNull(Messages.SegStoreStatisticsDataModel_Minimum);
        String max = checkNotNull(Messages.SegStoreStatisticsDataModel_Maximum);
        String avg = checkNotNull(Messages.SegStoreStatisticsDataModel_Average);
        String std = checkNotNull(Messages.SegStoreStatisticsDataModel_StandardDeviation);
        String count = checkNotNull(Messages.SegStoreStatisticsDataModel_Count);

        fDescriptors.add(new DataChartStringDescriptor<>(name, new NameResolver()));
        fDescriptors.add(new DataChartDurationDescriptor<>(min, new MinimumResolver()));
        fDescriptors.add(new DataChartDurationDescriptor<>(max, new MaximumResolver()));
        fDescriptors.add(new DataChartDurationDescriptor<>(avg, new AverageResolver()));
        fDescriptors.add(new DataChartDurationDescriptor<>(std, new StandardDeviationResolver()));
        fDescriptors.add(new DataChartNumericalDescriptor<>(count, new CountSource()));
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the list of data descriptors.
     *
     * @return The list of data descriptors
     */
    public List<IDataChartDescriptor<String, ?>> getDataDescriptors() {
        return fDescriptors;
    }

    // ------------------------------------------------------------------------
    // Data resolvers
    // ------------------------------------------------------------------------

    private final class NameResolver implements IStringResolver<String> {
        @Override
        public Function<String, @Nullable String> getMapper() {
            return o -> o;
        }
    }

    private final class MinimumResolver extends AbstractLongResolver<String> {
        @Override
        public Function<String, @Nullable Long> getMapper() {
            return o -> checkNotNull(fSegmentStats.get(o)).getMin();
        }
    }

    private final class MaximumResolver extends AbstractLongResolver<String> {
        @Override
        public Function<String, @Nullable Long> getMapper() {
            return o -> checkNotNull(fSegmentStats.get(o)).getMax();
        }
    }

    private final class AverageResolver extends AbstractDoubleResolver<String> {
        @Override
        public Function<String, @Nullable Double> getMapper() {
            return o -> checkNotNull(fSegmentStats.get(o)).getAverage();
        }
    }

    private final class StandardDeviationResolver extends AbstractDoubleResolver<String> {
        @Override
        public Function<String, @Nullable Double> getMapper() {
            return o -> {
                Double value = checkNotNull(fSegmentStats.get(o)).getStdDev();

                if (Double.isNaN(value)) {
                    return null;
                }

                return value;
            };
        }
    }

    private final class CountSource extends AbstractLongResolver<String> {
        @Override
        public Function<String, @Nullable Long> getMapper() {
            return (o) -> checkNotNull(fSegmentStats.get(o)).getNbSegments();
        }
    }

}
