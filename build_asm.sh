#!/usr/bin/env bash

cd output
g++ -m32 -g -c gdb.types.cc
for f in *.s; do
    nasm -O1 -f elf -g -F dwarf $f
done

ld -melf_i386 -o main *.o;
./main
echo $?

