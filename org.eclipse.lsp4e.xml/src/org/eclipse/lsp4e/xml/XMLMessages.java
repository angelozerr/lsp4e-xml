/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4e.xml;

import org.eclipse.osgi.util.NLS;

/**
 * XML messages.
 *
 */
public class XMLMessages extends NLS {

	private static final String BUNDLE_NAME = " org.eclipse.lsp4e.xml.XMLMessages";//$NON-NLS-1$

	private XMLMessages() {
		// Do not instantiate
	}

	public static String XMLPlugin_internal_error;

	static {
		NLS.initializeMessages(BUNDLE_NAME, XMLMessages.class);
	}

}
