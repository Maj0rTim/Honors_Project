import java.util.*;
import java.io.*;
import java.net.*;

/** Uses network sockets (loopback) for communication, and nanosecond timing.
  */
public class BPingPongSocket
  { private static final int PING2PONG = 8851;
    private static final int PONG2PING = 8852;
    private String myName;
    private String yourName;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;
    private long total = 0;
    private static long startSetup;
    private long endSetup;
    private static int ARRAY_SIZE = 40000;
    
    public BPingPongSocket (String myName) throws IOException
      { this.myName = myName;
        
        System.out.println(myName + " starting initialisation.");
        if (myName.equals("Ping"))
          { yourName = "Pong";
            Socket sock1 = new Socket("127.0.0.1", PONG2PING);
            inStream = new ObjectInputStream(sock1.getInputStream());
            System.out.println(myName + " inStream created.");
            
            Socket sock2 = new Socket("127.0.0.1", PING2PONG);
            outStream = new ObjectOutputStream(sock2.getOutputStream());
            outStream.flush();
            System.out.println(myName + " outStream created.");
          }
        else // Pong - does socket setup
          { yourName = "Ping";
            ServerSocket s1 = new ServerSocket(PONG2PING, 5);
            Socket clientSock1 = s1.accept();
            outStream = new ObjectOutputStream(clientSock1.getOutputStream());
            outStream.flush();
            System.out.println(myName + " outStream created.");

            ServerSocket s2 = new ServerSocket(PING2PONG, 5);
            Socket clientSock2 = s2.accept();
            inStream = new ObjectInputStream(clientSock2.getInputStream());
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

        try
          { new BPingPongSocket(myName).play(rounds);
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
            System.out.println("Average trip time (ns): " + (total/(rounds-1.0)) + " using loopback sockets");
          }
        System.out.println("Setup time (ms): " + (endSetup-startSetup));
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
        System.out.println("Round trip time (ns.) = " + time);
        if (i > 0)
          total += time;
      } // catchBall

  } // class BPingPongSocket
