import java.io.IOException;
public class SocketChannelPingPong {

    private String myName;
    private SocketMessageChannel socket;
    private Long Total;

    public SocketChannelPingPong(String name, String host, int port) throws IOException {
        this.myName = name;
        this.Total = 0L;
        if (myName.equals("Ping")) {
            socket = new SocketMessageChannel(host, port);
        } else if (myName.equals("Pong")) {
            socket = new SocketMessageChannel(host, port);
        }
    }

    public void playSimulation(int rounds) throws IOException {
        setChannels();
        synchronize();
        getRoundTripTime(rounds);
        closeChannels();
    }

    private void setChannels() throws IOException {
        if (myName.equals("Ping")) {
            
        } else {
            
        }
    }

    private void synchronize() throws IOException {
        if (myName.equals("Ping")) {
            socket.writeLong(0);
            socket.readLong();
        } else {
            socket.readLong();
            socket.writeLong(0);
        }
    }
    public static void main(String[] args) {
        
    }
}
