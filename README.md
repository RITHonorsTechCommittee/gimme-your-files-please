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
3.  Navigate to the git checkout and load the project that is found.
4.  After the import, right click on the main gimme-your-files-please project, and choose Maven > Update project to download the dependencies.
  
Running the development server
------------------------------
1.  From <basedir>, run the following command to build the project
        mvn clean install 
2.  Run the following command to start the server
        mvn appengine:devserver
3.  Use the tool!  http://localhost:8888/list?folderId=0BwyAT8fk8FVDTGpNbnlmQ09KTGM will list the owners of Tech Committee's files.  You will need to authenticate to access this page.
        
Running the development server from Eclipse
------------------------------------------------------------
1.  Right click on the server and select "Run As"
2.  select Run on Server
3.  Create a new local AppEngine server (if necessary).  The default configuration show be fine
4.  Hit finish!

From then on, you should be able to start the server using the run configuration "Google App Engine at localhost" (or something to that effect).  This also works for debugging.


To Do!
======

* Break out the long API work into an asynchronous API with a progress bar
* Persist the calculations in the datastore
* Better handle oauth redirect to keep url parameters
* Handle the permission transfer / revocation

