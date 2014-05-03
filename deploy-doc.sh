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

runSuite "git clone https://github.com/owlike/genson.git tmp_website" "cd tmp_website" "git checkout gh-pages" "rm -R *"

runSuite "cp -R ../website/* ." "cp -R ../target/site/apidocs Documentation/Javadoc"

runSuite "git add ." "git commit -m \"Documentation Release\"" "git push origin gh-pages"

runSuite "cd .." "rm -R tmp_website"

