// SharedMemoryInputStream.java

import java.io.*;

/** This class implements an input stream based on a Linux shared memory segment.
  * @author George Wells
  * @version 1.0 (3 December 2008)
  */
public class SharedMemoryInputStream extends InputStream
  { private final boolean DEBUG = false;
    private SharedMemoryStreams sms; // The native shared memory streams library
    private byte[] buf = new byte[SharedMemoryOutputStream.DEF_BUF_SIZE]; // Biggest buffer possible
    private int len; // Amount of buf occupied
    private int pos; // Current read position

    public SharedMemoryInputStream (int key, int size)
      { sms = new SharedMemoryStreams(key, size, false);
    } // constructor  

    public SharedMemoryInputStream (int key)
      { this(key, SharedMemoryOutputStream.DEF_BUF_SIZE);
      } // constructor

    public int read () throws IOException
      { if (pos >= len) // Need more data
          { len = sms.fillBuffer(buf);
            pos = 0;
          }
        byte b = buf[pos++];
        return (b >= 0) ? b : 256+b;
      } // read
      

    public void close () throws IOException
      { sms.close(true);
      } // close
      
    public int available () throws IOException
      { return (pos < len) ? (len-pos) : 0;
      } // available

  } // class SharedMemoryInputStream
