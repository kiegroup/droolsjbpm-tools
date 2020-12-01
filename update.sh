#!/bin/bash -e

mvnVersionsUpdateParent() {
    mvn -B -N -e versions:update-parent -Dfull\
     -DparentVersion="[7.48.0-SNAPSHOT]" -DallowSnapshots=true -DgenerateBackupPoms=false
}

mvnVersionsUpdateChildModules() {
    mvn -B -N -e versions:update-child-modules -Dfull\
     -DallowSnapshots=true -DgenerateBackupPoms=false
}

cd drools-eclipse
mvn -B -Dfull tycho-versions:set-version -DnewVersion=7.48.0-SNAPSHOT
returnCode=$?
versionToUse=7.48.0-SNAPSHOT
if [[ $newVersion == *-SNAPSHOT ]]; the
   versionToUse=`sed "s/-SNAPSHOT/.qualifier/" <<< $newVersion`
fi
sed -i "s/source_[^\"]*/source_$versionToUse/" org.drools.updatesite/category.xml
sed -i "s/version=\"[^\"]*\">/version=\"$versionToUse\">/" org.drools.updatesite/category.xml
cd ..            
if [ $returnCode == 0 ]; then
   mvn -B -N -s $settingsXmlFile clean install
   mvnVersionsUpdateParent
   # workaround for http://jira.codehaus.org/browse/MVERSIONS-161
   mvn -B -N clean install -DskipTests
   cd drools-eclipse
   mvnVersionsUpdateParent
   cd ..
   mvnVersionsUpdateChildModules
fi
