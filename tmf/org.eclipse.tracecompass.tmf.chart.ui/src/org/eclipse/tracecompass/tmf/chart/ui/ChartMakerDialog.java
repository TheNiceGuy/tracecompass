/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;

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
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartNumericalAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartData;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartModel;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * This dialog is used to configure series before building a chart.
 *
 * TODO: restrict X axis selection, we could create a interface for each chart
 * type to implement
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartMakerDialog extends SelectionDialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Parent composite
     */
    private Composite fComposite;
    /**
     * Combo list for selecting the data model
     */
    private Combo fComboDataModel;
    /**
     * Combo list for selecting the chart type
     */
    private Combo fComboChartType;
    /**
     * List for selecting the X axis
     */
    private List fSelectionX;
    /**
     * Checkbox list for selecting the Y axis
     */
    private CheckboxTableViewer fSelectionY;
    /**
     * Checkbox for indicating whether X axis is logarithmic
     */
    private Button fXLogscale;
    /**
     * Checkbox for indicating whether Y axis is logarithmic
     */
    private Button fYLogscale;
    /**
     * Filter reference to the first selected aspect in the Y list
     */
    private @Nullable IDataChartAspect fAspectFilter;
    /**
     * Index of the currently selected data model
     */
    private int fDataModelIndex = -1;
    /**
     * Data series created after the dialog
     */
    private @Nullable ChartData fDataSeries;
    /**
     * Data model created after the dialog
     */
    private @Nullable ChartModel fChartModel;

    // ------------------------------------------------------------------------
    // Important methods
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent
     *            Parent shell
     */
    public ChartMakerDialog(Shell parent) {
        super(parent);

        fComposite = parent;
        fComboDataModel = new Combo(parent, SWT.READ_ONLY);
        fComboChartType = new Combo(parent, SWT.READ_ONLY);
        fSelectionX = new List(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        fSelectionY = checkNotNull(CheckboxTableViewer.newCheckList(parent, SWT.BORDER));
        fAspectFilter = null;
        fXLogscale = new Button(parent, SWT.CHECK);
        fYLogscale = new Button(parent, SWT.CHECK);

        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    public Composite createDialogArea(@Nullable Composite parent) {
        fComposite = checkNotNull((Composite) super.createDialogArea(parent));

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
        fComboChartType.add(ChartModel.ChartType.BAR_CHART.toString());
        fComboChartType.add(ChartModel.ChartType.SCATTER_CHART.toString());
        fComboChartType.addSelectionListener(new ChartTypeSelectedEvent());

        Label selectorLabel = new Label(selector, SWT.NONE);
        selectorLabel.setText(Messages.ChartMakerDialog_AvailableData);

        fComboDataModel.setParent(selector);
        fComboDataModel.setLayoutData(selectorComboGridData);
        fComboDataModel.addSelectionListener(new DataModelSelectedEvent());
        IDataChartModel.getInstances().stream()
                .map(model -> model.getTitle())
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

    /**
     * @return The configured data series
     */
    public @Nullable ChartData getDataSeries() {
        return fDataSeries;
    }

    /**
     * @return The configured chart model
     */
    public @Nullable ChartModel getChartModel() {
        return fChartModel;
    }

    @Override
    public void create() {
        super.create();

        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    public void okPressed() {
        java.util.List<DataDescriptor> descriptors = getInstance(fDataModelIndex).getDataDescriptors();
        String title = getInstance(fDataModelIndex).getTitle();

        ChartModel.ChartType type = ChartModel.ChartType.resolveChartType(checkNotNull(fComboChartType.getText()));
        if (type == null) {
            return;
        }

        java.util.List<DataDescriptor> xAxis = new ArrayList<>();
        java.util.List<DataDescriptor> yAxis = new ArrayList<>();

        for (DataDescriptor descriptor : descriptors) {
            for (Object entry : fSelectionY.getCheckedElements()) {
                if (descriptor.getAspect().getLabel().equals(entry.toString())) {
                    yAxis.add(descriptor);
                    xAxis.add(descriptors.get(fSelectionX.getSelectionIndex()));
                }
            }
        }

        fDataSeries = new ChartData(xAxis, yAxis);
        fChartModel = new ChartModel(type, title, fXLogscale.getSelection(), fYLogscale.getSelection());

        super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    private void enableButton() {
        if (fComboDataModel.getSelectionIndex() == -1 ||
                fComboChartType.getSelectionIndex() == -1 ||
                fSelectionX.getSelectionIndex() == -1 ||
                fSelectionY.getCheckedElements().length == 0) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            return;
        }

        getButton(IDialogConstants.OK_ID).setEnabled(true);
    }

    private void enableXLog() {
        int index = fSelectionX.getSelectionIndex();

        DataDescriptor descriptor = checkNotNull(getInstance(fDataModelIndex).getDataDescriptors().get(index));
        if (descriptor.getAspect() instanceof IDataChartNumericalAspect) {
            fXLogscale.setEnabled(true);
        } else {
            fXLogscale.setEnabled(false);
            fXLogscale.setSelection(false);
        }
    }

    private void enableYLog() {
        if (fAspectFilter != null && fAspectFilter instanceof IDataChartNumericalAspect) {
            fYLogscale.setEnabled(true);
        } else {
            fYLogscale.setEnabled(false);
            fYLogscale.setSelection(false);
        }
    }

    private void populateX() {
        fSelectionX.removeAll();
        getInstance(fDataModelIndex).getDataDescriptors().stream()
                .map(descriptor -> descriptor.getAspect().getLabel())
                .forEach(fSelectionX::add);
    }

    private void populateY() {
        java.util.List<String> input = new ArrayList<>();

        if (fAspectFilter == null) {
            for (DataDescriptor descriptor : getInstance(fDataModelIndex).getDataDescriptors()) {
                input.add(descriptor.getAspect().getLabel());
            }
        } else {
            for (DataDescriptor descriptor : getInstance(fDataModelIndex).getDataDescriptors()) {
                if (descriptor.getAspect().getClass() == checkNotNull(fAspectFilter).getClass()) {
                    input.add(descriptor.getAspect().getLabel());
                }
            }
        }

        fSelectionY.setInput(input);
    }

    private static IDataChartModel getInstance(int index) {
        return checkNotNull(IDataChartModel.getInstances().get(index));
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    private class ChartTypeSelectedEvent extends SelectionAdapter {
        @Override
        public void widgetSelected(@Nullable SelectionEvent event) {
            enableButton();
        }
    }

    private class DataModelSelectedEvent extends SelectionAdapter {
        @Override
        public void widgetSelected(@Nullable SelectionEvent event) {
            fDataModelIndex = fComboDataModel.getSelectionIndex();
            fAspectFilter = null;

            populateX();
            populateY();

            enableButton();
        }
    }

    private class ListSelectionEvent implements SelectionListener {
        @Override
        public void widgetSelected(@Nullable SelectionEvent e) {
            enableButton();
            enableXLog();
        }

        @Override
        public void widgetDefaultSelected(@Nullable SelectionEvent e) {
            enableButton();
            enableXLog();
        }
    }

    private class CheckBoxSelectedEvent implements ICheckStateListener {
        @Override
        public void checkStateChanged(@Nullable CheckStateChangedEvent event) {
            if (event == null) {
                return;
            }

            if (fSelectionY.getCheckedElements().length == 0) {
                fAspectFilter = null;
            } else if (fSelectionY.getCheckedElements().length == 1) {
                if (!event.getChecked()) {
                    enableYLog();
                    return;
                }

                for (DataDescriptor descriptor : getInstance(fDataModelIndex).getDataDescriptors()) {
                    if (descriptor.getAspect().getLabel().equals(event.getElement())) {
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
        public void handleEvent(@Nullable Event event) {
            Rectangle rect = getShell().getClientArea();
            Point size = fComposite.computeSize(rect.width, SWT.DEFAULT);
            fComposite.setSize(size);
        }
    }
}
