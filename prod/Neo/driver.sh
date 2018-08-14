#!/bin/sh
########################################################################
# Driver script for neoantigen simulation.
########################################################################

PROP_FILE=neoantigen-prod.prop

if [ $# -ne 2 ]
then
    echo "Usage:" `basename $0` "SELECTION_COEFF NEOANTIGEN_RATE"
    exit 1
fi

SELECTION_COEFF=$1
shift

NEOANTIGEN_RATE=$1
shift

TRIAL_BEGIN=0
TRIAL_END=25

REPORT_BASE=`dirname $0`
cd $REPORT_BASE

TrialIndex=$TRIAL_BEGIN

while [ $TrialIndex -lt $TRIAL_END ]
do
    SubDir=$(printf "S%.2f/NR%s/Trial%02d" $SELECTION_COEFF $NEOANTIGEN_RATE $TrialIndex)

    if [ ! -d $SubDir ]
    then
        mkdir -p $SubDir
    fi

    tumor-driver.sh -Xmx96G \
                    -Djam.app.reportDir=${REPORT_BASE}/$SubDir \
                    -Dtumor.driver.trialIndex=$TrialIndex \
                    -Dtumor.mutation.selectionCoeff=$SELECTION_COEFF \
                    -Dtumor.mutation.neoantigenMeanRate=$NEOANTIGEN_RATE \
                    $PROP_FILE

    TrialIndex=$(expr $TrialIndex + 1)
done

exit 0
