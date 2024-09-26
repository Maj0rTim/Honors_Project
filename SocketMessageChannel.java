import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketMessageChannel {
    
    private static final int MAX_BUF_SIZE = 4096;
    private SocketChannel readChannel;
    private SocketChannel writeChannel;
    private ByteBuffer buffer;

    public SocketMessageChannel(String host, int port) throws IOException {
        buffer = ByteBuffer.allocate(MAX_BUF_SIZE);
        System.out.println("Buffer Allocated");
    }

    public void setReadChannel(String host, int port) throws IOException {
        readChannel = SocketChannel.open(new InetSocketAddress(host, port));
        readChannel.configureBlocking(true);
        System.out.println("Read Channel Created");
    }
    
    public void setWriteChannel(String host, int port) throws IOException {
        writeChannel = SocketChannel.open(new InetSocketAddress(host, port));
        writeChannel.configureBlocking(true);
        System.out.println("Write Channel Created");
    }

    public void write(byte[] data) throws IOException {
        buffer.clear();
        buffer.put(data);
        while (buffer.hasRemaining()) {
            writeChannel.write(buffer);
        }
    }

    public byte[] read(int totalBytes) throws IOException {
        int totalBytesRead = 0;
        byte[] totalMessage = new byte[totalBytes];
        while (totalBytesRead < totalBytes) {
            buffer.clear();
            int bytesRead = readChannel.read(buffer);
            buffer.flip();
            buffer.get(totalMessage, totalBytesRead, bytesRead);
            totalBytesRead += bytesRead;
        }
        return totalMessage;
    }

    public void closeSocketChannels() throws IOException {
        readChannel.close();
        writeChannel.close();
    }
}
