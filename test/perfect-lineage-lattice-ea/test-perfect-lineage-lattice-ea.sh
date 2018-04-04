#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/lineage-lattice-ea.sh perfect-lineage-lattice.prop" \
           gold \
           cell-count-stat.csv \
           cell-count-traj.csv \
           lineage-count-stat.csv \
           lineage-count-traj.csv \
           final-comp-coord.csv
