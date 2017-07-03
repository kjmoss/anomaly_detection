#!/bin/bash

#python ./src/process_log.py ./log_input/batch_log.json ./log_input/stream_log.json ./log_output/flagged_purchases.json

javac -cp ./src ./src/shopping_network/Detector.java
java -cp ./src shopping_network.Detector ./log_input ./log_output
