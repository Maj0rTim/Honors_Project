import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class SocketChannelPingPong {

    private String HOST;
    private static final int PINGPORT = 8851;
    private static final int PONGPORT = 8852;
    private String myName;
    private SocketMessageChannel socket;
    
    private Long Total;

    public SocketChannelPingPong(String name) throws IOException, UnknownHostException {
        this.HOST = InetAddress.getLocalHost().getHostName();
        this.myName = name;
        this.Total = 0L;
        if (myName.equals("Ping")) {
            socket = new SocketMessageChannel(true);
        } else if (myName.equals("Pong")) {
            socket = new SocketMessageChannel(false);
        }
    }

    public void playSimulation(int rounds) throws IOException {
        int[] results = new int[40];
        setChannels();
        synchronize();
        for (int i=0; i<results.length; i++) {
            int size = 1024*i;
            int result = getRoundTripTime(rounds, size);
            results[i] = result;
            Total = 0L;
        }
        closeChannels();
        System.out.println(Arrays.toString(results));
    }

    private void setChannels() throws IOException {
        if (myName.equals("Ping")) {
            socket.setWriteChannels(HOST, PINGPORT);
            socket.setReadChannels(HOST, PONGPORT);
        } else {
            socket.setReadChannels(HOST, PINGPORT);
            socket.setWriteChannels(HOST, PONGPORT);
        }
    }

    private void synchronize() throws IOException {
        byte[] data = new byte[1024];
        if (myName.equals("Ping")) {
            socket.write(data);
            socket.read(data.length);
        } else {
            socket.write(socket.read(data.length));
        }
    }

    private int getRoundTripTime(int rounds, int size) throws IOException {
        byte[] data = new byte[size];
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                socket.write(data);
                socket.read(data.length);
                Long end = System.nanoTime();
                Total += end - start;
            } else {
                socket.write(socket.read(data.length));
            }
        }
        return (int)(Total/rounds);
    }

    private void closeChannels() throws IOException {
        socket.closeChannels();
    }

    public static void main(String[] args) throws IOException {
        int rounds = Integer.parseInt(args[1]);
        SocketChannelPingPong simulation = new SocketChannelPingPong(args[0]);
        simulation.playSimulation(rounds);
    }
}
