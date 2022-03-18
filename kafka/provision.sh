#!/bin/bash

update_file_key_value_pairs() {
  file="${1?:'file is a required parameter'}"
  key_value_pairs="${2?:'key_value_pairs is a required parameter'}"
  delimiter='~'

  for key in "${!key_value_pairs[@]}"; do
    if [ "${key}" = '0' ]; then continue; fi
    echo "s${delimiter}^${key}=.*${delimiter}${key}=${key_value_pairs[${key}]}${delimiter}"
    sed --in-place --regexp-extended \
        "s${delimiter}^${key}=.*${delimiter}${key}=${key_value_pairs[${key}]}${delimiter}" \
        "${file}"
  done
}

./bin/kafka-storage.sh format \
  -t "$(./bin/kafka-storage.sh random-uuid)" \
  -c ./config/kraft/server.properties \

declare -A key_value_pairs=(
  ['listeners']='LOCALHOST://:9091,PLAINTEXT://kafka:9092,CONTROLLER://:9093'
  ['advertised.listeners']='LOCALHOST://:9091,PLAINTEXT://kafka:9092'
  ['listener.security.protocol.map']='LOCALHOST:PLAINTEXT,PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT'
)

update_file_key_value_pairs './config/kraft/server.properties' key_value_pairs