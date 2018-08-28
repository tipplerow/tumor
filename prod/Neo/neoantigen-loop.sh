#!/bin/sh
########################################################################
# Submits a series of "neoantigen-qsub.sh" simulations.
#
# Usage: neoantigen-loop.sh \
#        TRIAL_START \
#        TRIAL_END \
#        SELECTION_COEFF \
#        NEOANTIGEN_RATE
#        
########################################################################

if [ $# -ne 4 ]
then
    echo "Usage:" `basename $0` "TRIAL_START TRIAL_END SELECTION_COEFF NEOANTIGEN_RATE"
    exit 1
fi

TRIAL_START=$1
shift

TRIAL_END=$1
shift

TrialIndex=$TRIAL_START

while [ $TrialIndex -le $TRIAL_END ]
do
    qsub neoantigen-qsub.sh $TrialIndex $@
    TrialIndex=$(expr $TrialIndex + 1)
done

exit 0
