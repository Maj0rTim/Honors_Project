import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketMessageChannel {
    
    int port;
    String host;
    private static final int MAX_BUF_SIZE = 4096;
    private SocketChannel channel;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);

    public SocketMessageChannel(String host, int port) throws IOException {
        channel = SocketChannel.open(new InetSocketAddress(host, port));
        channel.configureBlocking(false);
    }

    public void write(byte[] data) throws IOException {
        buffer.clear();
        buffer.put(data);
        while (buffer.hasRemaining()) { channel.write(buffer); }
    }

    public byte[] read() throws IOException {
        buffer.clear();
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) { throw new IOException("Connection closed by remote host"); }
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }

    public void closeConnection() throws IOException {
        channel.close();
    }
}
