/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tmf.chart.core.aspect.AbstractAspect;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.AbstractDataModel;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataDescriptor;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries;
import org.eclipse.tracecompass.internal.tmf.chart.core.module.DataSeries.ChartType;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This dialog is used to configure series before building a chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartMakerDialog extends SelectionDialog {

    private Composite fComposite;

    private Combo fComboDataModel;

    private Combo fComboChartType;

    private List fSelectionX;

    private CheckboxTableViewer fSelectionY;

    private Button fXLogscale;

    private Button fYLogscale;

    private AbstractAspect fAspectFilter;

    private int fDataModelIndex;

    private @Nullable DataSeries fDataSeries;

    private class ChartTypeSelectedEvent extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent event) {
            enableButton();
        }
    }

    private class DataModelSelectedEvent extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent event) {
            fDataModelIndex = fComboDataModel.getSelectionIndex();
            fAspectFilter = null;

            populateX();
            populateY();

            enableButton();
        }
    }

    private class ListSelectionEvent implements SelectionListener {
        @Override
        public void widgetSelected(SelectionEvent e) {
            enableButton();
            enableXLog();
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
            enableButton();
            enableXLog();
        }
    }

    private class CheckBoxSelectedEvent implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {
            if(fSelectionY.getCheckedElements().length == 0) {
                fAspectFilter = null;
            } else if(fSelectionY.getCheckedElements().length == 1) {
                if(!event.getChecked()) {
                    enableYLog();
                    return;
                }

                for(DataDescriptor descriptor : AbstractDataModel.getInstances().get(fDataModelIndex).getDataDescriptors()) {
                    if(descriptor.getAspect().getLabel().equals(event.getElement())) {
                        fAspectFilter = descriptor.getAspect();
                        break;
                    }
                }
            }

            enableButton();
            enableYLog();
            populateY();
        }
    }

    private class ResizeEvent implements Listener {
        @Override
        public void handleEvent(Event event) {
            Rectangle rect = getShell().getClientArea();
            Point size = fComposite.computeSize(rect.width, SWT.DEFAULT);
            fComposite.setSize(size);
        }
    }

    /**
     * Constructor.
     *
     * @param parent parent shell
     */
    public ChartMakerDialog(Shell parent) {
        super(parent);

        fComboDataModel = new Combo (parent, SWT.READ_ONLY);
        fComboChartType = new Combo (parent, SWT.READ_ONLY);
        fSelectionX = new List(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fSelectionY = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
        fAspectFilter = null;
        fXLogscale = new Button(parent, SWT.CHECK);
        fYLogscale = new Button(parent, SWT.CHECK);

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    public Composite createDialogArea(Composite parent) {
        fComposite = (Composite) super.createDialogArea(parent);

        /*
         * Layouts
         */
        GridLayout baseLayout = new GridLayout();
        baseLayout.numColumns = 1;
        baseLayout.marginHeight = 5;
        baseLayout.marginWidth = 5;

        GridData baseSelectorGridData = new GridData();
        baseSelectorGridData.horizontalAlignment = SWT.FILL;
        baseSelectorGridData.grabExcessHorizontalSpace = true;

        GridData baseCreatorGridData = new GridData();
        baseCreatorGridData.horizontalAlignment = SWT.FILL;
        baseCreatorGridData.verticalAlignment = SWT.FILL;
        baseCreatorGridData.grabExcessHorizontalSpace = true;
        baseCreatorGridData.grabExcessVerticalSpace = true;

        GridData baseOptionsGridData = new GridData();
        baseOptionsGridData.horizontalAlignment = SWT.FILL;
        baseOptionsGridData.grabExcessHorizontalSpace = true;

        GridLayout creatorLayout = new GridLayout();
        creatorLayout.numColumns = 2;

        GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 1;

        fComposite.setLayout(baseLayout);

        /*
         * Groups
         */
        GridLayout selectorLayout = new GridLayout();
        selectorLayout.numColumns = 2;
        Composite selector = new Composite(fComposite, SWT.FILL);
        selector.setLayout(selectorLayout);
        selector.setLayoutData(baseSelectorGridData);

        Group creatorGroup = new Group(fComposite, SWT.BORDER);
        creatorGroup.setText(Messages.ChartMakerDialog_SeriesCreator);
        creatorGroup.setLayout(creatorLayout);
        creatorGroup.setLayoutData(baseCreatorGridData);

        Group optionsGroup = new Group(fComposite, SWT.BORDER);
        optionsGroup.setText(Messages.ChartMakerDialog_Options);
        optionsGroup.setLayout(optionsLayout);
        optionsGroup.setLayoutData(baseOptionsGridData);

        /*
         * Chart type selector
         */
        GridData selectorComboGridData = new GridData(SWT.FILL);
        selectorComboGridData.horizontalAlignment = SWT.FILL;
        selectorComboGridData.grabExcessHorizontalSpace = true;

        Label typeLabel = new Label(selector, SWT.NONE);
        typeLabel.setText(Messages.ChartMakerDialog_ChartType);

        fComboChartType.setParent(selector);
        fComboChartType.setLayoutData(selectorComboGridData);
        fComboChartType.add(DataSeries.ChartType.BAR_CHART.toString());
        fComboChartType.add(DataSeries.ChartType.SCATTER_CHART.toString());
        fComboChartType.addSelectionListener(new ChartTypeSelectedEvent());

        Label selectorLabel = new Label(selector, SWT.NONE);
        selectorLabel.setText(Messages.ChartMakerDialog_AvailableData);

        fComboDataModel.setParent(selector);
        fComboDataModel.setLayoutData(selectorComboGridData);
        fComboDataModel.addSelectionListener(new DataModelSelectedEvent());
        AbstractDataModel.getInstances().stream()
                .map(model -> model.getName())
                .forEach(fComboDataModel::add);

        /*
         * Series Creator
         */
        GridData creatorLabelGridData = new GridData();
        creatorLabelGridData.horizontalAlignment = SWT.CENTER;
        creatorLabelGridData.verticalAlignment = SWT.BOTTOM;

        GridData creatorBoxGridData = new GridData(SWT.FILL);
        creatorBoxGridData.horizontalAlignment = SWT.FILL;
        creatorBoxGridData.verticalAlignment = SWT.FILL;
        creatorBoxGridData.grabExcessHorizontalSpace = true;
        creatorBoxGridData.grabExcessVerticalSpace = true;

        Label creatorLabelX = new Label(creatorGroup, SWT.NONE);
        creatorLabelX.setText(Messages.ChartMakerDialog_XAxis);
        creatorLabelX.setLayoutData(creatorLabelGridData);

        Label creatorLabelY = new Label(creatorGroup, SWT.NONE);
        creatorLabelY.setText(Messages.ChartMakerDialog_YAxis);
        creatorLabelY.setLayoutData(creatorLabelGridData);

        fSelectionX.setParent(creatorGroup);
        fSelectionX.setLayoutData(creatorBoxGridData);
        fSelectionX.addSelectionListener(new ListSelectionEvent());

        IStructuredContentProvider contentProvider = ArrayContentProvider.getInstance();
        fSelectionY.getControl().setParent(creatorGroup);
        fSelectionY.getControl().setLayoutData(creatorBoxGridData);
        fSelectionY.setContentProvider(contentProvider);
        fSelectionY.addCheckStateListener(new CheckBoxSelectedEvent());

        /*
         * Options
         */
        fXLogscale.setParent(optionsGroup);
        fXLogscale.setText(Messages.ChartMakerDialog_LogScaleX);

        fYLogscale.setParent(optionsGroup);
        fYLogscale.setText(Messages.ChartMakerDialog_LogScaleY);

        this.getShell().addListener(SWT.Resize, new ResizeEvent());


        return fComposite;
    }

    @Override
    public void create() {
        super.create();

        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    public void okPressed() {
        java.util.List<DataDescriptor> descriptors = AbstractDataModel.getInstances().get(fDataModelIndex).getDataDescriptors();

        ChartType type = ChartType.resolveName(fComboChartType.getText());
        if(type == null) {
            return;
        }

        DataDescriptor xAxis = descriptors.get(fSelectionX.getSelectionIndex());
        Collection<DataDescriptor> yAxis = new ArrayList<>();

        for(DataDescriptor descriptor : descriptors) {
            for(Object entry : fSelectionY.getCheckedElements()) {
                if(descriptor.getAspect().getLabel().equals(entry.toString())) {
                    yAxis.add(descriptor);
                }
            }
        }

        fDataSeries = new DataSeries(xAxis, yAxis,
                fXLogscale.getSelection(),
                fYLogscale.getSelection(),
                type);

        super.okPressed();
    }

    /**
     * @return selected data series
     */
    public @Nullable DataSeries getDataSeries() {
        return fDataSeries;
    }

    private void enableButton() {
        if(fComboDataModel.getSelectionIndex() == -1 ||
           fComboChartType.getSelectionIndex() == -1 ||
           fSelectionX.getSelectionIndex() == -1 ||
           fSelectionY.getCheckedElements().length == 0)
        {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            return;
        }

        getButton(IDialogConstants.OK_ID).setEnabled(true);
    }

    private void enableXLog() {
        int index = fSelectionX.getSelectionIndex();

        if(AbstractDataModel.getInstances().get(fDataModelIndex).getDataDescriptors().get(index).getAspect().isContinuous()) {
            fXLogscale.setEnabled(true);
        } else {
            fXLogscale.setEnabled(false);
            fXLogscale.setSelection(false);
        }
    }

    private void enableYLog() {
        if(fAspectFilter != null && fAspectFilter.isContinuous()) {
            fYLogscale.setEnabled(true);
        } else {
            fYLogscale.setEnabled(false);
            fYLogscale.setSelection(false);
        }
    }

    private void populateX() {
        fSelectionX.removeAll();
        AbstractDataModel.getInstances().get(fDataModelIndex).getDataDescriptors().stream()
                .map(descriptor -> descriptor.getAspect().getLabel())
                .forEach(fSelectionX::add);
    }

    private void populateY() {
        java.util.List<String> input = new ArrayList<>();

        if(fAspectFilter == null) {
            input = AbstractDataModel.getInstances().get(fDataModelIndex).getDataDescriptors().stream()
                    .map(descriptor -> descriptor.getAspect().getLabel())
                    .collect(Collectors.toList());
        } else {
            input = AbstractDataModel.getInstances().get(fDataModelIndex).getDataDescriptors().stream()
                    .filter(descriptor -> descriptor.getAspect().getClass() == fAspectFilter.getClass())
                    .map(descriptor -> descriptor.getAspect().getLabel())
                    .collect(Collectors.toList());
        }

        fSelectionY.setInput(input);
    }
}
