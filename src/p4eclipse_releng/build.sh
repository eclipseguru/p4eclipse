#!/bin/bash
#

#################################################################################
# This script is used by BnR to build the p4eclipse for each release.
# It should work with the tools/build/conf/module-p4eclipse.conf
# especiall the following sections:
# [ module_sources ] : dependencies
# [ module_artifacts ] : outputs
#
# Note:
#   The BnR build will start the job by mapping jobdir to //depot
#   so:
#      //depot/import/eclipse/...  => jobdir/import/eclipse/...
#      p4-doc/manuals/p4-eclipse/...  => jobdir/p4-doc/manuals/p4-eclipse/...
#      p4eclipse/...  => jobdir/p4eclipse/...
#
#   In the following script, BUILDDIR=jobdir.
#  
##################################################################################

#set -x # echo command
#set -v # echo input

BASEDIR=`dirname $0`
SCRIPT=$0

BUILDDIR=$1
OUTPUTDIR=$2
VER=$3
QUALIFIER=$4
MVN=$5

: ${MVN:="/opt/apache-maven/bin/mvn"}
: ${PROP:="-Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true"}
: ${MAVEN_OPTS:='-Xmx2048m -XX:MaxPermSize=256m'}
: ${TYCHO_VERSION:="0.20.0"}
##: ${KEYSTORE:="/home/ali/.keystore"}

usage () {
  echo ""
  echo "Usage: "
  echo "      $SCRIPT builddir outputdir VER QUALIFIER MVN"
  echo
  echo "For example:"
  echo "      ./build.sh /home/ali/build/job/ /home/ali/build/job/p4-bin/bin.java 2012.1.432244 MAIN-TEST_ONLY-SNAPSHOT /opt/apache-maven/bin/mvn"
  echo ""
}

printSettings () {
  echo
  echo "Build P4ECLIPSE ..."
  echo
  echo "[Input variables]"
  echo "  BUILDDIR=$BUILDDIR"
  echo "  OUTPUTDIR=$OUTPUTDIR"
  echo "  VER=$VER"
  echo "  QUALIFIER=$QUALIFIER"
  echo "  MVN=$MVN"
  echo "  MAVEN_OPTS=$MAVEN_OPTS"
  echo "  KEYSTORE=${KEYSTORE}"
  echo 
}

buildBranch () {
  eclipseVersion=$1
  branch=$2
  
  echo $eclipseVersion $branch

  BCHDIR=$BUILDDIR/p4-eclipse/${branch}

  trunked=`echo $eclipseVersion|tr -d '.'`

  props="-DtargetPlatform=p4e-${trunked} -Dp2repo.url=http://artifactory.bnr.perforce.com:8081/artifactory/p2repo-${trunked}/"

  cd $BCHDIR/build/p4eclipse_parent	
  $MVN -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:$TYCHO_VERSION:set-version -DnewVersion=$VER
  # $MVN -DforceContextQualifier=$QUALIFIER -P p4update,replace-p4java clean process-sources --projects ../../plugins/com.perforce.team.core ${props}
  $MVN -f ${BCHDIR}/build/p4eclipse_parent/replacep4java.xml process-sources -Dtargetprj=${BCHDIR}/plugins/com.perforce.team.core 

  if [ -z "${KEYSTORE}" ]; then
    echo "Build without jar sigining ..."
    $MVN -DforceContextQualifier=$QUALIFIER -P p4update,replace-help clean package ${props} 
  else
    echo "Build and sign jars ..."
    $MVN -DforceContextQualifier=$QUALIFIER -P p4update,signjar,replace-help clean package ${props}
  fi

  echo "cp -a $BCHDIR/build/p4eclipse_updatesite/target/p4eclipse-updatesite*site.zip $OUTPUTDIR/p4eclipse${trunked}.zip"
  cp -a $BCHDIR/build/p4eclipse_updatesite/target/p4eclipse-updatesite*site.zip $OUTPUTDIR/p4eclipse${trunked}.zip
}


############ main entry ##############

if [ $# -lt 5 ]; then
   usage
   exit 1
fi

export MAVEN_OPTS=$MAVEN_OPTS

printSettings

# Update this list for each release
eclipseVersions=( 4.3 4.4 4.5 )
branch=3.7

for eclipseVersion in ${eclipseVersions[@]}; do
  buildBranch $eclipseVersion $branch
done

