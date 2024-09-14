import java.io.IOException;

public class NamedPipePingPong {
    
    private static final String PING2PONG = "/tmp/fifo_ping2pong";
    private static final String PONG2PING = "/tmp/fifo_pong2ping";
    private NamedPipeChannel pipe;
    private String myName;
    private String yourName;
    private Long Total = 0L;
    
    public NamedPipePingPong(String myName) throws IOException {
        if (myName.equals("Ping")) {
            pipe = new NamedPipeChannel(PING2PONG);
            yourName = "Pong";
        } else if (myName.equals("Pong")) {
            yourName = "Ping";
            pipe = new NamedPipeChannel(PONG2PING);
        }
    }

    public void playSimulation(int rounds) throws IOException {
        setChannels();
        synchronize();
        getRoundTripTime(rounds);
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
        if (myName.equals("Ping")) {
            pipe.writeLong(0);
            pipe.readLong();
        } else {
            pipe.readLong();
            pipe.writeLong(0);
        }
    }

    private void getRoundTripTime(int rounds) throws IOException {
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                pingThrowBall();
                Total += pingCatchBall(); 
                
            } else {
                Long time = pongCatchBall();
                pongThrowBall(time);
            }
        }
        System.err.printf("Total round trip time: %L for rounds: %d", Total, rounds);
    }

    private void pingThrowBall() throws IOException {
        Long time = System.currentTimeMillis();
        pipe.writeLong(time);
    }

    private Long pingCatchBall() throws IOException {
        return pipe.readLong();
    }

    private void pongThrowBall(Long time) throws IOException {
        pipe.writeLong(time);
    }

    private Long pongCatchBall() throws IOException {
        return pipe.readLong();
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