import java.util.*;
import java.io.*;

/** Uses message queues for communication, and nanosecond timing.
  */
public class BPingPongShm1
  { //private static LinuxIPC ipc = new LinuxIPC();
    private int inQid; // Message queue ID
    private int outQid; // Message queue ID
    private String myName;
    private String yourName;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    private long total = 0;
    private static long startSetup;
    private long endSetup;
    private static int ARRAY_SIZE = 40000;
    
    public BPingPongShm1 (int key, String myName) throws IOException
      { int pongKey = ((int)"pong".hashCode()) ^ key;
        int pingKey = ((int)"ping".hashCode()) ^ key;
        this.myName = myName;
        
        System.out.println(myName + " starting initialisation.");
        System.out.println(myName + " pongKey = " + pongKey);
        System.out.println(myName + " pingKey = " + pingKey);
        if (myName.equals("Ping"))
          { yourName = "Pong";
            outStream = new ObjectOutputStream(new BufferedOutputStream(new SharedMemoryOutputStream1(pongKey, ARRAY_SIZE)));
            outStream.flush();
            System.out.println(myName + " outStream created.");
            inStream = new ObjectInputStream(new SharedMemoryInputStream1(pingKey, ARRAY_SIZE));
            System.out.println(myName + " inStream created.");
          }
        else
          { yourName = "Ping";
            outStream = new ObjectOutputStream(new BufferedOutputStream(new SharedMemoryOutputStream1(pingKey, ARRAY_SIZE)));
            outStream.flush();
            System.out.println(myName + " outStream created.");
            inStream = new ObjectInputStream(new SharedMemoryInputStream1(pongKey, ARRAY_SIZE));
            System.out.println(myName + " inStream created.");
          }
        System.out.println(myName + " initialisation complete.");
      } // constructor

    public static void main(String[] args)
      { startSetup = System.currentTimeMillis();
        String myName = args[0];
        int rounds = Integer.parseInt(args[1]);
        if (args.length == 3)
          ARRAY_SIZE = Integer.parseInt(args[2]);
        int key = "BPingPongShm1".hashCode();

        try
          { new BPingPongShm1(key, myName).play(rounds);
          }
        catch (Exception exc)
          { System.err.println("I/O Error: " + exc);
            exc.printStackTrace();
          }
      } // main

    private void play (int rounds) throws IOException, ClassNotFoundException
      { endSetup = System.currentTimeMillis();
        System.out.println(myName + " starting rounds.");
        for (int i = 0; i < rounds; i++)
          { if (myName.equals("Ping"))
              { // Ping throws then catches
                throwDateBall(i);
                catchBall(i);
              }
            else
              { // Pong catches then throws
                PingPongBall d = catchDateBall(i);
                throwBall(d, i);
              }
          }
        if (myName.equals("Ping")) // Report average trip time
          { System.out.println("Data size: " + ARRAY_SIZE);
            System.out.println("Average trip time (ns): " + (total/(rounds-1.0)) + " using OLD shared memory");
          }
        System.out.println("Setup time: " + (endSetup-startSetup));
        try
          { Thread.sleep(1000);
          }
        catch (InterruptedException e)
          { // Ignore
          }
      } // play

    private void throwBall (PingPongBall d, int i) throws IOException
      { // System.out.println(myName + " throwing " + d);
        outStream.writeObject(d);
        outStream.flush();
        // System.out.println(myName + " threw " + i);
      } // throwBall

    private void throwDateBall (int i) throws IOException
      { // System.out.println(myName + " throwing");
        PingPongBall b = new PingPongBall();
        b.data = new byte[ARRAY_SIZE];
        b.time = System.nanoTime();
        outStream.writeObject(b);
        outStream.flush();
        // System.out.println(myName + " threw " + i);
      } // throwBall

    private PingPongBall catchDateBall(int i) throws IOException, ClassNotFoundException
      { PingPongBall d = null;

        // System.out.println(myName + " catching " + i);
        d = (PingPongBall)inStream.readObject();
        // System.out.println(myName + " caught " + i);
        return d;
      } // catchBall

    private void catchBall(int i) throws IOException, ClassNotFoundException
      { PingPongBall d = null;
        long time;

        // System.out.println(myName + " catching " + i);
        d = (PingPongBall)inStream.readObject();
        // System.out.println(myName + " caught " + i);
        time = System.nanoTime()-d.time;
        System.out.println("Round trip time = " + time);
        if (i > 0)
          total += time;
      } // catchBall

  } // class BPingPongShm1
