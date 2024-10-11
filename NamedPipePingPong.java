import java.io.IOException;
import java.util.Arrays;

public class NamedPipePingPong {
    
    private static final String PING2PONG = "/tmp/fifo_ping2pong";
    private static final String PONG2PING = "/tmp/fifo_pong2ping";
    private static final int MAX_BUF_SIZE = 4096;
    private NamedPipeChannel pipe;
    private String myName;
    private Long Total;
    
    public NamedPipePingPong(String name) throws IOException {
        this.myName = name;
        this.Total = 0L;
        if (myName.equals("Ping")) {
            pipe = new NamedPipeChannel(PING2PONG);
        } else if (myName.equals("Pong")) {
            pipe = new NamedPipeChannel(PONG2PING);
        }
    }

    public void playSimulation(int rounds) throws IOException {
        int[] results = new int[40];
        setChannels();
        synchronize();
        for (int i=0; i<results.length; i++) {
            int size = 1024*(i+1);
            int result = getRoundTripTime(rounds, size);
            results[i] = result;
            Total = 0L;
        }
        closeChannels();
        System.out.println(Arrays.toString(results));
    }

    private void setChannels() throws IOException {
        if (myName.equals("Ping")) {
            pipe.setWriteChannel(PING2PONG);
            pipe.setReadChannel(PONG2PING);
        } else {
            pipe.setReadChannel(PING2PONG);
            pipe.setWriteChannel(PONG2PING);
        }
    }

    private void synchronize() throws IOException {
        byte[] data = new byte[MAX_BUF_SIZE/2];
        if (myName.equals("Ping")) {
            pipe.write(data);
            pipe.read(data.length);
        } else {
            pipe.read(data.length);
            pipe.write(data);
        }
    }

    private int getRoundTripTime(int rounds, int size) throws IOException {
        byte[] data = new byte[size];
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                pipe.write(data);
                pipe.read(data.length);
                Long end = System.nanoTime();
                if (i != 0) {
                    Total += end - start;
                }
            } else {
                pipe.write(pipe.read(data.length));
            }
        }
        return (int) (Total/(rounds-1));
        
    }

    private void closeChannels() throws IOException {
        pipe.closeReadChannel();
        pipe.closeWriteChannel();
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