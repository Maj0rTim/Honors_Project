import java.io.IOException;

public class NamedPipePingPong {
    
    private static final String PING2PONG = "/tmp/fifo_ping2pong";
    private static final String PONG2PING = "/tmp/fifo_pong2ping";
    private NamedPipeChannel Pipe;
    private String myName;
    private String yourName;
    private long total;
    private static long startSetup;
    private long endSetup;

    public NamedPipePingPong(String myName) throws IOException {
        if (myName == "Ping") {
            yourName = "Pong";
            Pipe = new NamedPipeChannel(PING2PONG);
        } else if (myName == "Pong") {
            yourName = "Ping";
            Pipe = new NamedPipeChannel(PONG2PING);
        }
    }

    public void playSimulation(int rounds) throws IOException {
        long start = System.currentTimeMillis();
        for (int i=0; i<rounds; i++) {
            if (myName == "Ping") {
                throwMessage();
            } else {
                Long finish = catchMessage();
            }
        }
    }

    public void throwMessage() throws IOException {
        Pipe.write(System.currentTimeMillis());
    }

    public Long catchMessage() throws IOException {
        Long time = Pipe.read();
        return time;
    }
    

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        startSetup = System.currentTimeMillis();
        String myName = args[0];
        int rounds = Integer.parseInt(args[1]);
        NamedPipeChannel pipe = new NamedPipeChannel(myName);
        new NamedPipePingPong(myName).playSimulation(rounds);
    }

}