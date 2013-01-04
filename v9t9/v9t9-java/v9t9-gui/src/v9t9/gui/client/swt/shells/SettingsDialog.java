/**
 * 
 */
package v9t9.gui.client.swt.shells;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import v9t9.common.client.ISettingsHandler;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.IReadOnlyProperty;
import v9t9.common.settings.SettingSchemaProperty;
import v9t9.common.settings.SettingSchema;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.ImageCanvas;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

/**
 * @author ejs
 *
 */
public class SettingsDialog extends Composite implements IPropertyListener {
	public static final String SETTINGS_DIALOG_TOOL_ID = "settings.dialog";
	private TableViewer viewer;
	private Table table;

	public SettingsDialog(final Shell shell, final SwtWindow window, final IMachine machine) {
		super(shell, SWT.NONE);
		
		shell.setText("Settings");

		GridLayoutFactory.fillDefaults().applyTo(this);

		ISettingsHandler settings = machine.getSettings();
		
		viewer = new TableViewer(this, SWT.FULL_SELECTION | SWT.BORDER);
		
		table = viewer.getTable();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
		
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		viewer.setContentProvider(new ArrayContentProvider());
		

		FocusCellOwnerDrawHighlighter highlighter = new FocusCellOwnerDrawHighlighter(viewer); /* {

			protected Color getSelectedCellBackgroundColorNoFocus(
					ViewerCell cell) {
				return shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			}

			protected Color getSelectedCellForegroundColorNoFocus(
					ViewerCell cell) {
				return shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
			}
		};*/

		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(viewer, highlighter);
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(viewer);

		TableViewerEditor.create(viewer, focusCellManager, actSupport, ColumnViewerEditor.TABBING_HORIZONTAL
				| ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
				| ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION);

		final TextCellEditor textCellEditor = new TextCellEditor(viewer.getTable());

		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setMoveable(true);
		column.getColumn().setText("Name");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return ((IProperty) element).getName();
			}

		});
		

		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText("Value");
		column.setLabelProvider(new ColumnLabelProvider() {

			public String getText(Object element) {
				return String.valueOf(((IProperty) element).getValue());
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
			 */
			@Override
			public Color getForeground(Object element) {
				if (element instanceof IReadOnlyProperty) {
					return getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
				}
				return super.getForeground(element);
			}
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
			 */
			@Override
			public Font getFont(Object element) {
				if (element instanceof IReadOnlyProperty) {
					return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
				}
				return super.getFont(element);
			}

		});
		column.setEditingSupport(new EditingSupport(viewer) {
			protected boolean canEdit(Object element) {
				return (false == element instanceof IReadOnlyProperty);
			}

			protected CellEditor getCellEditor(Object element) {
				return textCellEditor;
			}

			protected Object getValue(Object element) {
				return String.valueOf(((IProperty) element).getValue());
			}

			protected void setValue(Object element, Object value) {
				((IProperty) element).setValueFromString((String) value);
				viewer.update(element, null);
			}
		});

		viewer.setComparator(new ViewerComparator() {
			/* (non-Javadoc)
			 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				IProperty s1 = (IProperty) e1;
				IProperty s2 = (IProperty) e2;
				if (s1 instanceof SettingSchemaProperty && s2 instanceof SettingSchemaProperty) {
					String c1 = ((SettingSchemaProperty) s1).getSchema().getContext();
					String c2 = ((SettingSchemaProperty) s2).getSchema().getContext();
					if (c1.equals(ISettingsHandler.TRANSIENT)) {
						if (!c2.equals(ISettingsHandler.TRANSIENT))
							return 1;
					} else {
						if (c2.equals(ISettingsHandler.TRANSIENT))
							return -1;
					}
					return super.compare(viewer, e1, e2);
				}
				else if (s1 instanceof SettingSchemaProperty) {
					return -1;
				}
				else if (s2 instanceof SettingSchemaProperty) {
					return 1;
				}
				else
					return super.compare(viewer, e1, e2);
			}
		});
		
		final List<IProperty> props = new ArrayList<IProperty>();
		for (Map.Entry<IProperty, SettingSchema> ent : settings.getAllSettings().entrySet()) {
			SettingSchema schema = ent.getValue();
			if (schema == null)
				continue;
			IProperty prop = settings.get(schema);
			if (prop != null) {
				props.add(prop);
				prop.addListener(this);
			}
		}
		
		viewer.setInput(props);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				for (IProperty prop : props)
					prop.removeListener(SettingsDialog.this);
			}
		});
		//viewer.setInput(settings.getAllSettings().keySet());
	}

	/* (non-Javadoc)
	 * @see ejs.base.properties.IPropertyListener#propertyChanged(ejs.base.properties.IProperty)
	 */
	@Override
	public void propertyChanged(final IProperty property) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!viewer.getControl().isDisposed()) {
					viewer.refresh(property);
				}
			}
		});
	}
	/**
	 * @param buttonBar
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final ImageCanvas buttonBar, 
			final IMachine machine,
			final SwtWindow window) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "SettingsDialogBounds";
				behavior.centering = Centering.OUTSIDE;
				behavior.centerOverControl = buttonBar.getShell();
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				SettingsDialog dialog = new SettingsDialog(shell, window, machine);
				return dialog;
			}
			@Override
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}



}
