// SharedMemoryOutputStream.java

import java.io.*;

/** This class implements an output stream based on a Linux message queue.
  * @author George Wells
  * @version 1.0 (18 November 2008)
  */
public class SharedMemoryOutputStream extends OutputStream
  { public static final int DEF_BUF_SIZE = 4092; // Default buffer size for shared memory segment
    private SharedMemoryStreams sms; // The native JNI library
    private byte[] maxBuffer = new byte[DEF_BUF_SIZE]; // Send buffer when we need to slice and dice

    public SharedMemoryOutputStream (int key, int size)
      { sms = new SharedMemoryStreams(key, size, true);
      } // constructor

    public SharedMemoryOutputStream (int key)
      { this(key, DEF_BUF_SIZE);
      } // constructor

    public void write (int b) throws IOException
      { // System.out.println("1");
        byte[] buf = { (byte)b };
        sms.sendData(buf, 0, 1);
      } // write

    public void write (byte[] b) throws IOException
      { // System.out.println("2");
        if (b.length <= maxBuffer.length) // Safe to do directly
          sms.sendData(b, 0, b.length);
        else // Slice and dice!
          { int k = 0;
            int chunkSize;
            while (k < b.length)
              { chunkSize = Math.min(maxBuffer.length, b.length-k);
                System.arraycopy(b, k, maxBuffer, 0, chunkSize);
                sms.sendData(maxBuffer, 0, chunkSize);
                k += chunkSize;
              } // while
          } // else
      } // write
          
    public void write (byte[] b, int off, int len) throws IOException
      { // System.out.println("3: " + len);
        if (len <= maxBuffer.length) // Safe to do directly
          sms.sendData(b, off, len);
        else // Slice and dice!
          { int k = off;
            int chunkSize;
            while (k < off+len)
              { chunkSize = Math.min(maxBuffer.length, off+len-k);
                System.arraycopy(b, k, maxBuffer, 0, chunkSize);
                sms.sendData(maxBuffer, 0, chunkSize);
                k += chunkSize;
              } // while
          } // else
      } // write

    public void close () throws IOException
      { sms.close(false);
      } // close
      
  } // class SharedMemoryOutputStream
