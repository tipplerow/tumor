#!/bin/sh
########################################################################
# Runs all application regression tests.
########################################################################

TEST_DIR=$(cd `dirname $0`; pwd)

cd ${TEST_DIR}/point-perfect
./test-point-perfect.sh

exit 0
