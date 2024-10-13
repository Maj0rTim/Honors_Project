
import java.io.IOException;
import java.util.Arrays;

public class SharedMemoryPingPong {
    
    private static final int MAX_BUF_SIZE = 1024*40;
    private static final int MAX_SHM_SIZE = 1024*40;
    private SharedMemoryChannel pingSegment;
    private SharedMemoryChannel pongSegment;
    private String myName;
    private Long Total;
    
    public SharedMemoryPingPong(String name) throws IOException {
        this.myName = name;
        this.Total = 0L;
       
        if (myName.equals("Ping")) {
            pingSegment = new SharedMemoryChannel("Ping", MAX_SHM_SIZE, true);
            pongSegment = new SharedMemoryChannel("Ping", MAX_SHM_SIZE, true);
            System.out.println("segemntes created!");
        } else {
            pingSegment = new SharedMemoryChannel("Ping", MAX_SHM_SIZE, false);
            pongSegment = new SharedMemoryChannel("Pong", MAX_SHM_SIZE, false);
        }
    }

    public void playSimulation(int rounds) throws IOException {
        int[] results = new int[40];
        synchronize();
        for (int i=0; i<results.length; i++) {
            int size = 1024*(i+1);
            int result = getRoundTripTime(rounds, size);
            results[i] = result;
            Total = 0L;
        }
        closeSharedMemory();
        System.out.println(Arrays.toString(results));
    }

    private void synchronize() throws IOException {
        byte[] data = new byte[MAX_BUF_SIZE];
        if (myName.equals("Ping")) {
            pingSegment.write(data);
            pongSegment.read(data.length);
            pingSegment.write(data);
            pongSegment.read(data.length);
        } else {
            pongSegment.write(pingSegment.read(data.length));
        }
    }

    private int getRoundTripTime(int rounds, int size) throws IOException {
        byte[] data = new byte[size];
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                pingSegment.write(data);
                pongSegment.read(data.length);
                Long end = System.nanoTime();
                if (i != 0) {
                    Total += end - start;
                }
            } else {
                pongSegment.write(pingSegment.read(data.length));
            }
        }
        return (int)(Total/(rounds - 1));
    }

    private void closeSharedMemory() {
        pingSegment.close(true);
        pongSegment.close(true);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 2) {
            System.out.println("Usage: java NamedPipePingPong <Ping|Pong> <rounds>");
            return;
        }
        String myName = args[0];
        int rounds = Integer.parseInt(args[1]);
        SharedMemoryPingPong simulation = new SharedMemoryPingPong(myName);
        simulation.playSimulation(rounds);
    }
}