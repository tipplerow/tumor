#!/bin/sh
########################################################################
# Runs all application regression tests.
########################################################################

TEST_DIR=$(cd `dirname $0`; pwd)

cd ${TEST_DIR}/perfect-cell-lattice
./test-perfect-cell-lattice.sh || exit $?

cd ${TEST_DIR}/perfect-cell-point
./test-perfect-cell-point.sh || exit $?

cd ${TEST_DIR}/perfect-lineage-lattice
./test-perfect-lineage-lattice.sh || exit $?

cd ${TEST_DIR}/perfect-lineage-lattice-ea
./test-perfect-lineage-lattice-ea.sh || exit $?

cd ${TEST_DIR}/perfect-lineage-point
./test-perfect-lineage-point.sh || exit $?

exit 0
