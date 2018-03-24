#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/perfect-deme-lattice.sh perfect-deme-lattice.prop" \
           gold \
           cell-count-stat.csv \
           cell-count-traj.csv \
           deme-count-stat.csv \
           deme-count-traj.csv
