// Filename: LinuxIPC.java

import java.nio.ByteBuffer;

/** This class implements the basic Linux IPC functions, using a JNI library.
  * @author George Wells
  * @version 1.0 (17 Novermber 2008)
  */
public class LinuxNIPC
  { /**  Create key if key does not exist.
      * Must remain consistent with <CODE>/usr/include/linux/ipc.h</CODE>.
      */
    public static final int IPC_CREAT = 01000;
    /** Fail if key exists.
      * Must remain consistent with <CODE>/usr/include/linux/ipc.h</CODE>.
      */
    public static final int IPC_EXCL = 02000;
    /** Return error on wait.
      * Must remain consistent with <CODE>/usr/include/linux/ipc.h</CODE>.
      */
    public static final int IPC_NOWAIT = 04000;
    /** No error if message is too big.
      * Must remain consistent with <CODE>/usr/include/linux/msg.h</CODE>.
      */
    public static final int MSG_NOERROR = 010000;
    /** Receive any message except of specified type.
      */
    public static final int MSG_EXCEPT = 020000;
    /** Removes the queue from the kernel.
      */
    public static final int IPC_RMID = 0;
    /** Sets the value of the ipc_perm member of the msqid_ds structure for a
      * queue. Takes the values from the buf argument.
      */
    public static final int IPC_SET = 1;
    /** Retrieves the msqid_ds structure for a queue, and stores it in the
      * address of the buf argument.
      */
    public static final int IPC_STAT = 2;
    
    /** Shared memory segment will be mapped in as read-only. 
      */
    public static final int SHM_RDONLY = 010000;
    /** Force a shared memory segment address to be page-aligned (rounds down
      * to the nearest page size).
      */
    public static final int SHM_RND = 020000;
  
    /** Linux error number, if any.
      */
    private int errnum;
    
    /** Error: Try again */
    public static final int EAGAIN = 11;

    /** Calls <CODE>mknod</CODE> to create a named FIFO pipe.
      * This call has the form:<br>
      * <CODE>mknod(name, S_IFIFO|perms, 0);</CODE><br>
      * where <CODE>name</CODE> and <CODE>perms</CODE> are the parameters
      * passed to this method.
      * @param name The name of the FIFO pipe.
      * @param perms Used to set the permissions for the FIFO pipe.
      * @returns 0 if successful, -1 if an error occurs (errnum has the Linux
      *   error code).
      */
    public native int mkfifo (String name, int perms);
    
    /** Create a new IPC key value.  See the man page for <CODE>ftok()</CODE>
      * for more details.
      * @param pathname A file name used to form the key.
      * @param proj A "project identifier" used to form the key.
      * @returns A value that can be used as a key for the IPC methods, or -1 if
      *   unsuccessful (errnum is set to the cause of the error).
      */
    public native int ftok (String pathname, char proj);
    
    /** Create a new message queue.  See the man page for <CODE>msgget()</CODE>
      * for more details.
      * @param key An identifier to be used for this queue.
      * @param flags The flags to be used for this queue (IPC_CREAT or
      * IPC_EXCL).
      */
    public native int msgget (int key, int flags);
    
    /** Send a message using a message queue.  See the man page for
      * <CODE>msgsnd()</CODE> for more details.
      * @param msgqid The message queue identifier (obtained from
      * <CODE>msgget()</CODE>).
      * @param type The message type.
      * @param buffer the java.nio ByteBuffer where the message will be copied from.
      * @param length The size of the message.  Only this number of bytes from
      * msg will be sent.  If the size is negative, all of msg will be sent.
      * @param flags The flags to be used for this queue (IPC_NOWAIT).
      * @returns 
      */
    public native int msgsnd (int msgqid, int type, ByteBuffer buffer, int length, int flags);
    
    
    /** Receive a message using a message queue.  See the man page for
      * <CODE>msgrcv()</CODE> for more details.
      * @param msgqid The message queue identifier (obtained from
      * <CODE>msgget()</CODE>).
      * @param type The message type. If type is 0, the first message on the
      * queue is received. If type is greater than 0, the first message of
      * type type is received. If type is less than 0, the first message
      * of the lowest type that is less than or equal to the absolute value
      * of type is received. 
      * @param buffer the java.nio ByteBuffer where the message will be copied to.
      * @param length The size of the message.  The received message is
      * truncated to msgsz bytes if it is larger than msgsz and
      * (msgflg & MSG_NOERROR) is non-zero. The truncated part of the message
      * is lost, and no indication of the truncation is given to the calling
      * process.
      * @param flags The flags to be used for this queue (IPC_NOWAIT, or
      *   MSG_NOERROR).
      * @returns If successful, the number of bytes actually placed into msg.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int msgrcv (int msgqid, int type, ByteBuffer buffer, int length, int flags);

    /** Remove an IPC message queue.  See the man page for
      * <CODE>msgctl()</CODE> for more details.
      * @param msgqid The message queue identifier (obtained from
      * <CODE>msgget()</CODE>).
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int msgRmid (int msgqid);

    /** Get error message corresponding to error code.  This simply calls the
      * C strerror function.
      * @param errnum Error code (as defined in errno.h).
      * @returns String representation of error code.
      */
    public native String strerror (int errnum);

    /** Get the error number returned by the last Linux call that encountered
      * an error.
      * This variable is only set when an error occurs (or when a Java program
      * calls setErrnum explicitly), so is not necessarily the result of the
      * last IPC call.
      * @returns The current value of the errnum variable.
      */
    public int getErrnum ()
      { return errnum;
      } // getErrnum

    /** Set the error number.  This method overwrites whatever value is
      * currently contained in the errnum variable.
      * @param errnum The new value for the errnum variable.
      */
    public void setErrnum (int errnum)
      { this.errnum = errnum;
      } // setErrnum

    static
      { System.loadLibrary("LinuxNIPC");
      } // static block

  } // class LinuxIPC
