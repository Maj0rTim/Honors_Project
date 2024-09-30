// SharedMemoryStreams.java

import java.io.IOException;

/** This class provides the JNI native language support for the
  * SharedMemoryInputStream and SharedMemoryOutputStream classes.
  * @author George Wells
  * @version 1.0 (4 December 2008)
  */
public class SharedMemoryStreams
  { private int shmid; // Shared memory segment identifier
    private int shmaddr; // Semaphore set identifier
    private int semid; // Semaphore set identifier
    /** 
      */
    private int errnum; // Linux error number, if any.
    
    /** Initialise private data fields - called by native code.
      */
    public void initFields (int shmid, int shmaddr, int semid)
      { this.shmid = shmid;
        this.shmaddr = shmaddr;
        this.semid = semid;
        System.out.println("Fields initialised: smid = " + shmid + " shmaddr = " + shmaddr + " semid = " + semid);
      } // initFields
    
    public SharedMemoryStreams (int key, int size, boolean initSems)
      { initStream(key, size, initSems ? 1 : 0);
      } // constructor
  
    /** Initialises stream by creating semaphore set and shared memory segment.
      * Initialises the shmid and semid fields of this class.
      */
    private native void initStream (int key, int size, int initSems);
    
    public void sendData (byte[]buf, int offset, int len) throws IOException
      { int retval = sendData(shmaddr, semid, buf, offset, len);
        if (retval == -1)
          throw new IOException("Native sendData failed: " + errnum + " " + strerror(errnum));
        /*
        
        */
      } // sendData
    
    /** Places the data into the shared memory segment, using the semaphores
      * for signalling.
      * Returns -1 if there is an error, 0 otherwise.
      */
    private native int sendData (int shmaddr, int semid, byte[]buf, int offset, int len);
  
    /** Tries to get next chunk of data and place it in buffer.
      * @param buf The array to be filled with data.
      * @returns the number of bytes placed in buf.
      */
    public int fillBuffer (byte[] buf)  throws IOException
      { int retval = fillBuffer(shmaddr, semid, buf);
        if (retval == -1)
          throw new IOException("Native fillBuffer failed: " + errnum + " " + strerror(errnum));
        else
          return retval;
      } // fillBuffer
    
    /** Fills buf from the the shared memory segment, using the semaphores
      * for signalling.
      * Returns The number of bytes placed in buf, or -1 if there is an error.
      */
    private native int fillBuffer (int shmaddr, int semid, byte[] buf);
    
    /** Close this connection by detaching the shared memory segment.
      * The shared memory and sempahore identifiers are also removed if necessary.
      */
    public void close (boolean removeIds)
      { close(shmid, shmaddr, semid, removeIds ? 1 : 0);
      } // close
      
    /** Detach shared memory segment and optionally remove share memory
      * and semaphore ids.
      */
    private native void close (int shmid, int shmaddr, int semid, int removeIds);
    
    /** Set the error number.  This method overwrites whatever value is
      * currently contained in the errnum variable.
      * @param errnum The new value for the errnum variable.
      */
    public void setErrnum (int errnum)
      { this.errnum = errnum;
      } // setErrnum

    /** Get error message corresponding to error code.  This simply calls the
      * C strerror function.
      * @param errnum Error code (as defined in errno.h).
      * @returns String representation of error code.
      */
    public native String strerror (int errnum);
  
    static
      { System.loadLibrary("SharedMemoryStreams");
      } // static block
  
  } // class SharedMemoryStreams
