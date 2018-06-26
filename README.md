[![Build Status](https://secure.travis-ci.org/angelozerr/lsp4e-xml.png)](http://travis-ci.org/angelozerr/lsp4e-xml)

Eclipse XML LSP
===========================

The `LSP4e XML` is a an Eclipse plugin for XML based on:

* [Eclipse LSP4E](https://projects.eclipse.org/projects/technology.lsp4e) to consume the [XML Language Server](https://github.com/angelozerr/xml-languageserver) inside Eclipse.
* [Eclipse TM4E](https://projects.eclipse.org/projects/technology.tm4e) to support XML syntax coloration based on TextMate grammar. 
* [XML Language Server](https://github.com/angelozerr/xml-languageserver) the XML Language Server.

Features
===========================

TODO
 
Demo
===========================

TODO
 
Installation
===========================

 * Update Site: http://oss.opensagres.fr/lsp4e-xml/snapshot/

Build
===========================

See cloudbees job: https://opensagres.ci.cloudbees.com/job/lsp4e-xml/

Development in Eclipse
======================

1. Use "Eclipse for Committers" (Photon M6 as of this writing).

2. In Eclipse, "File" / "Import..." / "Existing Maven Projects". Point at the `lsp4e-xml` project root directory, add all the Maven projects it finds.

3. Now go to "Window" / "Preferences" / "Plug-in Development" / "Target Platform", and Select "lsp4e-freemarker" (this only appears if you have imported the "target-platform" Maven project earlier).
   After this, there shouldn't be more errors in the project (no dependency classes that aren't found).

4. To try the plugin, right click on the `org.eclipse.lsp4j.xml` project, then "Run as" / "Eclipse Application".
   (TODO: Currently that will fail with `Application "org.eclipse.ui.ide.workbench" could not be found in the registry`. I have worked that around by adding 
   `<location path="${eclipse_home}" type="Directory"/>` to the target platform, but of course there must be a better way.)
