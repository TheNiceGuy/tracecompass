/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartDurationDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DataChartTimestampDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.resolver.AbstractLongResolver;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * This class provides a data model for a segment store analysis.
 *
 * @param <T>
 *            The type of segment it understands
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 * @since 1.1
 */
public class SegmentStoreDataModel<T extends ISegment> {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final List<IDataChartDescriptor<T, ?>> fDescriptors;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     */
    public SegmentStoreDataModel() {
        fDescriptors = new ArrayList<>();

        String start = checkNotNull(Messages.SegmentStoreDataModel_Start);
        String end = checkNotNull(Messages.SegmentStoreDataModel_End);
        String length = checkNotNull(Messages.SegmentStoreDataModel_Length);

        fDescriptors.add(new DataChartTimestampDescriptor<>(start, new StartResolver(), null));
        fDescriptors.add(new DataChartTimestampDescriptor<>(end, new EndResolver(), null));
        fDescriptors.add(new DataChartDurationDescriptor<>(length, new LengthResolver()));
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns the list of data descriptors of a segment store
     * analysis.
     *
     * @return The list of data descriptors
     */
    public List<@NonNull IDataChartDescriptor<T, ?>> getDataDescriptors() {
        return fDescriptors;
    }

    // ------------------------------------------------------------------------
    // Data resolvers
    // ------------------------------------------------------------------------

    private final class StartResolver extends AbstractLongResolver<T> {
        @Override
        public Function<@NonNull T, @Nullable Long> getMapper() {
            return o -> o.getStart();
        }
    }

    private final class EndResolver extends AbstractLongResolver<T> {
        @Override
        public Function<@NonNull T, @Nullable Long> getMapper() {
            return o -> o.getEnd();
        }

    }

    private final class LengthResolver extends AbstractLongResolver<T> {
        @Override
        public Function<@NonNull T, @Nullable Long> getMapper() {
            return o -> o.getLength();
        }
    }

}
