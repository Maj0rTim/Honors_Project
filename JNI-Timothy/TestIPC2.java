// Filename: TestIPC2.java

import java.io.*;
import java.util.Date;

/** This class is used for testing the LinuxIPC class.
  * @author George Wells
  * @version 1.0 (17 November 2008)
  */
public class TestIPC2
  { private final static int ARRAY_SIZE = 800000;
    // private static long startTime;
  
    /** Main method used for some basic testing.
      */
    public static void main (String[] args)
      { LinuxIPC ipc = new LinuxIPC();
        int[] array = new int[ARRAY_SIZE];
        int key;
        
        for (int k = 0; k < array.length; k++)
          array[k] = k+10;
          
        // Now test message queue streams with bulk files
        if ((key = ipc.ftok("/home/csgw/JavaProgs/JNI", 'e')) != -1)
          { Thread t = new Thread(new MsgStreamThread(key));
            t.start();
            long startTime = System.nanoTime();
            long endTime;
            try
              { ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new SharedMemoryOutputStream(key)));
                  // new ObjectOutputStream(new BufferedOutputStream(new MessageQueueOutputStream(key)));
                  // new FileOutputStream("/tmp/LINUXIPCFIFO_test")));
                startTime = System.nanoTime();
                oos.writeObject(array);
                endTime = System.nanoTime();
                System.out.println("Wrote to stream OK.  Time (ns) = " + (endTime-startTime));
                oos.close();
              }
            catch (Exception exc)
              { System.err.println("Error: " + exc);
                exc.printStackTrace();
              }
          }
        else
          System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

      } // main

    private static class MsgStreamThread implements Runnable
      { private int key;
        
        public MsgStreamThread (int key)
          { this.key = key;
          } // constructor

        public void run ()
          { long startTime = System.nanoTime();
            long endTime;
            try
              { ObjectInputStream ois = new ObjectInputStream(new SharedMemoryInputStream(key));
                   // new ObjectInputStream(new MessageQueueInputStream(key));
                   // new FileInputStream("/tmp/LINUXIPCFIFO_test"));
                startTime = System.nanoTime();
                int[] msg = (int[])ois.readObject();
                endTime = System.nanoTime();
                System.out.println("Read from message queue OK. Length = " + msg.length + "  Time (ns) = " + (endTime-startTime));
                if (msg.length != ARRAY_SIZE)
                  System.out.println("  ERROR: Length should be " + ARRAY_SIZE);
                for (int k = 0; k < msg.length; k++)
                  if (msg[k] != k+10)
                    System.out.println("  ERROR: data corrupted at position " + k + ".  Data is " + msg[k]);
                ois.close();
              }
            catch (Exception exc)
              { System.err.println("I/O Error: " + exc);
                exc.printStackTrace();
              }
          } // run

      } // inner class MsgStreamThread

  } // class LinuxIPC
