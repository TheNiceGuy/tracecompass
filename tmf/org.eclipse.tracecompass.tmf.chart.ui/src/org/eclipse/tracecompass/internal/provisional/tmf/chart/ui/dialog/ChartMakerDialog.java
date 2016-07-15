/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.dialog;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartSeries;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.model.IDataChartProvider;
import org.eclipse.tracecompass.internal.tmf.chart.ui.dialog.Messages;
import org.eclipse.tracecompass.internal.tmf.chart.ui.type.BarChartTypeDefinition;
import org.eclipse.tracecompass.internal.tmf.chart.ui.type.IChartTypeDefinition;
import org.eclipse.tracecompass.internal.tmf.chart.ui.type.ScatterChartTypeDefinition;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This dialog is used for configurating series before making a chart.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class ChartMakerDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Parent composite
     */
    private Composite fComposite;
    /**
     * Data model used for the creation of the chart
     */
    private IDataChartProvider<?> fDataProvider;
    /**
     * Table for displaying chart types
     */
    private TableViewer fTypeTable;
    /**
     * Table for displaying series
     */
    private TableViewer fSeriesTable;
    /**
     * Table for displaying valid X series
     */
    private TableViewer fSelectionX;
    /**
     * Checkbox table for displaying valid Y series
     */
    private CheckboxTableViewer fSelectionY;
    /**
     * Button used for creating a series
     */
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
     * Currently selected chart type
     */
    private @Nullable IChartTypeDefinition fType;
    /**
     * List of created series
     */
    private List<ChartSeries> fSeries = new ArrayList<>();
    /**
     * Filter for X data descriptors
     */
    private @Nullable IDataChartDescriptor<?, ?> fXFilter;
    /**
     * Filter for Y data descriptors
     */
    private @Nullable IDataChartDescriptor<?, ?> fYFilter;
    /**
     * Chart data created after the dialog
     */
    private @Nullable ChartData fDataSeries;
    /**
     * Chart model created after the dialog
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
     * @param model
     *            Model to choose source from
     */
    public ChartMakerDialog(Shell parent, IDataChartProvider<?> model) {
        super(parent);

        fComposite = parent;
        fDataProvider = model;

        /* Create tables */
        fTypeTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSeriesTable = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER);
        fSelectionX = new TableViewer(parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL);
        fSelectionY = checkNotNull(CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.NO_SCROLL | SWT.V_SCROLL));

        /* Create buttons */
        fAddButton = new Button(parent, SWT.NONE);
        fXLogscale = new Button(parent, SWT.CHECK);
        fYLogscale = new Button(parent, SWT.CHECK);

        setShellStyle(getShellStyle() | SWT.RESIZE);
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

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public Point getInitialSize() {
        return new Point(800, 600);
    }

    @Override
    public void create() {
        super.create();

        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    public Composite createDialogArea(@Nullable Composite parent) {
        fComposite = checkNotNull((Composite) super.createDialogArea(parent));
        getShell().setText(Messages.ChartMakerDialog_Title);

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
        typeColumn.getColumn().setWidth(50);
        typeColumn.setLabelProvider(new TypeLabelProvider());

        List<IChartTypeDefinition> types = new ArrayList<>();
        types.add(new BarChartTypeDefinition());
        types.add(new ScatterChartTypeDefinition());

        fTypeTable.getTable().setParent(fComposite);
        fTypeTable.getTable().setLayoutData(typeGridData);
        fTypeTable.setContentProvider(ArrayContentProvider.getInstance());
        fTypeTable.addSelectionChangedListener(new TypeSelectionListener());
        fTypeTable.setInput(types);

        /*
         * Series viewer
         */

        /**
         * FIXME: The labels in the first column cannot be aligned to the
         * center. The workaround is to put a dummy column that won't appear.
         */
        TableViewerColumn dummyColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        dummyColumn.setLabelProvider(new SeriesDummyLabelProvider());

        /* X series column */
        TableViewerColumn xSelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        xSelectionColumn.getColumn().setText(Messages.ChartMakerDialog_XSeries);
        xSelectionColumn.getColumn().setAlignment(SWT.CENTER);
        xSelectionColumn.getColumn().setResizable(false);
        xSelectionColumn.setLabelProvider(new SeriesXLabelProvider());

        /* Y series column */
        TableViewerColumn ySelectionColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        ySelectionColumn.getColumn().setText(Messages.ChartMakerDialog_YSeries);
        ySelectionColumn.getColumn().setAlignment(SWT.CENTER);
        ySelectionColumn.getColumn().setResizable(false);
        ySelectionColumn.setLabelProvider(new SeriesYLabelProvider());

        /* Remove buttons column */
        TableViewerColumn removeColumn = new TableViewerColumn(fSeriesTable, SWT.NONE);
        removeColumn.getColumn().setResizable(false);
        removeColumn.setLabelProvider(new SeriesRemoveLabelProvider());

        TableColumnLayout seriesLayout = new TableColumnLayout();
        seriesLayout.setColumnData(dummyColumn.getColumn(), new ColumnPixelData(0));
        seriesLayout.setColumnData(xSelectionColumn.getColumn(), new ColumnWeightData(50));
        seriesLayout.setColumnData(ySelectionColumn.getColumn(), new ColumnWeightData(50));
        seriesLayout.setColumnData(removeColumn.getColumn(), new ColumnPixelData(34));

        Group seriesGroup = new Group(fComposite, SWT.BORDER | SWT.FILL);
        seriesGroup.setText(Messages.ChartMakerDialog_SelectedSeries);
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
        fSelectionX.setInput(fDataProvider.getDataDescriptors());
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
        fSelectionY.setInput(fDataProvider.getDataDescriptors());
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

        GridLayout optionsLayout = new GridLayout();
        optionsLayout.numColumns = 1;

        GridData configOptionsGridData = new GridData();
        configOptionsGridData.horizontalAlignment = SWT.FILL;
        configOptionsGridData.grabExcessHorizontalSpace = true;

        Group optionsGroup = new Group(fComposite, SWT.BORDER);
        optionsGroup.setText(Messages.ChartMakerDialog_Options);
        optionsGroup.setLayout(optionsLayout);
        optionsGroup.setLayoutData(configOptionsGridData);

        /* Checkboxes for logscale */
        fXLogscale.setParent(optionsGroup);
        fXLogscale.setText(Messages.ChartMakerDialog_LogScaleX);

        fYLogscale.setParent(optionsGroup);
        fYLogscale.setText(Messages.ChartMakerDialog_LogScaleY);

        return fComposite;
    }

    @Override
    public void okPressed() {
        /* Create the data series */
        fDataSeries = new ChartData(fDataProvider, fSeries);

        /* Create the data model */
        ChartType type = checkNotNull(checkNotNull(fType).getType());
        String title = fDataProvider.getName();
        boolean xlog = fXLogscale.getSelection();
        boolean ylog = fYLogscale.getSelection();
        fChartModel = new ChartModel(type, title, xlog, ylog);

        super.okPressed();
    }

    // ------------------------------------------------------------------------
    // Util methods
    // ------------------------------------------------------------------------

    private boolean checkIfSeriesCompatible(IChartTypeDefinition typeA, IChartTypeDefinition typeB) {
        for (ChartSeries series : fSeries) {
            if (typeA.checkIfXDescriptorValid(series.getX(), null) != typeB.checkIfXDescriptorValid(series.getX(), null)) {
                return false;
            }

            if (typeA.checkIfYDescriptorValid(series.getY(), null) != typeB.checkIfYDescriptorValid(series.getY(), null)) {
                return false;
            }
        }

        return true;
    }

    private boolean checkIfButtonReady() {
        if (fSelectionX.getSelection().isEmpty()) {
            return false;
        }

        if (fSelectionY.getCheckedElements().length == 0) {
            return false;
        }

        return true;
    }

    private boolean checkIfSeriesPresent(ChartSeries test) {
        for (ChartSeries series : fSeries) {
            if (series.getX() == test.getX() && series.getY() == test.getY()) {
                return true;
            }
        }

        return false;
    }

    private @Nullable ChartSeries findButtonOwner(Button button) {
        for (ChartSeries series : fSeries) {
            ChartSeriesDialog line = (ChartSeriesDialog) series;
            if (line.getButton() == button) {
                return line;
            }
        }

        return null;
    }

    private void removeIncompatibleSeries(IChartTypeDefinition type) {
        Iterator<ChartSeries> iterator = fSeries.iterator();

        while (iterator.hasNext()) {
            ChartSeriesDialog series = (ChartSeriesDialog) checkNotNull(iterator.next());

            /* Check if the series if compatible */
            if (!type.checkIfXDescriptorValid(series.getX(), null) || !type.checkIfYDescriptorValid(series.getY(), null)) {
                /* Remove the button of the series */
                Button button = series.getButton();
                if (button != null) {
                    button.dispose();
                }

                /* Remove the series of the series list */
                iterator.remove();
            }
        }
    }

    private void unselectIncompatibleSeries(IChartTypeDefinition type) {
        /* Check if X selected series is compatible */
        IDataChartDescriptor<?, ?> descriptorX = (IDataChartDescriptor<?, ?>) fSelectionX.getStructuredSelection().getFirstElement();
        if (descriptorX != null && !type.checkIfXDescriptorValid(descriptorX, fXFilter)) {
            fSelectionX.getTable().deselectAll();
        }

        /* Check if Y selected series are compatible */
        for (Object element : fSelectionY.getCheckedElements()) {
            IDataChartDescriptor<?, ?> descriptorY = (IDataChartDescriptor<?, ?>) checkNotNull(element);

            if (!type.checkIfYDescriptorValid(descriptorY, fYFilter)) {
                fSelectionY.setChecked(element, false);
            }
        }
    }

    private void configureLogscaleCheckboxes() {
        /* Enable X logscale checkbox if possible */
        if (checkNotNull(fType).checkIfXLogscalePossible(fXFilter)) {
            fXLogscale.setEnabled(true);
        } else {
            fXLogscale.setEnabled(false);
            fXLogscale.setSelection(false);
        }

        /* Enable Y logscale checkbox if possible */
        if (checkNotNull(fType).checkIfYLogscalePossible(fYFilter)) {
            fYLogscale.setEnabled(true);
        } else {
            fYLogscale.setEnabled(false);
            fYLogscale.setSelection(false);
        }
    }

    private boolean tryResetXFilter() {
        if (fSeries.size() != 0) {
            return false;
        }

        fXFilter = null;
        return true;
    }

    private boolean tryResetYFilter() {
        if (fSeries.size() != 0) {
            return false;
        }

        if (fSelectionY.getCheckedElements().length != 0) {
            return false;
        }

        fYFilter = null;
        return true;
    }

    // ------------------------------------------------------------------------
    // Listeners, Providers, etc
    // ------------------------------------------------------------------------

    /**
     * This class extension avoid the use of an hashmap for linking a series
     * with a button. Each button in the series table are linked to a series.
     */
    private class ChartSeriesDialog extends ChartSeries {
        private @Nullable Button fButton;

        private ChartSeriesDialog(IDataChartDescriptor<?, ?> descriptorX, IDataChartDescriptor<?, ?> descriptorY) {
            super(descriptorX, descriptorY);
        }

        private @Nullable Button getButton() {
            return fButton;
        }

        private void setButton(Button button) {
            fButton = button;
        }
    }

    /**
     * This provider provides the image in the chart type selection table.
     */
    private class TypeLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public Image getImage(@Nullable Object element) {
            IChartTypeDefinition type = checkNotNull((IChartTypeDefinition) element);
            return new Image(fComposite.getDisplay(), type.getImageData());
        }
    }

    /**
     * This listener handle the selection in the chart type selection table.
     */
    private class TypeSelectionListener implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            IStructuredSelection selection = fTypeTable.getStructuredSelection();
            IChartTypeDefinition type = (IChartTypeDefinition) selection.getFirstElement();

            if (type == null) {
                return;
            }

            /* Check if the series are compatible with the chart type */
            if (fSeries.size() != 0 && !checkIfSeriesCompatible(checkNotNull(fType), type)) {
                String warning = Messages.ChartMakerDialog_WarningConfirm;
                String message = String.format(Messages.ChartMakerDialog_WarningIncompatibleSeries,
                        type.getType().toString().toLowerCase());

                /* Ask the user if he wants to continue */
                boolean choice = MessageDialog.openConfirm(fComposite.getShell(), warning, message);
                if (!choice) {
                    fTypeTable.setSelection(new StructuredSelection(fType));
                    return;
                }

                removeIncompatibleSeries(type);
                fSeriesTable.refresh();
            }

            fType = type;

            /* Refresh controls */
            unselectIncompatibleSeries(fType);

            if (tryResetXFilter()) {
                fSelectionX.refresh();
            }

            if (tryResetYFilter()) {
                fSelectionY.refresh();
            }

            fAddButton.setEnabled(checkIfButtonReady());
            configureLogscaleCheckboxes();
        }
    }

    /**
     * This dummy provider is used as a workaround in a column's bug.
     */
    private class SeriesDummyLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }
    }

    /**
     * This provider provides the labels for the X column of the series table.
     */
    private class SeriesXLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getX().getLabel();
        }
    }

    /**
     * This provider provides the labels for the Y column of the series table.
     */
    private class SeriesYLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            ChartSeries series = checkNotNull((ChartSeries) element);
            return series.getY().getLabel();
        }
    }

    /**
     * This provider provides the buttons for removing a series in the series
     * table.
     */
    private class SeriesRemoveLabelProvider extends ColumnLabelProvider {
        @Override
        public @Nullable String getText(@Nullable Object element) {
            return null;
        }

        @Override
        public void update(@Nullable ViewerCell cell) {
            if (cell == null) {
                return;
            }

            /* Create a button if it doesn't exist */
            ChartSeriesDialog series = (ChartSeriesDialog) cell.getViewerRow().getElement();
            Button button;
            if (series.getButton() == null) {
                Image image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE);
                button = new Button((Composite) cell.getViewerRow().getControl(), SWT.NONE);
                button.setImage(image);
                button.addListener(SWT.Selection, new SeriesRemoveButtonEvent());
                series.setButton(button);
            } else {
                button = series.getButton();
            }

            /* Set the position of the button into the cell */
            TableItem item = (TableItem) cell.getItem();
            TableEditor editor = new TableEditor(item.getParent());
            editor.grabHorizontal = true;
            editor.grabVertical = true;
            editor.setEditor(button, item, cell.getColumnIndex());
            editor.layout();
        }
    }

    /**
     * This listener handles the event when the button for removing a series is
     * clicked.
     */
    private class SeriesRemoveButtonEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            /* Dispose the button of the series */
            Button button = (Button) checkNotNull(event.widget);
            button.dispose();

            /* Remove the series from the list */
            ChartSeries series = findButtonOwner(button);
            fSeries.remove(series);
            fSeriesTable.refresh();

            /* Refresh controls */
            tryResetXFilter();
            fSelectionX.refresh();

            tryResetYFilter();
            fSelectionY.refresh();

            /* Disable OK button if no series are made */
            if (fSeries.size() == 0) {
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            }

            configureLogscaleCheckboxes();
        }
    }

    /**
     * This listener resizes the height of each row of the series table.
     */
    private class SeriesRowResize implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            if (event == null) {
                return;
            }

            event.height = 27;
        }
    }

    /**
     * This provider provides labels for {@link DataChartDescriptor}.
     */
    private class DataDescriptorLabelProvider extends ColumnLabelProvider {
        @Override
        public String getText(@Nullable Object element) {
            IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) checkNotNull(element);
            return descriptor.getLabel();
        }
    }

    /**
     * This filter is used for filtering labels in the X selection table.
     */
    private class CreatorXFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartTypeDefinition type = fType;

            if (type == null) {
                return false;
            }

            IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) checkNotNull(element);
            return type.checkIfXDescriptorValid(descriptor, fXFilter);
        }
    }

    /**
     * This filter is used for filtering labels in the Y selection table.
     */
    private class CreatorYFilter extends ViewerFilter {
        @Override
        public boolean select(@Nullable Viewer viewer, @Nullable Object parentElement, @Nullable Object element) {
            IChartTypeDefinition type = fType;

            if (type == null) {
                return false;
            }

            IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) checkNotNull(element);
            return type.checkIfYDescriptorValid(descriptor, fYFilter);
        }
    }

    /**
     * This listener handles the event when a selection is made in the X
     * selection table.
     */
    private class CreatorXSelectedEvent implements ISelectionChangedListener {
        @Override
        public void selectionChanged(@Nullable SelectionChangedEvent event) {
            /* Enable button if possible */
            fAddButton.setEnabled(checkIfButtonReady());
        }
    }

    /**
     * This listener handles the event when a value is checked in the Y
     * selection table.
     */
    private class CreatorYSelectedEvent implements ICheckStateListener {
        @Override
        public void checkStateChanged(@Nullable CheckStateChangedEvent event) {
            if (event == null) {
                return;
            }

            /* Set Y filter if needed */
            if (event.getChecked()) {
                if (fYFilter == null) {
                    IDataChartDescriptor<?, ?> descriptor = (IDataChartDescriptor<?, ?>) event.getElement();
                    fYFilter = descriptor;
                }
            } else {
                tryResetYFilter();
            }

            /* Refresh controls */
            fSelectionY.refresh();
            fAddButton.setEnabled(checkIfButtonReady());

            configureLogscaleCheckboxes();
        }
    }

    /**
     * This listener handle the event when the add button of the series creator
     * is clicked.
     */
    private class AddButtonClickedEvent implements Listener {
        @Override
        public void handleEvent(@Nullable Event event) {
            IDataChartDescriptor<?, ?> descriptorX = (IDataChartDescriptor<?, ?>) checkNotNull(fSelectionX.getStructuredSelection().getFirstElement());
            Object[] descriptorsY = fSelectionY.getCheckedElements();

            /* Create a series for each Y axis */
            for (int i = 0; i < descriptorsY.length; i++) {
                IDataChartDescriptor<?, ?> descriptorY = (IDataChartDescriptor<?, ?>) descriptorsY[i];
                ChartSeriesDialog series = new ChartSeriesDialog(descriptorX, checkNotNull(descriptorY));

                if (!checkIfSeriesPresent(series)) {
                    fSeries.add(series);
                }
            }

            /* Set the X filter */
            if (fXFilter == null) {
                fXFilter = descriptorX;
            }

            /* Refresh controls */
            fSeriesTable.refresh();
            fSelectionX.refresh();

            /* Enable OK button */
            getButton(IDialogConstants.OK_ID).setEnabled(true);

            configureLogscaleCheckboxes();
        }
    }

}
