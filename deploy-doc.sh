#!/bin/sh

niceMessage() {
echo "========================================================================================================================\n\
\t$1\n\
========================================================================================================================"
}

runCommand() {
    eval "$1"
    ret_code=$?

    if [ $ret_code != 0 ]; then
        niceMessage "$2"
        exit $ret_code
    fi
}

runSuite() {
    for cmd in "$@"
    do
        runCommand "$cmd" "Failed to execute command: $cmd"
    done
}

runCommand "git diff-index --quiet origin/HEAD --" "You have uncommited changes, please commit and push to origin everything before deploying the doc.";

runSuite "mvn clean javadoc:javadoc"

version=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')

runSuite "git clone https://github.com/owlike/genson.git tmp_website" "cd tmp_website" "git checkout gh-pages" "rm -R *"

echo -e "latest_version: $version \nproduction: true" > _config-release.yml

runSuite "jekyll build --source ../website --destination . --config ../website/_config.yml,_config-release.yml"

rm _config-release.yml

runSuite "cp -R ../genson/target/site/apidocs Documentation/Javadoc" "cp -R ../genson-scala/target/apidocs Documentation/Scaladoc"

runSuite "git add -u ." "git add ." "git commit -m \"Documentation Release $version\"" "git push origin gh-pages"

runSuite "cd .." "rm -Rf tmp_website"
