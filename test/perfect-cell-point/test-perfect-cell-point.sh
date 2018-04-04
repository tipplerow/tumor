#!/bin/sh

cd `dirname $0`

${JAM_HOME}/bin/jam-test.sh \
           "${TUMOR_HOME}/bin/cell-point.sh perfect-cell-point.prop" \
           gold \
           cell-count-stat.csv \
           cell-count-traj.csv
