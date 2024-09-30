// SharedMemoryInputStream1.java

import java.io.*;

/** This class implements an input stream based on a Linux shared memory segment.
  * @author George Wells
  * @version 1.0 (3 December 2008)
  */
public class SharedMemoryInputStream1 extends InputStream
  { private final boolean DEBUG = false;
    private int shmid; // The internal shared memory segment identifier
    private int shmaddr; // The address of the shared memory segment
    private int semid; // The internal semaphore identifier
    private  static final LinuxIPC.Sembuf[] WRITE_SIGNAL = { new LinuxIPC.Sembuf(0, 1, 0) };
    private  static final LinuxIPC.Sembuf[] WRITE_WAIT = { new LinuxIPC.Sembuf(0, -1, 0) };
    private  static final LinuxIPC.Sembuf[] READ_SIGNAL = { new LinuxIPC.Sembuf(1, 1, 0) };
    private  static final LinuxIPC.Sembuf[] READ_WAIT = { new LinuxIPC.Sembuf(1, -1, 0) };
    private LinuxIPC ipc = new LinuxIPC(); // The native IPC library
    private byte[] buf = new byte[SharedMemoryOutputStream.DEF_BUF_SIZE]; // Biggest buffer possible
    private int len; // Amount of buf occupied
    private int pos; // Current read position

    public SharedMemoryInputStream1 (int key, int size)
      { // System.out.println("SharedMemoryInputStream1: opening stream, key = " + key);
        // First create shared memory segment
        if ((shmid = ipc.shmget(key, size+4, LinuxIPC.IPC_CREAT | 0600)) == -1)
          System.err.println("SharedMemoryInputStream1: shmget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if ((shmaddr = ipc.shmat(shmid, 0, 0)) == -1)
          System.err.println("SharedMemoryInputStream1: shmat failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("SharedMemoryInputStream1: shmid = " + shmid + " shmaddr = " + shmaddr);
        // Now create semaphore set
        if ((semid = ipc.semget(key, 2, LinuxIPC.IPC_CREAT | 0600)) == -1)
          System.err.println("SharedMemoryInputStream1: semget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("SharedMemoryInputStream1: opened stream.");
      } // constructor

    public SharedMemoryInputStream1 (int key)
      { this(key, SharedMemoryOutputStream1.DEF_BUF_SIZE);
      } // constructor

    public int read () throws IOException
      { if (pos >= len) // Need more data
          fillBuffer();
        byte b = buf[pos++];
        return (b >= 0) ? b : 256+b;
      } // read

    public void close () throws IOException
      { if (ipc.shmdt(shmaddr) == -1)
          throw new IOException("SharedMemoryInputStream1: close failed on shmdt: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (ipc.shmRmid(shmid) == -1)
          throw new IOException("SharedMemoryInputStream1: close failed on shmRmid: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (ipc.semRmid(semid) == -1)
          throw new IOException("SharedMemoryInputStream1: close failed on semRmid: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // close
      
    public int available () throws IOException
      { if (pos < len)
          return (len-pos);
        return 0;
      } // available
      
    /** Tries to get next chunk of data and place it in buffer.
      */
    private void fillBuffer ()  throws IOException
      { if (ipc.semop(semid, READ_WAIT) == -1)
          throw new IOException("fillBuffer: semop wait failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("SharedMemoryInputStream1: got read signal");
        byte[] size = new byte[4];
        ipc.shmRead(shmaddr, size);
        /* for (int k = 0; k < size.length; k++)
          System.out.println("  fillBuffer: size[" + k + "] = " + size[k]);
        */
        len = size[0] & 0xFF;
        len |= (size[1] << 8) & 0xFF00;
        len |= (size[2] << 16) & 0xFF0000;
        len |= (size[3] << 24) & 0xFF000000;
        // System.out.println("fillBuffer: len = " + len);
        ipc.shmRead(shmaddr+4, buf, 0, len);
        if (ipc.semop(semid, WRITE_SIGNAL) == -1)
          throw new IOException("fillBuffer: semop signal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("SharedMemoryInputStream1: signalled write");
        if (DEBUG)
          { System.out.println("fillBuffer got " + len);
            for (int k = 0; k < Math.min(10, len); k++)
              System.console().printf("%02X ", buf[k]);
            System.out.print("...");
            for (int k = Math.max(len-10, 10); k < len; k++)
              // System.out.print(" " + Integer.toHexString(buf[k]));
              System.console().printf(" %02X", buf[k]);
            System.out.println();
          } // DEBUG        
        pos = 0;
      } // fillBuffer

  } // class SharedMemoryInputStream1
