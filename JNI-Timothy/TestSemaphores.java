// TestSemaphores.java

public class TestSemaphores
  { public static final int TIME = 1000; // Duration of test in seconds
    private static boolean running;
  
    private static void testLinuxSems ()
      { long counter = 0;
        LinuxSemaphore sem = new LinuxSemaphore(1);
        TimerThread t = new TimerThread();
        t.start();
        System.out.println("Testing LinuxSemaphore class");
        running = true;
        long startTime = System.currentTimeMillis();
        while (running)
          { sem.acquire();
            sem.release();
            System.out.print(++counter + "\r");
          }
        long endTime = System.currentTimeMillis();
        System.out.println(counter + " synchs in " + (endTime-startTime) + " ms. = " + (double)counter/(endTime-startTime) + " synchs/ms");
        sem.close();
      } // testLinuxSems

    private static void testIPCSems ()
      { long counter = 0;
        int key;
        int semid;
        LinuxIPC ipc = new LinuxIPC();
        LinuxIPC.Sembuf[] sb = { new LinuxIPC.Sembuf(0, 0, 0) };
        if ((key = ipc.ftok("/home/csgw/JavaProgs/JNI", 't')) != -1)
          System.out.println("ftok succeeded.  key = " + key);
        else
          System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));

        if ((semid = ipc.semget(key, 1, LinuxIPC.IPC_CREAT | 0660)) != -1)
          { System.out.println("semget succeeded.  sem ID = " + semid);
            TimerThread t = new TimerThread();
            t.start();
            System.out.println("Testing LinuxIPC semaphores");
            running = true;
            long startTime = System.currentTimeMillis();
            while (running)
              { sb[0].sem_op = 1;
                if (ipc.semop(semid, sb) == -1)
                  System.out.println("semop signal failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
                sb[0].sem_op = -1;
                if (ipc.semop(semid, sb) == -1)
                  System.out.println("semop wait failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
                System.out.print(++counter + "\r");
              }
            long endTime = System.currentTimeMillis();
            System.out.println(counter + " synchs in " + (endTime-startTime) + " ms. = " + (double)counter/(endTime-startTime) + " synchs/ms");

            if (ipc.semRmid(semid) != -1)
              System.out.println("semRmid succeeded.");
            else
              System.out.println("semRmid failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
          }
        else
          System.out.println("semget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        
      } // testIPCSems

    private static void testJavaSems () throws InterruptedException
      { long counter = 0;
        java.util.concurrent.Semaphore sem = new java.util.concurrent.Semaphore(1);
        TimerThread t = new TimerThread();
        t.start();
        System.out.println("Testing java.util.concurrent.Semaphore class");
        running = true;
        long startTime = System.currentTimeMillis();
        while (running)
          { sem.acquire();
            sem.release();
            System.out.print(++counter + "\r");
          }
        long endTime = System.currentTimeMillis();
        System.out.println(counter + " synchs in " + (endTime-startTime) + " ms. = " + (double)counter/(endTime-startTime) + " synchs/ms");
      } // testJavaSems

    public static void main (String[] args)
      { testLinuxSems();
        testIPCSems();
        try
          { testJavaSems();
          }
        catch (InterruptedException e)
          { System.out.println("Interrupted: " + e);
          }
      } // main
      
    private static class TimerThread extends Thread
      {
        public void run ()
          { try
              { sleep(TIME);
              }
            catch (InterruptedException exc)
              {  }
            running = false;
          } //
      } // inner class TimerThread

  } // class TestSemaphores
