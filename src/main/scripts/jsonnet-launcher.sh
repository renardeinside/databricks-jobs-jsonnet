#!/bin/bash

echo "Generating jsonnet configurations to directory $1 from file $2 with arguments ${@:3}"
jsonnet -m $1 $2 "${@:3}"