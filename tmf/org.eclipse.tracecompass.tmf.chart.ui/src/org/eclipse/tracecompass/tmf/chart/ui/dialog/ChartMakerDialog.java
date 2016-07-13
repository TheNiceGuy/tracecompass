/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.chart.ui.dialog;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.tmf.chart.core.aspect.IDataChartAspect;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartData;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartModel;
import org.eclipse.tracecompass.tmf.chart.core.model.ChartSeries;
import org.eclipse.tracecompass.tmf.chart.core.model.DataDescriptor;
import org.eclipse.tracecompass.tmf.chart.core.model.IDataChartModel;
import org.eclipse.tracecompass.tmf.chart.ui.type.BarChartType;
import org.eclipse.tracecompass.tmf.chart.ui.type.IChartType;
import org.eclipse.tracecompass.tmf.chart.ui.type.ScatterChartType;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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

    private IDataChartModel fDataModel;

    private TableViewer fTypeTable;

    private TableViewer fSeriesTable;
    /**
     * List for selecting the X axis
     */
    private TableViewer fSelectionX;
    /**
     * Checkbox list for selecting the Y axis
     */
    private CheckboxTableViewer fSelectionY;

    private Button fAddButton;
    /**
     * Checkbox for indicating whether X axis is logarithmic
     */
    private Button fXLogscale;
    /**
     * Checkbox for indicating whether Y axis is logarithmic
     */
    private Button fYLogscale;
    /**
     * Data series created after the dialog
     */
    private @Nullable ChartData fDataSeries;
    /**
     * Data model created after the dialog
     */
    private @Nullable ChartModel fChartModel;

    private @Nullable IChartType fType;

    private List<ChartSeriesDialog> fSeries = new ArrayList<>();

    private @Nullable IDataChartAspect fXAspectFilter;

    private @Nullable IDataChartAspect fYAspectFilter;

    // ------------------------------------------------------------------------
    // Important methods
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * @param parent
     *            Parent shell
     * @param model
     *            Model to choose source from
     */
    public ChartMakerDialog(Shell parent, IDataChartModel model) {
        super(parent);

        fComposite = parent;
        fDataModel = model;
        fTypeTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSeriesTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSelectionX = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSelectionY = checkNotNull(CheckboxTableViewer.newCheckList(parent, SWT.BORDER));
        fAddButton = new Button(parent, SWT.NONE);

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
        baseLayout.numColumns = 2;

        GridLayout genericGridLayout = new GridLayout();

        GridData genericFillGridData = new GridData();
        genericFillGridData.horizontalAlignment = SWT.FILL;
        genericFillGridData.verticalAlignment = SWT.FILL;
        genericFillGridData.grabExcessHorizontalSpace = true;
        genericFillGridData.grabExcessVerticalSpace = true;

        fComposite.setLayout(baseLayout);

        /*
         * Chart type selector
         */
        GridData typeGridData = new GridData();
        typeGridData.verticalSpan = 3;
        typeGridData.horizontalAlignment = SWT.FILL;
        typeGridData.verticalAlignment = SWT.FILL;
        typeGridData.grabExcessHorizontalSpace = false;
        typeGridData.grabExcessVerticalSpace = true;

        TableViewerColumn typeColumn = new TableViewerColumn(fTypeTable, SWT.NONE);
        typeColumn.getColumn().setWidth(70);
        typeColumn.setLabelProvider(new TypeLabelProvider());

        List<IChartType> types = new ArrayList<>();
        types.add(new BarChartType());
        types.add(new ScatterChartType());

        fTypeTable.getTable().setParent(fComposite);
        fTypeTable.getTable().setLayoutData(typeGridData);
        fTypeTable.setContentProvider(ArrayContentProvider.getInstance());
        fTypeTable.addSelectionChangedListener(new TypeSelectionListener());
        fTypeTable.setInput(types);

        /*
         * Series viewer
         */
        TableViewerColumn xSelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        xSelectionColumn.getColumn().setText("X");
        xSelectionColumn.getColumn().setResizable(false);
        xSelectionColumn.setLabelProvider(new SeriesXLabelProvider());

        TableViewerColumn ySelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        ySelectionColumn.getColumn().setText("Y");
        ySelectionColumn.getColumn().setResizable(false);
        ySelectionColumn.setLabelProvider(new SeriesYLabelProvider());

        TableViewerColumn removeColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        removeColumn.getColumn().setResizable(false);
        removeColumn.setLabelProvider(new RemoveLabelProvider());

        TableColumnLayout seriesLayout = new TableColumnLayout();
        seriesLayout.setColumnData(xSelectionColumn.getColumn(), new ColumnWeightData(47));
        seriesLayout.setColumnData(ySelectionColumn.getColumn(), new ColumnWeightData(47));
        seriesLayout.setColumnData(removeColumn.getColumn(), new ColumnPixelData(35));

        Group seriesGroup = new Group(fComposite, SWT.BORDER | SWT.FILL);
        seriesGroup.setText("Selected Series");
        seriesGroup.setLayout(genericGridLayout);
        seriesGroup.setLayoutData(genericFillGridData);

        Composite seriesComposite = new Composite(seriesGroup, SWT.NONE);
        seriesComposite.setLayout(seriesLayout);
        seriesComposite.setLayoutData(genericFillGridData);

        fSeriesTable.getTable().setParent(seriesComposite);
        fSeriesTable.getTable().setHeaderVisible(true);
        fSeriesTable.getTable().addListener(SWT.MeasureItem, new SeriesRowResize());
        fSeriesTable.setContentProvider(ArrayContentProvider.getInstance());
        fSeriesTable.setInput(fSeries);

        /*
         * Series creator
         */
        GridLayout creatorLayout = new GridLayout();
        creatorLayout.numColumns = 2;

        Group creatorGroup = new Group(fComposite, SWT.BORDER);
        creatorGroup.setText(Messages.ChartMakerDialog_SeriesCreator);
        creatorGroup.setLayout(creatorLayout);
        creatorGroup.setLayoutData(genericFillGridData);

        GridData creatorLabelGridData = new GridData();
        creatorLabelGridData.horizontalAlignment = SWT.CENTER;
        creatorLabelGridData.verticalAlignment = SWT.BOTTOM;

        /* Top labels */
        Label creatorLabelX = new Label(creatorGroup, SWT.NONE);
        creatorLabelX.setText(Messages.ChartMakerDialog_XAxis);
        creatorLabelX.setLayoutData(creatorLabelGridData);

        Label creatorLabelY = new Label(creatorGroup, SWT.NONE);
        creatorLabelY.setText(Messages.ChartMakerDialog_YAxis);
        creatorLabelY.setLayoutData(creatorLabelGridData);

        /* X axis table */
        TableViewerColumn creatorXColumn = new TableViewerColumn(fSelectionX, SWT.NONE);
        creatorXColumn.getColumn().setResizable(false);
        creatorXColumn.setLabelProvider(new DataDescriptorLabelProvider());

        TableColumnLayout creatorXLayout = new TableColumnLayout();
        creatorXLayout.setColumnData(creatorXColumn.getColumn(), new ColumnWeightData(100));

        Composite creatorXComposite = new Composite(creatorGroup, SWT.NONE);
        creatorXComposite.setLayout(creatorXLayout);
        creatorXComposite.setLayoutData(genericFillGridData);

        fSelectionX.getTable().setParent(creatorXComposite);
        fSelectionX.setContentProvider(ArrayContentProvider.getInstance());
        fSelectionX.setInput(fDataModel.getDataDescriptors());
        fSelectionX.setFilters(new CreatorXFilter());
        fSelectionX.addSelectionChangedListener(new CreatorXSelectedEvent());

        /* Y axis table */
        TableViewerColumn creatorYColumn = new TableViewerColumn(fSelectionY, SWT.NONE);
        creatorYColumn.getColumn().setResizable(false);
        creatorYColumn.setLabelProvider(new DataDescriptorLabelProvider());

        TableColumnLayout creatorYLayout = new TableColumnLayout();
        creatorYLayout.setColumnData(creatorYColumn.getColumn(), new ColumnWeightData(100));

        Composite creatorYComposite = new Composite(creatorGroup, SWT.NONE);
        creatorYComposite.setLayout(creatorYLayout);
        creatorYComposite.setLayoutData(genericFillGridData);

        fSelectionY.getTable().setParent(creatorYComposite);
        fSelectionY.setContentProvider(ArrayContentProvider.getInstance());
        fSelectionY.setInput(fDataModel.getDataDescriptors());
        fSelectionY.setFilters(new CreatorYFilter());
        fSelectionY.addCheckStateListener(new CreatorYSelectedEvent());

        /* Add button */
        Label creatorLabelEmpty = new Label(creatorGroup, SWT.NONE);
        creatorLabelEmpty.setText(""); //$NON-NLS-1$

        GridData creatorButtonGridData = new GridData();
        creatorButtonGridData.horizontalAlignment = SWT.RIGHT;
        creatorButtonGridData.widthHint = 30;
        creatorButtonGridData.heightHint = 30;

        Image addImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD);
        fAddButton.setParent(creatorGroup);
        fAddButton.setLayoutData(creatorButtonGridData);
        fAddButton.setImage(addImage);
        fAddButton.addListener(SWT.Selection, new AddButtonClickedEvent());

        /*
         * Options
         */
        GridData configOptionsGridData = new GridData();
        configOptionsGridData.horizontalAlignment = SWT.FILL;
        configOptionsGridData.grabExcessHorizontalSpace = true;

        GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 1;

        Group optionsGroup = new Group(fComposite, SWT.BORDER);
        optionsGroup.setText(Messages.ChartMakerDialog_Options);
        optionsGroup.setLayout(optionsLayout);
        optionsGroup.setLayoutData(configOptionsGridData);

        fXLogscale.setParent(optionsGroup);
        fXLogscale.setText(Messages.ChartMakerDialog_LogScaleX);

        fYLogscale.setParent(optionsGroup);
        fYLogscale.setText(Messages.ChartMakerDialog_LogScaleY);

        getShell().setSize(800, 600);

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
        // java.util.List<DataDescriptor> descriptors =
        // getInstance(fDataModelIndex).getDataDescriptors();
        // String title = getInstance(fDataModelIndex).getTitle();
        //
        // ChartModel.ChartType type =
        // ChartModel.ChartType.resolveChartType(checkNotNull(fComboChartType.getText()));
        // if (type == null) {
        // return;
        // }
        //
        // java.util.List<DataDescriptor> xAxis = new ArrayList<>();
        // java.util.List<DataDescriptor> yAxis = new ArrayList<>();
        //
        // for (DataDescriptor descriptor : descriptors) {
        // for (Object entry : fSelectionY.getCheckedElements()) {
        // if (descriptor.getAspect().getLabel().equals(entry.toString())) {
        // yAxis.add(descriptor);
        // xAxis.add(descriptors.get(fSelectionX.getSelectionIndex()));
        // }
        // }
        // }
        //
        // fDataSeries = new ChartData(xAxis, yAxis);
        // fChartModel = new ChartModel(type, title, fXLogscale.getSelection(),
        // fYLogscale.getSelection());
        //
        // super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    private boolean checkIfButtonReady() {
        if (fSelectionX.getSelection().isEmpty()) {
            return false;
        }

        if (fSelectionY.getCheckedElements().length == 0) {
            return false;
        }

        return true;
    }

    private boolean isSeriesPresent(ChartSeries test) {
        for (ChartSeries series : fSeries) {
            if (series.getX() == test.getX() && series.getY() == test.getY()) {
                return true;
            }
        }

        return false;
    }

    private void enableXLog() {
        // int index = fSelectionX.getSelectionIndex();
        //
        // DataDescriptor descriptor =
        // checkNotNull(getInstance(fDataModelIndex).getDataDescriptors().get(index));
        // if (descriptor.getAspect() instanceof IDataChartNumericalAspect) {
        // fXLogscale.setEnabled(true);
        // } else {
        // fXLogscale.setEnabled(false);
        // fXLogscale.setSelection(false);
        // }
    }

    private void enableYLog() {
        // if (fAspectFilter != null && fAspectFilter instanceof
        // IDataChartNumericalAspect) {
        // fYLogscale.setEnabled(true);
        // } else {
        // fYLogscale.setEnabled(false);
        // fYLogscale.setSelection(false);
        // }
    }

    // ------------------------------------------------------------------------
    // Listeners, Providers, etc
    // ------------------------------------------------------------------------

    private class ChartSeriesDialog extends ChartSeries {

        private @Nullable Button fButton;

        private ChartSeriesDialog(DataDescriptor descriptorX, DataDescriptor descriptorY) {
            super(descriptorX, descriptorY);
        }

        private @Nullable Button getButton() {
            return fButton;
        }

        private void setButton(Button button) {
            fButton = button;
        }
    }


    private class TypeLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public Image getImage(@Nullable Object element) {
            IChartType type = checkNotNull((IChartType) element);
            return new Image(fComposite.getDisplay(), type.getImageData());
        }
    }

    private class TypeSelectionListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            IStructuredSelection selection = fTypeTable.getStructuredSelection();
            fType = (IChartType) selection.getFirstElement();

            fSelectionX.getTable().deselectAll();
            fSelectionY.getTable().deselectAll();

            fXAspectFilter = null;
            fYAspectFilter = null;

            fSelectionX.refresh();
            fSelectionY.refresh();

            fAddButton.setEnabled(checkIfButtonReady());
        }
    }

    private class SeriesXLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getX().getAspect().getLabel();
        }
    }

    private class SeriesYLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getY().getAspect().getLabel();
        }
    }

    private class RemoveLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public void update(@Nullable ViewerCell cell) {
            if(cell == null) {
                return;
            }

            /* Create a button if it doesn't exist */
            ChartSeriesDialog series = (ChartSeriesDialog) cell.getViewerRow().getElement();
            Button button;
            if(series.getButton() == null) {
                Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
                button = new Button((Composite) cell.getViewerRow().getControl(),SWT.NONE);
                button.setImage(image);
                series.setButton(button);
            } else {
                button = series.getButton();
            }

            /* Set the position of the button into the cell */
            TableItem item = (TableItem) cell.getItem();
            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal  = true;
            editor.grabVertical = true;
            editor.setEditor(button , item, cell.getColumnIndex());
            editor.layout();
        }
    }

    private class SeriesRowResize implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if(event == null) {
                return;
            }

            event.height = 25;
        }
    }

    private class DataDescriptorLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            DataDescriptor descriptor = checkNotNull((DataDescriptor) element);
            return descriptor.getAspect().getLabel();
        }
    }

    private class CreatorXFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartType type = fType;

            if (type == null) {
                return false;
            }

            DataDescriptor descriptor = checkNotNull((DataDescriptor) element);
            return type.filterX(descriptor.getAspect(), fXAspectFilter);
        }
    }

    private class CreatorYFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartType type = fType;

            if (type == null) {
                return false;
            }

            DataDescriptor descriptor = checkNotNull((DataDescriptor) element);
            return type.filterY(descriptor.getAspect(), fYAspectFilter);
        }
    }

    private class CreatorXSelectedEvent implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            fAddButton.setEnabled(checkIfButtonReady());
        }
    }

    private class CreatorYSelectedEvent implements ICheckStateListener {
        @Override
        public void checkStateChanged(@Nullable CheckStateChangedEvent event) {
            if (event == null) {
                return;
            }

            if (event.getChecked()) {
                if (fYAspectFilter == null) {
                    DataDescriptor descriptor = (DataDescriptor) event.getElement();
                    fYAspectFilter = descriptor.getAspect();
                }
            } else {
                if (fSelectionY.getCheckedElements().length == 0) {
                    fYAspectFilter = null;
                }
            }

            fSelectionY.refresh();
            fAddButton.setEnabled(checkIfButtonReady());
        }
    }

    private class AddButtonClickedEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            DataDescriptor descriptorX = (DataDescriptor) checkNotNull(fSelectionX.getStructuredSelection().getFirstElement());
            Object[] descriptorsY = fSelectionY.getCheckedElements();

            for (int i = 0; i < descriptorsY.length; i++) {
                DataDescriptor descriptorY = (DataDescriptor) descriptorsY[i];
                ChartSeriesDialog series = new ChartSeriesDialog(descriptorX, checkNotNull(descriptorY));

                if (!isSeriesPresent(series)) {
                    fSeries.add(series);
                }
            }

            fSeriesTable.refresh();
        }
    }
}
