/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.AbstractAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.DurationAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.IntAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.StringAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.TimestampAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.AbstractLongSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IDataSource;
import org.eclipse.tracecompass.internal.tmf.chart.core.source.IStringSource;

/**
 * This is a simple {@link AbstractDataModel} implementation for any LAMI
 * result table.
 *
 * @author Gabriel-Andrew Poll-Guilbert
 */
public class LamiResultTableDataModel extends AbstractDataModel {

    LamiResultTable fResultTable;

    private final class LamiDataNumberSource extends AbstractLongSource {

        /**
         *  FIXME: LAMI can send more than long values.
         */

        LamiTableEntryAspect fAspect;

        public LamiDataNumberSource(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public @NonNull Stream<@Nullable Number> getStreamNumber() {
            Stream<@Nullable Number> stream = fResultTable.getEntries().stream()
                    .map(entry -> fAspect.resolveNumber(entry));
            return checkNotNull(stream);
        }
    }

    private final class LamiDataStringSource implements IStringSource {

        LamiTableEntryAspect fAspect;

        public LamiDataStringSource(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public @NonNull Stream<@Nullable String> getStreamString() {
            Stream<@Nullable String> stream = fResultTable.getEntries().stream()
                    .map(entry -> fAspect.resolveString(entry));
            return checkNotNull(stream);
        }
    }

    /**
     * @param name
     *              Name of the table
     * @param resultTable
     *              Result table from a LAMI analysis
     */
    public LamiResultTableDataModel(String name, LamiResultTable resultTable) {
        super(name);

        fResultTable = resultTable;

        // create descriptors for timestamps
        fResultTable.getTableClass().getAspects().stream()
                .filter(aspect -> aspect.isTimeStamp())
                .forEach(aspect -> {
                    AbstractAspect newAspect = new TimestampAspect(aspect.getName());
                    IDataSource newSource = new LamiDataNumberSource(aspect);
                    getDataDescriptors().add(new DataDescriptor(newAspect, newSource));
                });

        // create descriptors for time durations
        fResultTable.getTableClass().getAspects().stream()
                .filter(aspect -> aspect.isTimeDuration())
                .forEach(aspect -> {
                    AbstractAspect newAspect = new DurationAspect(aspect.getName());
                    IDataSource newSource = new LamiDataNumberSource(aspect);
                    getDataDescriptors().add(new DataDescriptor(newAspect, newSource));
                });

        // create descriptors for general numbers
        fResultTable.getTableClass().getAspects().stream()
                .filter(aspect -> aspect.isContinuous() && !aspect.isTimeStamp() && !aspect.isTimeDuration())
                .forEach(aspect -> {
                    AbstractAspect newAspect = new IntAspect(aspect.getLabel());
                    IDataSource newSource = new LamiDataNumberSource(aspect);
                    getDataDescriptors().add(new DataDescriptor(newAspect, newSource));
                });

        // create descriptors for general strings
        fResultTable.getTableClass().getAspects().stream()
                .filter(aspect -> !aspect.isContinuous() && !aspect.isTimeStamp() && !aspect.isTimeDuration())
                .filter(aspect -> !(aspect instanceof LamiEmptyAspect))
                .forEach(aspect -> {
                    AbstractAspect newAspect = new StringAspect(aspect.getLabel());
                    IDataSource newSource = new LamiDataStringSource(aspect);
                    getDataDescriptors().add(new DataDescriptor(newAspect, newSource));
                });
    }
}
