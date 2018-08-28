#!/bin/sh
########################################################################
# Execute one trial in the neoantigen simulation.
#
# Usage: qsub neoantigen-qsub.sh \
#             TRIAL_INDEX \
#             SELECTION_COEFF \
#             NEOANTIGEN_RATE
#
########################################################################

#SBATCH --job-name="Neo"
#SBATCH --nodes=1
#SBATCH --ntasks=1

#SBATCH --cpus-per-task=2
#SBATCH --mem=32G
#SBATCH --time=25:00:00

#SBATCH --mail-user=jsshaff@stanford.edu
#SBATCH --mail-type=FAIL

#SBATCH --workdir=/N/users/jsshaff/SimWork/Neo

# --------------------------------------------------------------------

if [ $# -ne 3 ]
then
    echo "Usage:" `basename $0` "TRIAL_INDEX SELECTION_COEFF NEOANTIGEN_RATE"
    exit 1
fi

. $HOME/.bash_profile

if [ -z "$JAM_HOME" ]
then
    echo "Environment variable JAM_HOME is not set; exiting."
    exit 1
fi

if [ -z "$JAVA_HOME" ]
then
    echo "Environment variable JAVA_HOME is not set; exiting."
    exit 1
fi

if [ -z "$TUMOR_HOME" ]
then
    echo "Environment variable TUMOR_HOME is not set; exiting."
    exit 1
fi

WORK_DIR=/N/users/jsshaff/SimWork/Neo
PROP_FILE=${WORK_DIR}/neoantigen-prod.prop
TUMOR_DRIVER=${TUMOR_HOME}/bin/tumor-driver.sh

TRIAL_INDEX=$1
shift

SELECTION_COEFF=$1
shift

NEOANTIGEN_RATE=$1
shift

REPORT_DIR=$(printf "%s/S%.3f/NR%.5f/Trial%02d" $WORK_DIR $SELECTION_COEFF $NEOANTIGEN_RATE $TRIAL_INDEX)

if [ ! -d $REPORT_DIR ]
then
    mkdir -p $REPORT_DIR
fi

tumor-driver.sh -Xmx32G \
    -Djam.app.reportDir=$REPORT_DIR \
    -Dtumor.driver.trialIndex=$TRIAL_INDEX \
    -Dtumor.mutation.selectionCoeff=$SELECTION_COEFF \
    -Dtumor.mutation.neoantigenMeanRate=$NEOANTIGEN_RATE \
    $PROP_FILE

exit 0
