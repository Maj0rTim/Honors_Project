// Filename: LinuxIPC.java

/** This class implements the basic Linux IPC functions, using a JNI library.
  * @author George Wells
  * @version 1.0 (17 Novermber 2008)
  */
public class LinuxIPC
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
      * @param msgflg The flags to be used for this queue (IPC_CREAT or
      * IPC_EXCL).
      */
    public native int msgget (int key, int msgflg);
    
    /** Send a message using a message queue.  See the man page for
      * <CODE>msgsnd()</CODE> for more details.
      * @param msgqid The message queue identifier (obtained from
      * <CODE>msgget()</CODE>).
      * @param type The message type.
      * @param msg The message to be sent.
      * @param msgsz The size of the message.  Only this number of bytes from
      * msg will be sent.  If the size is negative, all of msg will be sent.
      * @param msgflg The flags to be used for this queue (IPC_NOWAIT).
      * @returns 
      */
    public native int msgsnd (int msqid, int type, byte[] msg, int msgsz, int msgflg);
    
    
    /** Receive a message using a message queue.  See the man page for
      * <CODE>msgrcv()</CODE> for more details.
      * @param msgqid The message queue identifier (obtained from
      * <CODE>msgget()</CODE>).
      * @param msg The message received.
      * @param msgsz The size of the message.  The received message is
      * truncated to msgsz bytes if it is larger than msgsz and
      * (msgflg & MSG_NOERROR) is non-zero. The truncated part of the message
      * is lost, and no indication of the truncation is given to the calling
      * process.
      * @param type The message type. If type is 0, the first message on the
      * queue is received. If type is greater than 0, the first message of
      * type type is received. If type is less than 0, the first message
      * of the lowest type that is less than or equal to the absolute value
      * of type is received. 
      * @param msgflg The flags to be used for this queue (IPC_NOWAIT, or
      *   MSG_NOERROR).
      * @returns If successful, the number of bytes actually placed into msg.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int msgrcv (int msgqid, byte[] msg, int msgsz, int type, int msgflg);
    
    /** Control a message queue.  See the man page for
      * <CODE>msgctl()</CODE> for more details.
      * @param msgqid The message queue identifier (obtained from
      * <CODE>msgget()</CODE>).
      * @param cmd The command to be performed (IPC_STAT, IPC_SET, or
      *   IPC_RMID).
      * @param buf The data structure used for set/stat calls.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
    public native int msgctl (int msgqid, int cmd, Msqid_ds buf);
      */



    /** Remove an IPC message queue.  See the man page for
      * <CODE>msgctl()</CODE> for more details.
      * @param msgqid The message queue identifier (obtained from
      * <CODE>msgget()</CODE>).
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int msgRmid (int msgqid);

    /** Create a new sempahore set.  See the man page for
      * <CODE>semget()</CODE> for more details.
      * @param key An identifier to be used for this set.
      * @param n_sems The number of sempahores in this set.
      * @param semflg The flags to be used for this set (IPC_CREAT or
      * IPC_EXCL).
      * @returns Semaphore set identifier on success.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int semget (int key, int n_sems, int semflg);

    /** Perform a sempahore operation.  See the man page for <CODE>semop()</CODE>
      * for more details.
      * @param semid The semaphore identifier for this set.
      * @param sops The sempahore operations to be performed.
      * @param nsops The number of operations in sops to be performed.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int semop (int semid, Sembuf[] sops, int nsops);

    public int semop (int semid, Sembuf[] sops)
      { return semop(semid, sops, sops.length);
      } // semop
      
    /** Perform a sempahore operation.  See the man page for
      * <CODE>semtimedop()</CODE> for more details.
      * @param semid The semaphore identifier for this set.
      * @param sops The sempahore operations to be performed.
      * @param nsops The number of operations in sops to be performed.
      * @param timeout_s The timeout seconds.
      * @param timeout_ns The timeout nanoseconds.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int semtimedop (int semid, Sembuf[] sops, int nsops, long timeout_s, long timeout_ns); 

    /** Remove an IPC semaphore set.  See the man page for
      * <CODE>semctl()</CODE> for more details.
      * @param semid The semaphore identifier (obtained from
      * <CODE>semget()</CODE>).
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int semRmid (int semid);

    /** Get the current value of a semaphore in a semaphore set.  See the man
      *  page for <CODE>semctl()</CODE> for more details.
      * @param semid The semaphore identifier (obtained from
      * <CODE>semget()</CODE>).
      * @param semnum The number of the semaphore in the set.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int semGetVal (int semid, int semNum);

    /** Set the current value of a semaphore in a semaphore set.  See the man
      *  page for <CODE>semctl()</CODE> for more details.
      * @param semid The semaphore identifier (obtained from
      * <CODE>semget()</CODE>).
      * @param semnum The number of the semaphore in the set.
      * @param val The new value of the semaphore.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int semSetVal (int semid, int semNum, int val);

    /** Get the current value of a semaphore in a semaphore set.  See the man
      *  page for <CODE>semctl()</CODE> for more details.
      * @param semid The semaphore identifier (obtained from
      * <CODE>semget()</CODE>).
      * @param semnum The number of the semaphore in the set.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int semGetNCnt (int semid, int semNum);
    
    
    /** Create a new shared memory segment.  See the man page for
      * <CODE>shmget()</CODE> for more details.
      * @param key An identifier to be used for this segment.
      * @param size The size of this segment.
      * @param shmflg The flags to be used for this segment (IPC_CREAT
      * or IPC_EXCL).
      * @returns Shared memory segment identifier on success.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int shmget (int key, int size, int shmflg);
    
    /** Access a shared memory segment.  See the man page for
      * <CODE>shmat()</CODE> for more details.
      * @param shmid The identifier for this segment.
      * @param shmaddr The address requested for this segment (normally,
      *   one should simply use 0).
      * @param shmflg The flags to be used for this segment (SHM_RND or
      *   SHM_RDONLY).
      * @returns The address at which segment was attached to the process.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int shmat (int shmid, int shmaddr, int shmflg);
    
    /** Detach a shared memory segment.  See the man page for
      * <CODE>shmdt()</CODE> for more details.
      * @param shmaddr The address of this segment.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int shmdt (int shmaddr);

    /** Write to a shared memory segment.
      * @param shmaddr The address of the shared memory segment.
      * @param data The data to be written.
      * @param offset The starting point for writing the data.
      * @param nbytes The number of bytes to be written.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native void shmWrite (int shmaddr, byte[] data, int offset, int nbytes);
    
    public void shmWrite (int shmaddr, byte[] data)
      { shmWrite(shmaddr, data, 0, data.length);
      } // shmWrite

    /** Read from a shared memory segment.
      * @param shmaddr The address of the shared memory segment.
      * @param data The array to be filled with data.
      * @param offset The starting point for filling the array.
      * @param nbytes The number of bytes to be read.
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native void shmRead (int shmaddr, byte[] data, int offset, int nbytes);
    
    public void shmRead (int shmaddr, byte[] data)
      { shmRead(shmaddr, data, 0, data.length);
      } // shmRead

    /** Remove a shared memory segment.  See the man page for
      * <CODE>shmctl()</CODE> for more details.
      * @param shmid The shared memory segment identifier (obtained from
      * <CODE>shmget()</CODE>).
      * @returns If successful, 0.
      *   On failure, -1 (errnum has the Linux error code).
      */
    public native int shmRmid (int shmid);


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
      { System.loadLibrary("LinuxIPC");
      } // static block

    /** Main method used for some basic testing.
      */
    public static void main (String[] args)
      { LinuxIPC ipc = new LinuxIPC();
        int key;
        int msgqid;
        int semid;
        int retval;
        byte[] msg = { 1, 2, 3, 4 };
        byte[] recvd = new byte[10];
        Msqid_ds msqid_ds = new Msqid_ds();
        
        // Start with FIFOs
        if (ipc.mkfifo("/tmp/FIFO_test", 0660) == 0)
          System.out.println("mkfifo succeeded");
        else
          System.out.println("mkfifo failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        // Test ftok
        if ((key = ipc.ftok("/home/csgw/JavaProgs/JNI", 'b')) != -1)
          System.out.println("ftok succeeded.  key = " + key);
        else
          System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        // Test message queues
        if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) != -1)
          System.out.println("msgget succeeded.  msgq ID = " + msgqid);
        else
          System.out.println("msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (ipc.msgsnd(msgqid, 9999, msg, msg.length, LinuxIPC.IPC_NOWAIT) != -1)
          System.out.println("msgsnd succeeded.");
        else
          System.out.println("msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if ((retval = ipc.msgrcv(msgqid, recvd, recvd.length, 9999, 0)) != -1)
          { System.out.print("msgrcv succeeded. Message =");
            for (int k = 0; k < retval; k++)
              System.out.print(" " + recvd[k]);
            System.out.println();
          }
        else
          System.out.println("msgrcv failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (ipc.msgRmid(msgqid) != -1)
          System.out.println("msgRmid succeeded.\n");
        else
          System.out.println("msgRmid failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        // Test semaphores
        if ((semid = ipc.semget(key, 1, LinuxIPC.IPC_CREAT | 0660)) != -1)
          System.out.println("semget succeeded.  sem ID = " + semid);
        else
          System.out.println("semget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        Sembuf[] sb = { new Sembuf(0, 1, 0) };
        if (ipc.semop(semid, sb) != -1)
          System.out.println("semop signal succeeded.");
        else
          System.out.println("semop signal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        int semVal = ipc.semGetVal(semid, 0);
        if (semVal != -1)
          System.out.println("semGetVal = " + semVal);
        else
          System.out.println("semGetVal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        sb[0] = new Sembuf(0, -1, 0);
        if (ipc.semop(semid, sb) != -1)
          System.out.println("semop wait succeeded.");
        else
          System.out.println("semop wait failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        semVal = ipc.semGetVal(semid, 0);
        if (semVal != -1)
          System.out.println("semGetVal = " + semVal);
        else
          System.out.println("semGetVal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        sb[0] = new Sembuf(0, -1, 0);
        if (ipc.semtimedop(semid, sb, sb.length, 2, 0) != -1)
          System.out.println("semop wait succeeded.");
        else
          System.out.println("semop wait failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (ipc.semRmid(semid) != -1)
          System.out.println("semRmid succeeded.\n");
        else
          System.out.println("semRmid failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
          
        // Test shared memory segments
        int shmid;
        int shmaddr;
        byte[] in = { 'a', 'b', 'c', 'd', 'e' };
        byte[] out = new byte[4];
        if ((shmid = ipc.shmget(key, 10, LinuxIPC.IPC_CREAT | 0660)) != -1)
          System.out.println("shmget succeeded.  shm ID = " + semid);
        else
          System.out.println("shmget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if ((shmaddr = ipc.shmat(shmid, 0, 0)) != -1)
          System.out.println("shmat succeeded.  shmaddr = " + shmaddr);
        else
          System.out.println("shmat failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        ipc.shmWrite(shmaddr, in);
        ipc.shmRead(shmaddr, out);
        System.out.print("out = ");
        for (int k = 0; k < out.length; k++)
          System.out.print(((k ==0) ? "{'" : "', '") + (char)out[k]);
        System.out.println("'}");
        if (ipc.shmdt(shmaddr) != -1)
          System.out.println("shmdt succeeded.");
        else
          System.out.println("shmdt failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // main
      
    /** Class provides a simplified subset of the full msqid_ds struct defined in linux/msg.h.
      */
    public static class Msqid_ds
      { public Ipc_perm msg_perm;
	      public int msg_stime;	/* last msgsnd time */
	      public int msg_rtime;	/* last msgrcv time */
	      public int msg_ctime;	/* last change time */
	      public int msg_lcbytes;	/* Reuse junk fields for 32 bit */
	      public int msg_lqbytes;	/* ditto */
	      public int msg_cbytes;	/* current number of bytes on queue */
	      public int msg_qnum;	/* number of messages in queue */
	      public int msg_qbytes;	/* max number of bytes on queue */
	      public int msg_lspid;	/* pid of last msgsnd */
	      public int msg_lrpid;	/* last receive pid */
	      
	      public Msqid_ds ()
	        { msg_perm = new Ipc_perm();
	        } // constructor
	        
	      public String toString ()
	        { StringBuffer sb = new StringBuffer("msqid_ds: msg_perm = ");
	          sb.append(msg_perm);
	          sb.append(" msg_stime = ");
	          sb.append(msg_stime);
	          sb.append(" msg_rtime = ");
	          sb.append(msg_rtime);
	          sb.append(" msg_ctime = ");
	          sb.append(msg_ctime);
	          sb.append(" msg_lcbytes = ");
	          sb.append(msg_lcbytes);
	          sb.append(" msg_lqbytes = ");
	          sb.append(msg_lqbytes);
	          sb.append(" msg_cbytes = ");
	          sb.append(msg_cbytes);
	          sb.append(" msg_qnum = ");
	          sb.append(msg_qnum);
	          sb.append(" msg_qbytes = ");
	          sb.append(msg_qbytes);
	          sb.append(" msg_lspid = ");
	          sb.append(msg_lspid);
	          sb.append(" msg_lrpid = ");
	          sb.append(msg_lrpid);

	          return sb.toString();
	        }
      } // inner class Msqid_ds

    /** Class provides a simplified version of the ipc_perm struct defined in linux/ipc.h.
      */
    public static class Ipc_perm
      { public int key;
	      public int uid;
	      public int gid;
	      public int cuid;
	      public int cgid;
	      public int mode; 
	      public int seq;

	      public String toString ()
	        { StringBuffer sb = new StringBuffer("ipc_perm: key = ");
	          sb.append(key);
	          sb.append(" uid = ");
	          sb.append(uid);
	          sb.append(" gid = ");
	          sb.append(gid);
	          sb.append(" cuid = ");
	          sb.append(cuid);
	          sb.append(" cgid = ");
	          sb.append(cgid);
	          sb.append(" mode = ");
	          sb.append(mode);
	          sb.append(" seq = ");
	          sb.append(seq);

	          return sb.toString();
	        }
      } // inner class Ipc_perm

    /** Class implementing the sembuf struct defined in linux/sem.h.
      */
    public static class Sembuf
      { public short sem_num; // semaphore index in array
	      public short sem_op; // semaphore operation
	      public short sem_flg; // operation flags
	      
	      public Sembuf (int sem_num, int sem_op, int sem_flg)
	        { this.sem_num = (short)sem_num;
	          this.sem_op = (short)sem_op;
	          this.sem_flg = (short)sem_flg;
	        } // constructor

	      public String toString ()
	        { StringBuffer sb = new StringBuffer("Sembuf: sem_num = ");
	          sb.append(sem_num);
	          sb.append(" sem_op = ");
	          sb.append(sem_op);
	          sb.append(" sem_flg = ");
	          sb.append(sem_flg);

	          return sb.toString();
	        }
      } // inner class Sembuf

  } // class LinuxIPC
