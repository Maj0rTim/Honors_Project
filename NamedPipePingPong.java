import java.io.IOException;

public class NamedPipePingPong {
    
    private static final String PING2PONG = "/tmp/fifo_ping2pong";
    private static final String PONG2PING = "/tmp/fifo_pong2ping";
    private NamedPipeChannel pipe;
    private String myName;
    private String yourName;
    
    public NamedPipePingPong(String myName) throws IOException {
        if (myName.equals("Ping")) {
            yourName = "Pong";
            pipe = new NamedPipeChannel(PING2PONG);
        } else if (myName.equals("Pong")) {
            yourName = "Ping";
            pipe = new NamedPipeChannel(PONG2PING);
        }
    }

    public void playSimulation(int rounds) throws IOException {
        synchronize();
        
        long totalRoundTripTime = 0;
        for (int i = 0; i < rounds; i++) {
            long roundTripTime = measureRoundTripTime();
            totalRoundTripTime += roundTripTime;
            System.out.println(myName + " - Round " + (i + 1) + " round-trip time: " + roundTripTime + " ms");
        }
        
        if (myName.equals("Ping")) {
            System.out.println("Average round-trip time: " + (totalRoundTripTime / rounds) + " ms");
        }
    }

    private void synchronize() throws IOException {
        if (myName.equals("Ping")) {
            pipe.writeLong(System.nanoTime());
            pipe.readLong();
        } else {
            pipe.readLong();
            pipe.writeLong(System.nanoTime());
        }
    }
    
    private long measureRoundTripTime() throws IOException {
        long startTime = System.nanoTime();
        pipe.writeLong(startTime);
        long responseTime = pipe.readLong();
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 2) {
            System.out.println("Usage: java NamedPipePingPong <Ping|Pong> <rounds>");
            return;
        }

        String myName = args[0];
        int rounds = Integer.parseInt(args[1]);
        
        NamedPipePingPong simulation = new NamedPipePingPong(myName);
        simulation.playSimulation(rounds);
    }
}