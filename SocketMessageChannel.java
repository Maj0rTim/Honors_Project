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

    public void closeReadChannel() throws IOException {
        readChannel.close();
        System.out.println("Read Channel Closed");
    }

    public void closeWriteChannel() throws IOException {
        writeChannel.close();
        System.out.println("Write Channel Closed");
    }

    public void write(byte[] data) throws IOException {
        int totalBytesWritten = 0;
        int totalBytes = data.length;
        while (totalBytesWritten < totalBytes) {
            buffer.clear();
            int bytesToWrite = Math.min(MAX_BUF_SIZE, totalBytes - totalBytesWritten);
            buffer.put(data, totalBytesWritten, bytesToWrite);
            buffer.flip();
            while (buffer.hasRemaining()) {
                writeChannel.write(buffer);
            }
        }
    }

    public byte[] read(int totalBytes) throws IOException {
        int totalBytesRead = 0;
        byte[] totalMessage = new byte[totalBytes];
        while (totalBytesRead < totalBytes) {
            buffer.clear();
            int bytesRead = readChannel.read(buffer);
            if (bytesRead == -1) { break; }
            buffer.flip();
            int bytesToRead = Math.min(bytesRead, totalBytes - totalBytesRead);
            buffer.get(totalMessage, totalBytesRead, bytesToRead);
            totalBytesRead += bytesRead;
        }
        return totalMessage;
    }
}
