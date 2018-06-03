#!/bin/sh
########################################################################
# Runs all application regression tests.
########################################################################

TEST_DIR=$(cd `dirname $0`; pwd)

cd ${TEST_DIR}/point-cell-neutral
./test-point-cell-neutral.sh || exit $?

cd ${TEST_DIR}/point-cell-perfect
./test-point-cell-perfect.sh || exit $?

cd ${TEST_DIR}/point-deme-perfect
./test-point-deme-perfect.sh || exit $?

cd ${TEST_DIR}/point-lineage-perfect
./test-point-lineage-perfect.sh || exit $?

exit 0

cd ${TEST_DIR}/perfect-cell-lattice
./test-perfect-cell-lattice.sh || exit $?

cd ${TEST_DIR}/perfect-cell-point
./test-perfect-cell-point.sh || exit $?

cd ${TEST_DIR}/perfect-deme-lattice
./test-perfect-deme-lattice.sh || exit $?

cd ${TEST_DIR}/perfect-deme-point
./test-perfect-deme-point.sh || exit $?

cd ${TEST_DIR}/perfect-lineage-lattice
./test-perfect-lineage-lattice.sh || exit $?

cd ${TEST_DIR}/perfect-lineage-lattice-ea
./test-perfect-lineage-lattice-ea.sh || exit $?

cd ${TEST_DIR}/perfect-lineage-point
./test-perfect-lineage-point.sh || exit $?

exit 0
