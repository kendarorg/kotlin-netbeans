# Kotlin plugin for NetBeans IDE

## Installing Kotlin plugin
1. Download [the latest release](https://github.com/Baratynskiy/kotlin-netbeans/releases/tag/v0.0.0.3)
2. Launch NetBeans IDE
3. Choose **Tools** and then **Plugins** from the main menu
4. Switch to **Downloaded** tab
5. On the **Downloaded** tab click **Add Plugins...** button
6. In the file chooser, navigate to the folder with downloaded plugin. Select the NBM file and click OK. The plugin will show up in the list of plugins to be installed.
7. Click **Install** button in the Plugins dialog
8. Complete the installation wizard by clicking **Next**, agreeing to the license terms and clicking **Install** button.

* Note that this plugin requires NetBeans 8.1


## Plugin feature set

1. Kotlin project type
2. Syntax highlighting
3. Semantics highlighting
4. Diagnostics
5. Code completion
6. Navigation in Source Code

## For contributors

### Importing the project

1. File -> Create Project
2. Maven -> Project with the existing POM

To build this project you need to add *kotlin-ide-common.jar* from *lib* folder to local maven repository.