
import java.io.IOException;
import java.util.Arrays;

public class SharedMemoryPingPong {
    
    private static final int MAX_BUF_SIZE = 1024;
    private static final int MAX_SHM_SIZE = 1024;
    private SharedMemoryChannel writeSegment;
    private SharedMemoryChannel readSegment;
    private String myName;
    private Long Total;
    
    public SharedMemoryPingPong(String name) throws IOException {
        this.myName = name;
        this.Total = 0L;
        int pingKey = ((int)"Ping".hashCode() ^ 42);
       
        if (myName.equals("Ping")) {
            writeSegment = new SharedMemoryChannel(pingKey, MAX_SHM_SIZE, true);
            readSegment = new SharedMemoryChannel(pingKey, MAX_SHM_SIZE, false);
            System.out.println("segemntes created!");
        }
    }

    public void playSimulation(int rounds) throws IOException {
        synchronize();
        closeSharedMemory();
    }

    private void synchronize() throws IOException {
        byte[] data = new byte[MAX_BUF_SIZE];
        if (myName.equals("Ping")) {
            writeSegment.write(data);
            readSegment.read(data.length);
        }
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