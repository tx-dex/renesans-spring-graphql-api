#!/usr/bin/env bash
# small helper file to create timestamped migration file

echo ""
if [ -z "$1" ]; then
    echo "Usage: ./create-migration [name]"
    echo ""
    exit 1
fi

NAME=$1
TIMESTAMP=`date "+%Y%m%d%H%M%S"`
TIMESTAMP_HUMAN=`date "+%d.%m.%Y %H:%M:%S"`
TARGET_DIR="./src/main/resources/db/migration/postgresql"

FILENAME="${TIMESTAMP}__${NAME}.sql"
MIGRATION="V${FILENAME}"

echo "Creating migration file .. ${MIGRATION}"
`echo "-- migration ${TIMESTAMP_HUMAN} / ${NAME}" >> ${TARGET_DIR}/${MIGRATION}`
`echo "SET SCHEMA 'dataserver';" >> ${TARGET_DIR}/${MIGRATION}`
`echo "" >> ${TARGET_DIR}/${MIGRATION}`
echo ""
