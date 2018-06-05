# Instructions for building P4Eclipse in a Linux environment
 
## Prerequisites:

* Java 6 or higher
* Ant 1.6 or higher
* Maven 3.x
* Eclipse Luna (4.4) or Eclipse Mars (4.5)

To build:
---------

 1. Environment variables (change according to your own environment):
    
        $ export MVN=`which mvn`
        $ export VER=2015.1
        $ export CHANGE=123456
 
 2. Run the script to build an updatesite
 
         cd src/3.7/build/p4eclipse_parent
         ./build_linux.sh updatesite
 
     The results will be in:

        src/3.7/build/p4eclipse_updatesite/target/p4eclipse-updatesite-***-site.zip
 
 3. Unarchive the new updatesite zip file onto an empty directory (i.e. p4-eclipse-plugin):
 
        $ unzip p4eclipse-updatesite-***-site.zip
 
 4. Install from the new updatesite directory:
 
    * Eclipse -> Help -> Install New Software
    * click "Add..."
    * click "Local..."
    * browse to the new unarchive p4eclipse updatesite directory
    * ...and follow the normal installation procedure... 
 
Version note: Eclipse Luna (4.4) is required for the 2014.1 build. Eclipse Mars (4.5) is required for the 2015.1 build.
