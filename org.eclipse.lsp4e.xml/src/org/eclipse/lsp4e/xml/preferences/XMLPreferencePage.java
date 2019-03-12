package org.eclipse.lsp4e.xml.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class XMLPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XMLPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		Group formattingGroup = new Group(getFieldEditorParent(), SWT.NONE);
		formattingGroup.setLayout(new GridLayout());
		formattingGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		formattingGroup.setText("Formatting");
		addField(new BooleanFieldEditor("insertSpaces", "Insert spaces", formattingGroup));
		addField(new IntegerFieldEditor("tabSize", "Tab size", formattingGroup));
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.lsp4e.xml"));
		setDescription("XML preferences");
	}

}
