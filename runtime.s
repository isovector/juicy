section .text

; Allocates eax bytes of memory. Pointer to allocated memory returned in eax.
    global __malloc
__malloc:
    push eax
    mov eax, 45  ; sys_brk system call
    mov ebx, 0   ; 0 bytes - query current brk
    int 0x80
    pop ebx
    push eax
    add ebx, eax ; move brk ahead by number of bytes requested
    mov eax, 45  ; sys_brk system call
    int 0x80
    pop ebx
    cmp eax, 0   ; on error, exit with code 22
    jne ok
    mov eax, 22
    call __debexit
ok:
    mov eax, ebx
    ret

; Debugging exit: ends the process, returning the value of
; eax as the exit code.
    global __debexit
__debexit:
    mov ebx, eax
    mov eax, 1   ; sys_exit system call
    int 0x80

; Exceptional exit: ends the process with exit code 13.
; Call this in cases where the Joos code would throw an exception.
    global __exception
__exception:
    mov eax, 1   ; sys_exit system call
    mov ebx, 13
    int 0x80

; Implementation of java.io.OutputStream.nativeWrite method.
; Outputs the low-order byte of eax to standard output.
    global NATIVEjava.io.OutputStream.nativeWrite
NATIVEjava.io.OutputStream.nativeWrite:
    mov [char], al ; save the low order byte in memory
    mov eax, 4     ; sys_write system call
    mov ecx, char  ; address of bytes to write
    mov ebx, 1     ; stdout
    mov edx, 1     ; number of bytes to write
    int 0x80
    mov eax, 0     ; return 0
    ret

section .data

char:
    dd 0
