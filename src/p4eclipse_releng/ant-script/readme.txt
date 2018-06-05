To build p4eclipse, follow steps:

1) Setup local p2 target
   a. mirror from remote to local, and convert local mirror to runnable format
      $ ant p2repo3.7
      
2) go into each branch, and invoke:
   $ cd build/p4eclipse_parent
   $ mvn clean package -Pp4update,signjar
   
Step 1) only needed to run once for each new eclipse release.

The mirror configuration is in p2mirrorx.x.xml (e.g., p2mirror3.7.xml).
And the default mirror path is /home/ali/build/tools/heliosRepo, the
runnable format is at /home/ali/build/tools/p2repox.x


To build standalone p4eclipse, run:
  $mvn clean pakcage -Pp4update,p4rcp         

To prepare RCP with all dependencies:
ant installRcpTarget -DrcpTargetPath=/home/ali/build/rcp_target
