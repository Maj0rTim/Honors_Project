// MessageQueueInputStream.java

import java.io.*;

/** This class implements an input stream based on a Linux message queue.
  * @author George Wells
  * @version 1.0 (18 November 2008)
  */
public class MessageQueueInputStream extends InputStream
  { private final boolean DEBUG = false;
    private int msgqid; // The internal message queue identifier
    private LinuxIPC ipc = new LinuxIPC(); // The native IPC library
    private byte[] buf = new byte[MessageQueueOutputStream.MAX_BUF_SIZE]; // Biggest buffer possible
    private int len; // Amount of buf occupied
    private int pos; // Current read position

    public MessageQueueInputStream (int key)
      { // System.out.println("MessageQueueInputStream: opening stream, key = " + key);
        if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) == -1)
          System.err.println("MessageQueueInputStream: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        // System.out.println("MessageQueueInputStream: opened stream.");
      } // constructor

    public int read () throws IOException
      { if (pos >= len) // Need more data
          fillBuffer();
        byte b = buf[pos++];
        return (b >= 0) ? b : 256+b;
      } // read

    public void close () throws IOException
      { if (ipc.msgRmid(msgqid) == -1)
         throw new IOException("MessageQueueInputStream: close failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
      } // close
      
    public int available () throws IOException
      { // System.out.println("available");
        if (pos < len)
          return (len-pos);
        if (ipc.msgrcv(msgqid, null, 0, MessageQueueOutputStream.QUEUE_TYPE, LinuxIPC.IPC_NOWAIT) == -1) // Should fail!
          { if (ipc.getErrnum() == 7)  // 7 == E2BIG error code
              return 1; // At least one byte available
          }
        return 0;
      } // available
      
    /** Tries to get next message and place it in buffer.
      */
    private void fillBuffer ()  throws IOException
      { if ((len = ipc.msgrcv(msgqid, buf, buf.length, MessageQueueOutputStream.QUEUE_TYPE, 0)) == -1)
          throw new IOException("MessageQueueInputStream: fillBuffer failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        if (DEBUG)
          { System.out.println("fillBuffer got " + len);
            for (int k = 0; k < len; k++)
              System.console().printf("%02X ", buf[k]);
            /*System.out.print("...");
            for (int k = Math.max(len-10, 10); k < len; k++)
              // System.out.print(" " + Integer.toHexString(buf[k]));
              System.console().printf(" %02X", buf[k]);*/
            System.out.println();
          } // DEBUG        
        pos = 0;
      } // fillBuffer

  } // class MessageQueueInputStream
