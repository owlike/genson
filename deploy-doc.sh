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

runSuite "mvn javadoc:javadoc" "rm -Rf website/Documentation/Javadoc/*" "cp -R target/site/apidocs website/Documentation/Javadoc"




