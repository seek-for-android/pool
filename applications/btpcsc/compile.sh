#!/bin/bash
gcc -shared -o libbtpcsc/libbtpcsc.so *.c *.h libbtpcsc/*.c libbtpcsc/*.h -lbluetooth -lpcsclite -I/usr/include/PCSC
gcc -o btpcsc_setup/btpcsc_setup *.c *.h btpcsc_setup/btpcsc_setup.c -lbluetooth
