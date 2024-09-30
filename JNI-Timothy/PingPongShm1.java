import java.util.*;
import java.io.*;

/** Uses old shared memory for communication, and nanosecond timing.
  */
public class PingPongShm1
  { private String myName;
    private String yourName;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    private long total = 0;
    private static long startSetup;
    private long endSetup;
    
    public PingPongShm1 (int key, String myName) throws IOException
      { int pongKey = "pong".hashCode() ^ key;
        int pingKey = "ping".hashCode() ^ key;
        this.myName = myName;
        
        System.out.println(myName + " starting initialisation.");
        System.out.println(myName + " pongKey = " + pongKey);
        System.out.println(myName + " pingKey = " + pingKey);
        if (myName.equals("Ping"))
          { yourName = "Pong";
            outStream = new ObjectOutputStream(new BufferedOutputStream(new SharedMemoryOutputStream1(pongKey)));
            outStream.flush();
            System.out.println(myName + " outStream created.");
            inStream = new ObjectInputStream(new SharedMemoryInputStream1(pingKey));
            System.out.println(myName + " inStream created.");
          }
        else
          { yourName = "Ping";
            outStream = new ObjectOutputStream(new BufferedOutputStream(new SharedMemoryOutputStream1(pingKey)));
            outStream.flush();
            System.out.println(myName + " outStream created.");
            inStream = new ObjectInputStream(new SharedMemoryInputStream1(pongKey));
            System.out.println(myName + " inStream created.");
          }
        System.out.println(myName + " initialisation complete.");
      } // constructor

    public static void main(String[] args)
      { startSetup = System.currentTimeMillis();
        String myName = args[0];
        int rounds = Integer.parseInt(args[1]);
        int key = "PingPongShm1".hashCode();
        System.out.println("Class key = " + key);

        try
          { new PingPongShm1(key, myName).play(rounds);
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
                Long d = catchDateBall(i);
                throwBall(d, i);
              }
          }
        if (myName.equals("Ping")) // Report average trip time
          System.out.println("Average trip time (ns): " + (total/(rounds-1.0)) + " using OLD shared memory");
        System.out.println("Setup time: " + (endSetup-startSetup));
        outStream.close();
        inStream.close();
        try
          { Thread.sleep(1000);
          }
        catch (InterruptedException e)
          { // Ignore
          }
      } // play

    private void throwBall (Long d, int i) throws IOException
      { // System.out.println(myName + " throwing " + d);
        outStream.writeObject(d);
        outStream.flush();
        // System.out.println(myName + " threw " + i);
      } // throwBall

    private void throwDateBall (int i) throws IOException
      { // System.out.println(myName + " throwing");
        outStream.writeObject(new Long(System.nanoTime()));
        outStream.flush();
        // System.out.println(myName + " threw " + i);
      } // throwBall

    private Long catchDateBall(int i) throws IOException, ClassNotFoundException
      { Long d = null;

        // System.out.println(myName + " catching " + i);
        d = (Long)inStream.readObject();
        // System.out.println(myName + " caught " + i);
        return d;
      } // catchBall

    private void catchBall(int i) throws IOException, ClassNotFoundException
      { Long d = null;
        long time;

        // System.out.println(myName + " catching " + i);
        d = (Long)inStream.readObject();
        // System.out.println(myName + " caught " + i);
        time = System.nanoTime()-d;
        System.out.println("Round trip time = " + time);
        if (i > 0)
          total += time;
      } // catchBall

  } // class PingPongShm1
