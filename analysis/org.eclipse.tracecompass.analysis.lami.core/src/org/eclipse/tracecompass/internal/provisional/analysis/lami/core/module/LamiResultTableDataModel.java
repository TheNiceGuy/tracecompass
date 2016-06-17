/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import java.util.stream.Stream;

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
import org.eclipse.tracecompass.internal.tmf.chart.core.module.IDataSource;

public class LamiResultTableDataModel extends AbstractDataModel {

    LamiResultTable fResultTable;

    private final class LamiDataNumberSource implements IDataSource {

        LamiTableEntryAspect fAspect;

        public LamiDataNumberSource(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public @Nullable Stream<?> getStream() {
            return fResultTable.getEntries().stream()
                    .map(entry -> fAspect.resolveNumber(entry));
        }
    }

    private final class LamiDataStringSource implements IDataSource {

        LamiTableEntryAspect fAspect;

        public LamiDataStringSource(LamiTableEntryAspect aspect) {
            fAspect = aspect;
        }

        @Override
        public @Nullable Stream<?> getStream() {
            return fResultTable.getEntries().stream()
                    .map(entry -> fAspect.resolveString(entry));
        }
    }

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
