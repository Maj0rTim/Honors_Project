
import java.io.IOException;

public class NamedPipePong {
    
    private static final String PING2PONG = "/tmp/fifo_ping2pong";
    private NamedPipeChannel pipe;
    
    public NamedPipePong() throws IOException {
        pipe = new NamedPipeChannel(PING2PONG);
        
    }

    public void playSimulation() throws IOException {
        synchronize();
    }

    private void synchronize() throws IOException {
        Long time = 0L;
        pipe.setReadChannel(PING2PONG);
        time = pipe.readLong();
        System.out.println("Recieved Message!!!");
        pipe.closeReadChannel();
        System.out.println(time);
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        NamedPipePong simulation = new NamedPipePong();
        simulation.playSimulation();
    }
}