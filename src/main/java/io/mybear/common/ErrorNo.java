package io.mybear.common;

public class ErrorNo {
    // Operation not permitted
    public static final int EPERM = 1;

    // No such file or directory
    public static final int ENOFILE = 2;

    public static final int ENOENT = 2;

    // No such process
    public static final int ESRCH = 3;

    // Interrupted function call
    public static final int EINTR = 4;

    // Input/output error
    public static final int EIO = 5;

    // No such device or address
    public static final int ENXIO = 6;

    // Arg list too long
    public static final int E2BIG = 7;

    // Exec format error
    public static final int ENOEXEC = 8;

    // Bad file descriptor
    public static final int EBADF = 9;

    // No child processes
    public static final int ECHILD = 10;

    // Resource temporarily unavailable
    public static final int EAGAIN = 11;

    // Not enough space
    public static final int ENOMEM = 12;

    // Permission denied
    public static final int EACCES = 13;

    // Bad address
    public static final int EFAULT = 14;

    // 15 - Unknown Error

    // strerror reports "Resource device"
    public static final int EBUSY = 16;

    // File exists
    public static final int EEXIST = 17;

    // Improper link (cross-device link?)
    public static final int EXDEV = 18;

    // No such device
    public static final int ENODEV = 19;

    // Not a directory
    public static final int ENOTDIR = 20;

    // Is a directory
    public static final int EISDIR = 21;

    // Invalid argument
    public static final int EINVAL = 22;

    // Too many open files in system
    public static final int ENFILE = 23;

    // Too many open files
    public static final int EMFILE = 24;

    // Inappropriate I/O control operation
    public static final int ENOTTY = 25;

    /* 26 - Unknown Error */

    // File too large
    public static final int EFBIG = 27;

    // No space left on device
    public static final int ENOSPC = 28;

    // Invalid seek (seek on a pipe?)
    public static final int ESPIPE = 29;

    // Read-only file system
    public static final int EROFS = 30;

    // Too many links
    public static final int EMLINK = 31;

    // Broken pipe
    public static final int EPIPE = 32;

    // Domain error (math functions)
    public static final int EDOM = 33;

    // Result too large (possibly too small)
    public static final int ERANGE = 34;

    /* 35 - Unknown Error */

    // Resource deadlock avoided (non-Cyg)
    public static final int EDEADLOCK = 36;

    public static final int EDEADLK = 36;

    /* 37 - Unknown Error */

    // Filename too long (91 in Cyg?)
    public static final int ENAMETOOLONG = 38;

    // No locks available (46 in Cyg?)
    public static final int ENOLCK = 39;

    // Function not implemented (88 in Cyg?)
    public static final int ENOSYS = 40;

    // Directory not empty (90 in Cyg?)
    public static final int ENOTEMPTY = 41;

    // Illegal byte sequence
    public static final int EILSEQ = 42;
}
