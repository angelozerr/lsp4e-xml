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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.osgi.framework.Bundle;

/**
 * LSP4e XML Language server.
 *
 */
public class XMLLanguageServer extends ProcessStreamConnectionProvider {

	private static final String[] SERVER_JARS = new String[] { "org.eclipse.lsp4j-0.4.1.jar",
			"org.eclipse.lsp4xml-0.0.1-SNAPSHOT.jar",
			"org.eclipse.lsp4xml.contentmodel-0.0.1-SNAPSHOT.jar", "xercesImpl-2.11.0.jar", "xml-apis-1.4.01.jar" };

	public XMLLanguageServer() {
		super(computeCommands(), computeWorkingDir());
	}

	private static String computeWorkingDir() {
		return System.getProperty("user.dir");
	}

	private static List<String> computeCommands() {
		List<String> commands = new ArrayList<>();
		// Try to use the configured Install JRE.
		IVMInstall install = JavaRuntime.getDefaultVMInstall();
		if (install != null) {
			File vmInstallLocation = install.getInstallLocation();
			File javaExecutableLocation = StandardVMType.findJavaExecutable(vmInstallLocation);
			// ex: C:\Program Files\Java\jre1.8.0_77\bin\javaw.exe
			commands.add(javaExecutableLocation.getAbsolutePath());
		} else {
			commands.add("java");
		}
		commands.add("-classpath");
		commands.add(computeXMLLanguageServerJarPath());
		commands.add("org.eclipse.lsp4xml.XMLServerLauncher");
		return commands;
	}

	private static String computeXMLLanguageServerJarPath() {
		StringBuilder path = new StringBuilder();
		for (String jarPath : SERVER_JARS) {
			if (path.length() != 0) {
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					path.append(";");
				} else {
					path.append(":");
				}
			}
			path.append(computeJarPath(jarPath));
		}
		return path.toString();
	}

	private static String computeJarPath(String jarName) {
		Bundle bundle = Platform.getBundle(XMLPlugin.PLUGIN_ID);
		URL fileURL = bundle.getEntry("server/" + jarName);
		try {
			URL resolvedFileURL = FileLocator.toFileURL(fileURL);

			// We need to use the 3-arg constructor of URI in order to properly escape file
			// system chars
			URI resolvedURI = new URI(resolvedFileURL.getProtocol(), resolvedFileURL.getPath(), null);
			File file = new File(resolvedURI);
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				return "" + file.getAbsolutePath() + "";
			} else {
				return file.getAbsolutePath();
			}
		} catch (URISyntaxException | IOException exception) {
			XMLPlugin.log(new Status(IStatus.ERROR, XMLPlugin.PLUGIN_ID, "Cannot get the XML LSP Server jar.", //$NON-NLS-1$
					exception));
		}
		return "";
	}

	class GetErrorThread extends Thread {

		private final Process process;
		private String message = null;

		public GetErrorThread(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			try (final BufferedReader b = new BufferedReader(new InputStreamReader(getErrorStream()))) {
				String line;
				if ((line = b.readLine()) != null) {
					message = line;
					synchronized (XMLLanguageServer.this) {
						XMLLanguageServer.this.notifyAll();
					}
				}
			} catch (IOException e) {
				message = e.getMessage();
			}
		}

		public void check() throws IOException {
			if (message != null) {
				// throw new IOException(message);
			}
			if (!process.isAlive()) {
				throw new IOException("Process is not alive"); //$NON-NLS-1$
			}
		}

	}

	@Override
	public void start() throws IOException {
		// Start the process
		super.start();
		// Get the process by Java reflection
		Process p = getProcess();
		if (p != null) {
			// Sart a thread which read error stream to check that the java command is
			// working.
			GetErrorThread t = new GetErrorThread(p);
			try {
				t.start();
				// wait a little to execute java command line...
				synchronized (XMLLanguageServer.this) {
					try {
						this.wait(500);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				// check if there is an error or if process is not alived.
				try {
					t.check();
				} catch (IOException e) {
					throw new IOException("Unable to start language server: " + this.toString(), e); //$NON-NLS-1$
				}
			} finally {
				t.interrupt();
			}
		}
	}

	private Process getProcess() {
		try {
			Field f = ProcessStreamConnectionProvider.class.getDeclaredField("process");
			f.setAccessible(true);
			Process p = (Process) f.get(this);
			return p;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder builder = super.createProcessBuilder();
		// override redirect to PIPE to read error stream with GetErrorThread
		builder.redirectError(ProcessBuilder.Redirect.PIPE);
		return builder;
	}

	@Override
	public String toString() {
		return "XML (" + super.toString() + ")";
	}

}
