package org.eclipse.lsp4e.xml.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class XMLCatalogPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public XMLCatalogPreferencePage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		FileEditor catalogList = new FileEditor("catalogs", "XML catalogs:", getFieldEditorParent());
		addField(catalogList);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.eclipse.lsp4e.xml"));
		setDescription("Select your XML catalog to use");
	}

}
