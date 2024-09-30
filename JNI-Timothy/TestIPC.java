// Filename: TestIPC.java

import java.io.*;
import java.util.Date;

/** This class is used for testing the LinuxIPC class.
  * @author George Wells
  * @version 1.0 (17 Novermber 2008)
  */
public class TestIPC
  {
    /** Main method used for some basic testing.
      */
    public static void main (String[] args)
      { LinuxIPC ipc = new LinuxIPC();
        final String fifoName = "/tmp/LINUXIPCFIFO_test"; // Filename for named pipe test

        // First test named pipes
        if (ipc.mkfifo(fifoName, 0660) == 0)
          { System.out.println("mkfifo succeeded");
            Thread t = new Thread(new FIFOThread(fifoName));
            t.start();
            try
              { PrintWriter pw = new PrintWriter(new FileOutputStream(fifoName));
                pw.println("Hello thread");
                System.out.println("Wrote to FIFO OK");
                pw.close();
              }
            catch (IOException exc)
              { System.err.println("I/O Error: " + exc);
                exc.printStackTrace();
              }
          }
        else
          System.out.println("mkfifo failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        // Now test message queues
        int key;
        int msgqid;
        if ((key = ipc.ftok("/home/csgw/JavaProgs/JNI", 'c')) != -1)
          System.out.println("ftok succeeded.  key = " + key);
        else
          System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) != -1)
          { System.out.println("msgget succeeded.  msgq ID = " + msgqid);
            Thread t = new Thread(new MSGQThread(key));
            t.start();

            byte[] msg = "Hello Message Queue".getBytes();
            if (ipc.msgsnd(msgqid, 9999, msg, msg.length, LinuxIPC.IPC_NOWAIT) != -1)
              System.out.println("msgsnd succeeded.");
            else
              System.out.println("msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            try
              { t.join();
              }
            catch (InterruptedException e)
              {  }
            if (ipc.msgRmid(msgqid) != -1)
              System.out.println("msgRmid succeeded.");
            else
              System.out.println("msgRmid failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
          }
        else
          System.out.println("msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        // Now test message queue streams
        if ((key = ipc.ftok("/home/csgw/JavaProgs/JNI", 'd')) != -1)
          { Thread t = new Thread(new MsgStreamThread(key));
            t.start();
            try
              { ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new MessageQueueOutputStream(key)));
                oos.writeObject(new Date()); // "Hello message queue stream");
                oos.close();
                Thread.sleep(1000);
                System.out.println("Wrote to message queue OK\n");
              }
            catch (Exception exc)
              { System.err.println("Error: " + exc);
                exc.printStackTrace();
              }
          }
        else
          System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
          
        // Now test semaphores
        int semid;
        if ((key = ipc.ftok("/home/csgw/JavaProgs/JNI", 's')) != -1)
          System.out.println("ftok succeeded.  key = " + key);
        else
          System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        if ((semid = ipc.semget(key, 1, LinuxIPC.IPC_CREAT | 0660)) != -1)
          { System.out.println("semget succeeded.  sem ID = " + semid);
            Thread t = new Thread(new SemThread(key));
            t.start();
            try
              { Thread.sleep(1000);
              }
            catch (InterruptedException e)
              {  }
            int numWaiting = ipc.semGetNCnt(semid, 0);
            if (numWaiting != -1)
              System.out.println(numWaiting + " threads waiting on semaphore.");
            else
              System.out.println("semctl/GETNCNT failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            LinuxIPC.Sembuf[] sb = { new LinuxIPC.Sembuf(0, 1, 0) };
            if (ipc.semop(semid, sb) != -1)
              System.out.println("semop signal succeeded.");
            else
              System.out.println("semop signal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            if (ipc.semRmid(semid) != -1)
              System.out.println("semRmid succeeded.");
            else
              System.out.println("semRmid failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
          }
        else
          System.out.println("semget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

      } // main

    private static class FIFOThread implements Runnable
      { private String fifoName;

        public FIFOThread (String fifoName)
          { this.fifoName = fifoName;
          } // constructor

        public void run ()
          { try
              { BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fifoName)));
                String line = br.readLine();
                System.out.println("Read from FIFO OK: " + line);
                br.close();
              }
            catch (IOException exc)
              { System.err.println("I/O Error: " + exc);
                exc.printStackTrace();
              }
          } // run

      } // inner class FIFOThread

    private static class MSGQThread implements Runnable
      { private LinuxIPC ipc = new LinuxIPC();
        private int key;

        public MSGQThread (int key)
          { this.key = key;
          } // constructor

        public void run ()
          { byte[] msg = new byte[1000];
            int msgqid;
            int len;

            if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) != -1)
              System.out.println("msgget succeeded.  msgq ID = " + msgqid);
            else
              { System.out.println("msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
                return;
              }

            if ((len = ipc.msgrcv(msgqid, msg, msg.length, 9999, 0)) != -1)
              { String s = new String(msg, 0, len);
                System.out.println("msgrcv succeeded. Message = \"" + s + "\"");
              }
            else
              System.out.println("msgrcv failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
          } // run

      } // inner class MSGQThread

    private static class MsgStreamThread implements Runnable
      { private int key;

        public MsgStreamThread (int key)
          { this.key = key;
          } // constructor

        public void run ()
          { try
              { ObjectInputStream ois = new ObjectInputStream(new MessageQueueInputStream(key));
                Object o = ois.readObject();
                System.out.println("Read from message queue OK: " + o);
                ois.close();
              }
            catch (Exception exc)
              { System.err.println("I/O Error: " + exc);
                exc.printStackTrace();
              }
          } // run

      } // inner class MsgStreamThread

    private static class SemThread implements Runnable
      { private LinuxIPC ipc = new LinuxIPC();
        private int key;

        public SemThread (int key)
          { this.key = key;
          } // constructor

        public void run ()
          { int semid;

            if ((semid = ipc.semget(key, 1, LinuxIPC.IPC_CREAT | 0660)) != -1)
              System.out.println("semget succeeded.  sem ID = " + semid);
            else
              { System.out.println("semget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
                return;
              }
            LinuxIPC.Sembuf[] sb = { new LinuxIPC.Sembuf(0, -1, 0) };
            if (ipc.semop(semid, sb) != -1)
              System.out.println("SemThread: semop wait succeeded.");
            else
              System.out.println("SemThread: semop wait failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

          } // run

      } // inner class SemThread


  } // class LinuxIPC
