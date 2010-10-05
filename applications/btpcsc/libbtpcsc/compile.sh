#!/bin/bash
gcc -shared -o libbtpcsc.so *.c *.h -lbluetooth -lpcsclite -I/usr/include/PCSC
