#!/usr/bin/env bash

# script to upgrade the version of droolsjbpm-tools
# parameters:
# $1 = new droolsjbpm-tools version

initializeScriptDir() {
 cd `dirname $0`
 scriptDir=`pwd -P`
}

mvnVersionsUpdateParent() {
    mvn -B -N -e -s $settingsXmlFile versions:update-parent -Dfull\
     -DparentVersion="[$newVersion]" -DallowSnapshots=true -DgenerateBackupPoms=false
}

mvnVersionsUpdateChildModules() {
    mvn -B -N -e -s $settingsXmlFile versions:update-child-modules -Dfull\
     -DallowSnapshots=true -DgenerateBackupPoms=false
}

initializeScriptDir
echo "scriptDir: " $scriptDir

if [ $# != 1 ] ; then
    echo
    echo "Usage:"
    echo "  $0 newVersion"
    echo "For example:"
    echo "  $0 7.48.0.Final"
    echo
    exit 1
fi

newVersion=$1

# *** upgrade version of droolsjbpm-tools ***

workingDir="$scriptDir/../"
cd $workingDir
baseDir=$(pwd)
settingsXmlFile="$scriptDir/settings.xml"
echo "settingsXmlFile: " $settingsXmlFile

cd drools-eclipse
mvn -B -s $settingsXmlFile -Dfull tycho-versions:set-version -DnewVersion=$newVersion
returnCode=$?
# replace the leftovers not covered by the tycho plugin (bug?)
versionToUse=$newVersion

sed -i "s/source_[^\"]*/source_$versionToUse/" org.drools.updatesite/category.xml
sed -i "s/version=\"[^\"]*\">/version=\"$versionToUse\">/" org.drools.updatesite/category.xml

cd ..

if [ $returnCode == 0 ]; then
    mvn -B -N -s $settingsXmlFile clean install
    mvnVersionsUpdateParent
    # workaround for http://jira.codehaus.org/browse/MVERSIONS-161
    mvn -B -N -s $settingsXmlFile clean install -DskipTests
    cd drools-eclipse
    mvnVersionsUpdateParent
    cd ..
    mvnVersionsUpdateChildModules
    returnCode=$?
fi
