import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SocketMessageChannel {
    
    private static final int MAX_BUF_SIZE = 4096;
    private ByteBuffer buffer;
    private SocketChannel readChannel;
    private SocketChannel writeChannel;
    private boolean isServer;

    public SocketMessageChannel(boolean isServer) throws IOException {
        this.buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
        this.isServer = isServer;
    }

    public void setReadChannels(String host, int port) throws IOException {
        if (isServer) {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.configureBlocking(true);
            readChannel = serverSocketChannel.accept();
            readChannel.configureBlocking(true);
        } else {
            readChannel = SocketChannel.open(new InetSocketAddress(host, port));
            readChannel.configureBlocking(true);
        }
    }

    public void setWriteChannels(String host, int port) throws IOException {
        if (isServer) {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.configureBlocking(true);
            writeChannel = serverSocketChannel.accept();
            writeChannel.configureBlocking(true);
        } else {
            writeChannel = SocketChannel.open(new InetSocketAddress(host, port));
            writeChannel.configureBlocking(true);
        }
    }

    public void closeChannels() throws IOException{
        readChannel.close();
        writeChannel.close();
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
            totalBytesWritten += bytesToWrite;
        }
    }

    public byte[] read(int totalBytes) throws IOException {
        int totalBytesRead = 0;
        byte[] totalMessage = new byte[totalBytes];
        while (totalBytesRead < totalBytes) {
            buffer.clear();
            int bytesRead = readChannel.read(buffer);
            buffer.flip();
            int bytesToRead = Math.min(bytesRead, totalBytes - totalBytesRead);
            buffer.get(totalMessage, totalBytesRead, bytesToRead);
            totalBytesRead += bytesRead;
        }
        return totalMessage;
    }
}
