
import java.io.IOException;
import java.util.Arrays;

public class SharedMemoryPingPong {
    
    private static final int MAX_BUF_SIZE = 4096;
    private static final int MAX_SHM_SIZE = 1024*40;
    private SharedMemoryChannel writeSegment;
    private SharedMemoryChannel readSegment;
    private String myName;
    private Long Total;
    
    public SharedMemoryPingPong(String name) throws IOException {
        this.myName = name;
        this.Total = 0L;
        int pingKey = ((int)"Ping".hashCode() ^ 42);
        int pongKey = ((int)"Pong".hashCode() ^ 42);

        if (myName.equals("Ping")) {
            writeSegment = new SharedMemoryChannel(pingKey, MAX_SHM_SIZE, false);
            readSegment = new SharedMemoryChannel(pongKey, MAX_SHM_SIZE, false);
        } else {
            writeSegment = new SharedMemoryChannel(pongKey, MAX_SHM_SIZE, false);
            readSegment = new SharedMemoryChannel(pingKey, MAX_SHM_SIZE, false);
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
            writeSegment.write(data);
            readSegment.read(data.length);
        } else {
            writeSegment.write(readSegment.read(data.length));
        }
    }

    private int getRoundTripTime(int rounds, int size) throws IOException {
        byte[] data = new byte[size];
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                writeSegment.write(data);
                readSegment.read(data.length);
                Long end = System.nanoTime();
                Total += end - start;
            } else {
                writeSegment.write(readSegment.read(data.length));
            }
        }
        return (int) (Total/(rounds));
        
    }

    private void closeSharedMemory() {
        writeSegment.close(true);
        readSegment.close(true);
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