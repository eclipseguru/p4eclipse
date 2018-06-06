# Instructions for building P4Eclipse

## Prerequisites:

* Java 6 or higher
* Maven 3.x
* Eclipse Photon (4.8)

To build:
---------

 1. Run Maven to build an updatesite

         cd src/3.7/build/p4eclipse_parent
         mvn clean package -P p4update

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

Version note: Eclipse Photon (4.8) is required for the 2018.1 build.
