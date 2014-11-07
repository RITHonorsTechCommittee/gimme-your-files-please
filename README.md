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


Testing an API
--------------

1.  Deploy your version to AppEngine.  Unfortunately the devserver ALWAYS returns the same user even after authentication (example@example.com).  In order to test with actual drive API calls it must be running "in production"
2.  Go to http://gimmeyourfilesplease.appspot.com/_ah/api/explorer.  Between runs you may need to clear cache and/or cookies to get the changes to appear
3.  Find and run the API call you want to test
4.  If you are not authenticated, use the "main" version of the app first.  Go to https://gimmeyourfilesplease.appspot.com/list?folderId=0BwyAT8fk8FVDTGpNbnlmQ09KTGM  This request should ensure that you are properly authenticated for future API calls.

To Do!
======

* Implement API method bodies
* Create frontend (HTML + JS) to leverage backend API
* Test the calls that require authentication with a real client

