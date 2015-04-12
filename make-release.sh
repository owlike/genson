#!/usr/bin/env bash

set -e

# All global default vars used in this script
######################################################################################
BASE_DIR=$PWD
COMMAND="release"
ACTUAL_VERSION=$(sed -n 's|[ \t]*<version>\(.*\)</version>|\1|p' pom.xml|head -1)
RELEASE_VERSION=$(echo $ACTUAL_VERSION|sed 's/-SNAPSHOT//')
NEXT_VERSION=
BASE_BRANCH=$(git rev-parse --abbrev-ref HEAD)
DIRTY_SCM=false
######################################################################################

while [[ $# > 1 ]]; do
key="$1"

case $key in
    -c|--command)
      COMMAND="$2"
      ;;
    -v|--release-version)
      RELEASE_VERSION="$2"
      ;;
    -n|--next-version)
      NEXT_VERSION="$2"
      __MVN_DEV_VERSION="-DdevelopmentVersion=$2"
      ;;
    -d|--dirty-scm)
      DIRTY_SCM="$2"
      ;;
    *)
      echo "Unknown parameter $key"
      echo "Usage: ./make-release.sh [OPTION]
-c,--comand commandName
    possible values test|install|deploy|release|website-only
-v,--release-version versionNumber
    will be used to deploy the artifacts and make the release branch
-n,--next-version versionNumber
    will be used as the new version after the release
-d,--dirty-scm true/false
    false by default, all changes must be in sync with origin"
      exit 1
      ;;
esac
shift 2
done

#######################################################################################################################
#######################################################################################################################
#######################################################################################################################

function checkNoUncommitedChanges {
  if [ "$DIRTY_SCM" == "false" ]; then
    echo "Checking that there are no uncommited changes"
    git diff-index --quiet origin/$BASE_BRANCH --
    local RET=$?
    if [ $RET != 0 ]; then
      echo "You have uncommited changes, please commit and push to origin everything before deploying the doc."
      exit $RET;
    fi;
  fi;
}

function createScalaProject {
  local SCALA_VERSION=$1
  local SCALA_NEXT_VERSION=$2
  local SCALA_RANGE_VERSION="$SCALA_VERSION.0"
  local SCALA_PROJECT="genson-scala_$SCALA_VERSION"

  cp -R genson-scala $SCALA_PROJECT

  # Replacing the first occurrence of scala version definition in the properties and letting maven take care of the rest
  xmlstarlet edit -L -u "/project/properties/scala.version" -v $SCALA_VERSION $SCALA_PROJECT/pom.xml
  xmlstarlet edit -L -u "/project/properties/scala.range.version" -v "$SCALA_RANGE_VERSION" $SCALA_PROJECT/pom.xml

  # Need also to change the artifact id to include the scala version
  xmlstarlet edit -L -u "/project/artifactId" -v "$SCALA_PROJECT" $SCALA_PROJECT/pom.xml

  # Add this project to the parent pom
  sed -i "/<module>genson<\/module>/a <module>$SCALA_PROJECT</module>" pom.xml
}

# We don't care here about updating dependencies versions as we use the project version
# for dependencies between modules
function updateReleaseVersions {
  local PROJECT=$1
  local PROJECT_POM=$1/pom.xml

  xmlstarlet edit -L -u "/project/version" -v $RELEASE_VERSION $PROJECT_POM

  for MODULE in $(xmlstarlet sel -t -v "/project/modules/module" $PROJECT_POM); do
    # update here the parent reference version
    xmlstarlet edit -L -u "/project/parent/version" -v $RELEASE_VERSION "$PROJECT/$MODULE/pom.xml"

    updateReleaseVersions "$PROJECT/$MODULE"
  done;
}

function deployWebsite {
  mvn clean package -DskipTests

  # checkout and prepare gh-pages branch for the new generated doc
  git clone git@github.com:owlike/genson.git tmp_doc
  cd tmp_doc
  git checkout gh-pages
  rm -R *

  echo -e "latest_version: $RELEASE_VERSION" > _config-release.yml
  echo -e "production: true" >> _config-release.yml

  jekyll build --source ../website --destination . --config ../website/_config.yml,_config-release.yml

  cp -R ../genson/target/apidocs Documentation/Javadoc

  # So we can call it during release where we have generated the cross scala artifacts
  # but also without it (not sure we should allow this)
  if [ ! -d ../genson-scala_2.10 ]; then
    local __GENSON_SCALA_DIR=../genson-scala
  else
    local __GENSON_SCALA_DIR=../genson-scala_2.10
  fi

  cp -R $__GENSON_SCALA_DIR/target/apidocs Documentation/Scaladoc

  git add -A .
  git commit -m "Documentation Release $RELEASE_VERSION"
  git push origin gh-pages

  cd ..
  rm -Rf tmp_doc
}

function prepareFromLocal {
  if [ ! -d /tmp/genson_release ]; then
    rm -Rf /tmp/genson_release
  fi

  mkdir /tmp/genson_release

  cp -R . /tmp/genson_release
  cd /tmp/genson_release
}

function prepareFromRemote {
  if [ ! -d /tmp/genson_release ]; then
    rm -Rf /tmp/genson_release
  fi

  git clone git@github.com:owlike/genson.git /tmp/genson_release
  cd /tmp/genson_release
}

function prepareProjects {
  sed -i "s/<module>genson-scala<\/module>//" pom.xml

  createScalaProject 2.10
  createScalaProject 2.11
}

#######################################################################################################################
#######################################################################################################################
#######################################################################################################################

case "$COMMAND" in
"test")
    prepareFromLocal
    git checkout $BASE_BRANCH
    prepareProjects

    mvn test
    ;;
"install")
    echo "Will install current version"
    prepareFromLocal
    git checkout $BASE_BRANCH
    prepareProjects

    mvn install
    ;;
"deploy")
    echo "Will deploy current version"
    prepareFromLocal
    git checkout $BASE_BRANCH
    prepareProjects

    mvn deploy
    ;;
"release")
    echo "Will run full release including: release branch, deploy artifacts and website, update current branch to next version"

    checkNoUncommitedChanges
    prepareFromRemote

    # We want to make the release from the initial branch, here we are in the working copy, not the original directory
    git checkout $BASE_BRANCH

    # prepare the branch here, but we add nothing, just make it so people have access to the code used to produce
    # this release and can use this branch to do internal changes/deploiements
    git branch "genson-$RELEASE_VERSION"


    # Update the versions on the base branch
    mvn release:update-versions -DaddSchema=false $__MVN_DEV_VERSION
    git add pom.xml **/pom.xml
    __NEXT_VERSION=$(sed -n 's|[ \t]*<version>\(.*\)</version>|\1|p' pom.xml|head -1)
    git commit --allow-empty -m "[Release] - $RELEASE_VERSION, update from dev version $ACTUAL_VERSION to $__NEXT_VERSION"

    # Now move on to releasing the current version on the release branch
    git checkout "genson-$RELEASE_VERSION"
    git commit --allow-empty -m "Release $RELEASE_VERSION"

    prepareProjects
    updateReleaseVersions .

    mvn deploy -DperformRelease=true

    # Releasing the artifacts succeeded, we can now push our changes to the remote repo
    git push origin "genson-$RELEASE_VERSION"

    # We are already on the release branch, directly deploy the website
    deployWebsite

    # Only now push the changes to the original branches and update the clone from which we initiated the release
    git checkout $BASE_BRANCH
    git push origin $BASE_BRANCH

    # Until here we are supposed to be able to easily revert things as we still have our unchanged clone
    cd $BASE_DIR
    git pull origin $BASE_BRANCH
    ;;
"website-only")
    echo "Deploy website"
    prepareFromLocal
    git checkout $BASE_BRANCH
    prepareProjects
    # Updating the versions so we can catch up website mistakes after a release has been made
    updateReleaseVersions .
    deployWebsite
    ;;
*)
    echo "Unknown command: $COMMAND"
    exit 1;
esac

# Cleaning after us (in case of an error we want the src to remain so we can debug things)
rm -Rf /tmp/genson_release
