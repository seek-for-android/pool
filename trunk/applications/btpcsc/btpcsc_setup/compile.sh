#!/bin/bash
gcc -o btpcsc_setup *.c *.h -lbluetooth -lpcsclite -I/usr/include/PCSC
