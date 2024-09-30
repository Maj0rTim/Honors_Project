// MessageQueueOutputStream.java

import java.io.*;

/** This class implements an output stream based on a Linux message queue.
  * @author George Wells
  * @version 1.0 (18 November 2008)
  */
public class MessageQueueOutputStream extends OutputStream
  { public static final int QUEUE_TYPE = 9999; // Message queue type field
    public static final int MAX_BUF_SIZE = 4096; // Maximum buffer size for message queues

    private int msgqid; // The internal message queue identifier
    private LinuxIPC ipc = new LinuxIPC(); // The native IPC library
    private byte[] maxBuffer = new byte[MAX_BUF_SIZE]; // Send buffer when we need to slice and dice

    public MessageQueueOutputStream (int key)
      { if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) == -1)
          System.err.println("MessageQueueOutputStream: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // constructor

    private void sendData (byte[]buf, int len) throws IOException
      { if (ipc.msgsnd(msgqid, QUEUE_TYPE, buf, len, 0) == -1)
          throw new IOException("MessageQueueOutputStream: msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // write

    public void write (int b) throws IOException
      { // System.out.println("1");
        byte[] buf = { (byte)b };
        sendData(buf, 1);
      } // write

    public void write (byte[] b) throws IOException
      { // System.out.println("2");
        if (b.length <= maxBuffer.length) // Safe to do directly
          { sendData(b, b.length);
          }
        else // Slice and dice!
          { int k = 0;
            int chunkSize;
            while (k < b.length)
              { chunkSize = Math.min(maxBuffer.length, b.length-k);
                System.arraycopy(b, k, maxBuffer, 0, chunkSize);
                sendData(maxBuffer, chunkSize);
                k += chunkSize;
              } // while
          } // else
      } // write
          
    public void write (byte[] b, int off, int len) throws IOException
      { // System.out.println("3: " + len);
        int k = off;
        int chunkSize;
        while (k < off+len)
          { chunkSize = Math.min(maxBuffer.length, off+len-k);
            System.arraycopy(b, k, maxBuffer, 0, chunkSize);
            sendData(maxBuffer, chunkSize);
            k += chunkSize;
          } // while
      } // write

  } // class MessageQueueOutputStream
