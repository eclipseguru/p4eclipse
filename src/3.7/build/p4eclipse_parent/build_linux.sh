#!/usr/bin/env bash
#
set -x # echo command
set -v # echo input

BASEDIR=`dirname $0`
SCRIPT=$0
OS=${OSTYPE//[0-9.]/}

: ${WORKSPACE:="$BASEDIR/../.."}
: ${MVN:="/opt/apache-maven/bin/mvn"}
: ${P4:="/opt/perforce/client/bin/p4"}
: ${VER:="2018.1"}
: ${CHANGE:="0"}
: ${PROP:="-Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true"}
: ${MAVEN_OPTS:='-Xmx1024m -XX:MaxPermSize=128m'}
: ${SIGN_JAR:="no"}
: ${UPDATE_VERSION:="no"}
: ${TYCHO_VERSION:="1.2.0"}

usage () {
  echo ""
  echo "Usage: "
  echo "      $SCRIPT updatesite|rcp|test|sonar|clean|replacep4java [-update] [-Dprop1=value1 -Dprop2=value2 ...]"
  echo
  echo "Reference to following environment variables (with example values):"
  echo "      WORKSPACE=parent_of_build_folder"
  echo "      MVN=/opt/apache-maven/bin/mvn"
  echo "      P4=/opt/perforce/client/bin/p4"
  echo "      VER=2011.2"
  echo "      CHANGE=377088"
  echo ""
}

failWithMessage () {
  echo $1
  echo
  exit 1
}

printSettings () {
  echo
  echo "Build P4ECLIPSE ..."
  echo
  echo "[Environment variables]"
  echo "  WORKSPACE=$WORKSPACE"
  echo "  MVN=$MVN"
  echo "  P4=$P4"
  echo "  VER=$VER"
  echo "  CHANGE=$CHANGE"
  echo "  MAVEN_OPTS=$MAVEN_OPTS"
  echo
  echo "[Arguments]"
  echo "  MODE=$MODE"
  echo "  UPDATE_VERSION=$UPDATE_VERSION"
  echo "  PROP=$PROP"
  echo
}

cleantest () {
  # clean the temporary files
  rm -rf /tmp/p4d*
  rm -rf /tmp/client*
  rm -f $WORKSPACE/build/p4eclipse_parent/jacoco.exec

  killall p4d

  if [ ${?} -ne 0 ]; then
     echo "p4d does not exist."
  fi

}

updateVersion () {
  cd $WORKSPACE/build/p4eclipse_parent
  $MVN -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:$TYCHO_VERSION:set-version -DnewVersion=$VER.$CHANGE-SNAPSHOT
}

buildUpdatesite () {
if [ "$SIGN_JAR" == "yes" ]; then
    echo "Build update site with jar signing ... "
    $MVN clean package -P p4update,signjar $PROP
else
    echo "Build update site without jar signing ... "
    $MVN clean package -P p4update $PROP
fi
}

buildRcp () {
  $MVN clean package -P p4update,p4rcp $PROP
}

test () {
  if [ "$OS" == "darwin" ]; then
     failWithMessage "Can not run test on MacOS, run it on Linux, please!"
  fi

  cleantest

  $MVN clean verify -fae -P p4test $PROP
}

testAlone () {
  if [ "$OS" == "darwin" ]; then
     failWithMessage "Can not run test on MacOS, run it on Linux, please!"
  fi

  cleantest

  buildUpdatesite

  cd $WORKSPACE;rm -rf site;mkdir site;cd site;  unzip ../build/p4eclipse_updatesite/target/p4eclipse-updatesite*.zip; export CHANGE=`$P4 -u ali -pperforce:1666 -Pali changes -m 1|sed 's/Change \([0-9]*\) on.*/\1/'`;cd ../build/p4eclipse_parent;
  $MVN clean verify -fae -P p4test $PROP -Dp4repo.url=file:$WORKSPACE/site
}

clean () {
  cleantest
  $MVN clean -P p4update,p4rcp,p4test $PROP
}

replacep4java () {
  $MVN -f replacep4java.xml process-resources $PROP
}

analyzeCode () {
  if [ "$OS" == "darwin" ]; then
     failWithMessage "Can not run code anaysis on MacOS, run it on Linux, please!"
  fi
  $MVN clean install -fae -P p4test,codeCoverage $PROP
  $MVN sonar:sonar -PcodeCoverage $PROP
}

coverage () {
  if [ "$OS" == "darwin" ]; then
     failWithMessage "Can not run code anaysis on MacOS, run it on Linux, please!"
  fi

  #-Dp4repo.url=file:$WORKSPACE/build/p4eclipse_updatesite/target/site
  cleantest
  $MVN clean install -fae -P p4update,p4test,codeCoverage $PROP
  $MVN sonar:sonar -PcodeCoverage $PROP
}

############ main entry ##############
export MAVEN_OPTS=$MAVEN_OPTS

if [ "$OS" == "darwin" ]; then
  PROP="$PROP -Djava.vendor.url='http://www.apple.com'"
  echo "Build on darwin..."
  echo "PROP=$PROP"
fi

if [ $# == 0 ]; then
   usage
   exit 1
fi

while [ "$1" != "" ]; do
    case $1 in
        -sign | --signjar )
                        SIGN_JAR="yes"
                        ;;
        -update | --updateversion )
                        UPDATE_VERSION="yes"
                        ;;
        updatesite | rcp | test | sonar | clean | replacep4java | testAlone)
                        MODE=$1
                        ;;
        -h | --help )   usage
                        exit 0
                        ;;
        -D* )
                        PROP="$PROP $1"
                        ;;
        * )             usage
                        exit 1
    esac
    shift
done

if [ -z "${MODE+xxx}" ]; then
  usage;
  failWithMessage "MODE is not defined! Valid Mode is updatesite | rcp | test | sonar";
fi

printSettings


if [ "$UPDATE_VERSION" == "yes" ]; then
    echo "Update version ... "
    updateVersion
fi

cd $WORKSPACE/build/p4eclipse_parent
if [ "$MODE" == "updatesite" ]; then
    buildUpdatesite
elif [ "$MODE" == "clean" ]; then
    clean
elif [ "$MODE" == "replacep4java" ]; then
    replacep4java
elif [ "$MODE" == "rcp" ]; then
    buildRcp
elif [ "$MODE" == "test" ] && [ "$OS" != "darwin" ]; then
    test
elif [ "$MODE" == "testAlone" ] && [ "$OS" != "darwin" ]; then
    testAlone
elif [ "$MODE" == "sonar" ] && [ "$OS" != "darwin" ]; then
    #analyzeCode
    coverage
else
    usage
    exit 1
fi
