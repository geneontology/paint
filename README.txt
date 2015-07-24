Instructions for Building PAINT

1.  Install JDK1.5 or above

2.  Install ant version 1.6 or above

3.  Retrieve code from paint and paintCommon packages.  Ensure paint and paintCommon directories are parallel.  Code from the paintCommon package is necessary since, the paint package is dependent on paintCommon. The build file from paint will invoke the build file from paintCommon to build components in paintCommon.

4.  Update property files with applicable settings.  The default configuration file for retrieving GO annotation information (paint/src/hibernate.cfg.xml) can be modified to refer to another data source.

5.  If necessary, execute command ant cleanall to delete files from previous builds.

6.  Execute command ant.  This will compile the files and build jar files.  All the files necessary for running the PAINT application will be copied into the paintApp directory.

=== Please update the wiki page with any changes you might make to the README - <code>http://wiki.geneontology.org/index.php/PAINT:_Getting_the_Source_Code</code> ===
==Getting the Source Code with Eclipse==

===Installing Subclipse===
Subclipse plugin adds seamless subversion support to Eclipse, using the existing Team mechanisms.<br>
Installation instructions: http://subclipse.tigris.org/install.html

===Getting the PAINT source code===
# Choose File -> New -> Project...
# Select "Checkout Projects from SVN" in the New Project Wizard, and click "Next"
# Choose "Create a New Repository Location" and click "Next"
# Enter <code>https://pantherdb.svn.sourceforge.net/svnroot/pantherdb</code> into the location field and click "Next"
# Choose the Paint directory to get the latest version and click "Next"
# Enter a name for the Eclipse project ("PAINT" is recommended) and click "Finish"


===Updating the Source Code via Subclipse===
# The source code for this project is updated periodically.  You have to update manually; Eclipse will not do this automatically.
# Right-click on the project name (e.g. PAINT) in the Package Explorer and choose Team -> Update from the menu
# When the update is complete, right-click on the project name again and choose Refresh

===Building PAINT in Eclipse===

# Setting up build path: Right-click on the project name (e.g. PAINT) in the Package Explorer and choose  Build Path -> Configure Build Path... from the menu (Order and Export Tab -> Select All, if all dependencies are not already selected).
#Checkout the paintCommon project from the pantherdb svn repository (Refer Getting the PAINT source code section above).
# The paintCommons jar is not always up to date in the PAINT project so build a jar after checking out paintCommons and add it to the PAINT classpath or add the paintCommons project in project references.
# GO to project properties - Run/Debug settings - PAINT - Edit - classpath - user settings and add "paint/config" and "paintCommons/config" folders which should contain the user.properties and treeViewer.properties files respectively.
# Clean all projects and Build 
# Make sure the JRE System Library (Default JVM on the system you are using) is included in the classpath. 