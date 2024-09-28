import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SocketChannelPingPong {

    private String HOST;
    private static final int PINGPORT = 8851;
    private static final int PONGPORT = 8852;
    private static final int SIZE = 40;
    private String myName;
    private SocketMessageChannel socket;
    private Long Total;

    public SocketChannelPingPong(String name) throws IOException, UnknownHostException {
        this.HOST = InetAddress.getLocalHost().getHostName();
        this.myName = name;
        this.Total = 0L;
        if (myName.equals("Ping")) {
            socket = new SocketMessageChannel(HOST, PINGPORT);
        } else if (myName.equals("Pong")) {
            socket = new SocketMessageChannel(HOST, PONGPORT);
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
            socket.setReadChannel(HOST, PONGPORT);
            socket.setWriteChannel(HOST, PINGPORT);
        } else {
            socket.setReadChannel(HOST, PINGPORT);
            socket.setWriteChannel(HOST, PONGPORT);
        }
    }

    private void synchronize() throws IOException {
        byte[] data = new byte[8];
        if (myName.equals("Ping")) {
            socket.write(data);
            socket.read(8);
        } else {
            socket.read(8);
            socket.write(data);
        }
    }

    private void getRoundTripTime(int rounds) throws IOException {
        byte[] data = new byte[SIZE];
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                socket.write(data);
                socket.read(SIZE);
                Long end = System.nanoTime();
                Total += end - start;
            } else {
                socket.write(socket.read(SIZE));
            }
        }
        System.out.println(Total/rounds);
    }

    private void closeChannels() throws IOException {
        socket.closeReadChannel();
        socket.closeWriteChannel();
    }

    public static void main(String[] args) throws IOException {
        int rounds = Integer.parseInt(args[1]);
        SocketChannelPingPong simulation = new SocketChannelPingPong(args[0]);
        simulation.playSimulation(rounds);
    }
}
