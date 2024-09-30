// SharedMemoryOutputStream1.java

import java.io.*;

/** This class implements an output stream based on a Linux message queue.
  * @author George Wells
  * @version 1.0 (18 November 2008)
  */
public class SharedMemoryOutputStream1 extends OutputStream
  { public static final int DEF_BUF_SIZE = 4092; // Default buffer size for shared memory segment

    private int shmid; // The internal shared memory segment identifier
    private int shmaddr; // The address of the shared memory segment
    private int semid; // The internal semaphore identifier
    private  static final LinuxIPC.Sembuf[] WRITE_SIGNAL = { new LinuxIPC.Sembuf(0, 1, 0) };
    private  static final LinuxIPC.Sembuf[] WRITE_WAIT = { new LinuxIPC.Sembuf(0, -1, 0) };
    private  static final LinuxIPC.Sembuf[] READ_SIGNAL = { new LinuxIPC.Sembuf(1, 1, 0) };
    private  static final LinuxIPC.Sembuf[] READ_WAIT = { new LinuxIPC.Sembuf(1, -1, 0) };
    private LinuxIPC ipc = new LinuxIPC(); // The native IPC library
    private byte[] maxBuffer = new byte[DEF_BUF_SIZE]; // Send buffer when we need to slice and dice

    public SharedMemoryOutputStream1 (int key, int size)
      { // System.out.println("SharedMemoryOutputStream1: opening stream, key = " + key);
        // First create shared memory segment
        if ((shmid = ipc.shmget(key, size+4, LinuxIPC.IPC_CREAT | 0600)) == -1)
          System.err.println("SharedMemoryOutputStream1: shmget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if ((shmaddr = ipc.shmat(shmid, 0, 0)) == -1)
          System.err.println("SharedMemoryOutputStream1: shmat failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("SharedMemoryOutputStream1: shmid = " + shmid + " shmaddr = " + shmaddr);
        // Now create semaphore set
        if ((semid = ipc.semget(key, 2, LinuxIPC.IPC_CREAT | 0600)) == -1)
          System.err.println("SharedMemoryOutputStream1: semget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (ipc.semSetVal(semid, 0, 1) == -1) // Clear to write
          System.err.println("SharedMemoryOutputStream1: semSetVal for write failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (ipc.semSetVal(semid, 1, 0) == -1) // Not clear to read
          System.err.println("SharedMemoryOutputStream1: semSetVal for read failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // constructor

    public SharedMemoryOutputStream1 (int key)
      { this(key, DEF_BUF_SIZE);
      } // constructor

    private void sendData (byte[]buf, int offset, int len) throws IOException
      { /*int semVal = ipc.semGetVal(semid, 0);
        if (semVal != -1)
          System.out.println("semGetVal for WRITE = " + semVal);
        else
          System.out.println("semGetVal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        semVal = ipc.semGetVal(semid, 1);
        if (semVal != -1)
          System.out.println("semGetVal for READ = " + semVal);
        else
          System.out.println("semGetVal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        */
        if (ipc.semop(semid, WRITE_WAIT) == -1)
          throw new IOException("sendData: semop wait failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("SharedMemoryOutputStream: got write signal");
        byte[] size = new byte[4];
        size[0] = (byte)len;
        size[1] = (byte)(len>>8);
        size[2] = (byte)(len>>16);
        size[3] = (byte)(len>>24);
        /*System.out.println("  len = " + len);
        for (int k = 0; k < size.length; k++)
          System.out.println("  size[" + k + "] = " + size[k]);*/
        ipc.shmWrite(shmaddr, size);
        ipc.shmWrite(shmaddr+4, buf, offset, len);
        if (ipc.semop(semid, READ_SIGNAL) == -1)
          throw new IOException("sendData: semop signal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("SharedMemoryOutputStream: signalled read");
      } // write

    public void write (int b) throws IOException
      { // System.out.println("1");
        byte[] buf = { (byte)b };
        sendData(buf, 0, 1);
      } // write

    public void write (byte[] b) throws IOException
      { // System.out.println("2");
        if (b.length <= maxBuffer.length) // Safe to do directly
          sendData(b, 0, b.length);
        else // Slice and dice!
          { int k = 0;
            int chunkSize;
            while (k < b.length)
              { chunkSize = Math.min(maxBuffer.length, b.length-k);
                System.arraycopy(b, k, maxBuffer, 0, chunkSize);
                sendData(maxBuffer, 0, chunkSize);
                k += chunkSize;
              } // while
          } // else
      } // write
          
    public void write (byte[] b, int off, int len) throws IOException
      { // System.out.println("3: " + len);
        if (len <= maxBuffer.length) // Safe to do directly
          sendData(b, off, len);
        else // Slice and dice!
          { int k = off;
            int chunkSize;
            while (k < off+len)
              { chunkSize = Math.min(maxBuffer.length, off+len-k);
                System.arraycopy(b, k, maxBuffer, 0, chunkSize);
                sendData(maxBuffer, 0, chunkSize);
                k += chunkSize;
              } // while
          } // else
      } // write

    public void close () throws IOException
      { if (ipc.shmdt(shmaddr) == -1)
          throw new IOException("SharedMemoryInputStream1: close failed on shmdt: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // close
      
  } // class SharedMemoryOutputStream1
