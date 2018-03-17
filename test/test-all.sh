#!/bin/sh
########################################################################
# Runs all application regression tests.
########################################################################

TEST_DIR=$(cd `dirname $0`; pwd)

cd ${TEST_DIR}/perfect-cell-point
./test-perfect-cell-point.sh

exit 0
