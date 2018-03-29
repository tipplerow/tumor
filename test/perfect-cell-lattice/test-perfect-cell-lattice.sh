#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/perfect-cell-lattice.sh perfect-cell-lattice.prop" \
           gold \
           cell-count-stat.csv \
           cell-count-traj.csv \
           final-comp-coord.csv
