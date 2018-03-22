#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/perfect-cell-point.sh perfect-cell-point.prop" \
           gold \
           cell-count-stat.csv \
           cell-count-traj.csv
