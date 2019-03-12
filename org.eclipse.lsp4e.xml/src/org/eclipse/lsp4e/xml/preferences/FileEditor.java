package org.eclipse.lsp4e.xml.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;

/**
 * A field editor to edit file paths.
 */
public class FileEditor extends ListEditor {

	/**
	 * The last path, or <code>null</code> if none.
	 */
	private String lastPath;

	/**
	 * Creates a new path field editor
	 */
	protected FileEditor() {
	}

	/**
	 * Creates a path field editor.
	 *
	 * @param name                the name of the preference this field editor works
	 *                            on
	 * @param labelText           the label text of the field editor
	 * @param dirChooserLabelText the label text displayed for the directory chooser
	 * @param parent              the parent of the field editor's control
	 */
	public FileEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}

	@Override
	protected String createList(String[] items) {
		StringBuilder path = new StringBuilder("");//$NON-NLS-1$

		for (String item : items) {
			path.append(item);
			path.append(File.pathSeparator);
		}
		return path.toString();
	}

	@Override
	protected String getNewInputObject() {

		FileDialog dialog = new FileDialog(getShell(), SWT.SHEET);
		if (lastPath != null) {
			if (new File(lastPath).exists()) {
				dialog.setFilterPath(lastPath);
			}
		}
		String dir = dialog.open();
		if (dir != null) {
			dir = dir.trim();
			if (dir.length() == 0) {
				return null;
			}
			lastPath = dir;
		}
		return dir;
	}

	@Override
	protected String[] parseString(String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
		ArrayList<Object> v = new ArrayList<>();
		while (st.hasMoreElements()) {
			v.add(st.nextElement());
		}
		return v.toArray(new String[v.size()]);
	}
}
