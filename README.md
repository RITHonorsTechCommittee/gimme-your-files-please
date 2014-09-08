gimme-your-files-please
=======================

Tool to quickly manage permissions for an entire directory of Google Drive Files




Development
===========

This project uses Maven to manage dependencies.  This allows us to specify the different libraries we need without having to manually download them all and add them to the git repository.

Importing the projects into eclipse
-----------------------------------
1.  On project explorer, right click and choose import
2.  Choose Maven > Existing Maven Projects
3.  Navigate to the git checkout and load all the projects that are found.
4.  After the import, right click on the main gimme-your-files-please project, and choose Maven > Update project to download the dependencies.
  
Running the development server
------------------------------
1.  From <basedir>, run the following command to build the project
        mvn clean install 
2.  cd into <base dir>/gimme-your-files-please-ear
3.  run the following command to start the server
        mvn appengine:devserver
        
Running the development server from Eclipse (with debugging)
------------------------------------------------------------
1.  Todo...
