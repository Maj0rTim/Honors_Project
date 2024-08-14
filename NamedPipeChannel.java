
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NamedPipeChannel
{
    private LinuxIPC ipc = new LinuxIPC();
    private final String PATH = "/tmp/fifo_temp";
    private static final int PERMISSIONS = 0660;
    private static final int MAX_BUF_SIZE = 4096;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
    private FileChannel channel;
    
    public NamedPipeChannel() throws IOException {
        if (ipc.mkfifo(PATH, PERMISSIONS) == 0) { System.out.println("mkfifo succeeded"); }
        else { System.out.println("mkfifo failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
        Path filePath = Paths.get(PATH);
        channel = FileChannel.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.READ);
    }   

    public byte[] read() throws IOException {
        buffer.clear();
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) { throw new IOException("End of stream reached"); }
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return data;
    }

    public void write(byte[] data) throws IOException {
        int position = 0;
        while (position < MAX_BUF_SIZE) {
            buffer.clear();
            buffer.put(data, position, MAX_BUF_SIZE);
            buffer.flip();
            while (buffer.hasRemaining()) { channel.write(buffer); }
            position += MAX_BUF_SIZE;
        }
    }

    public void close() throws IOException {
        channel.close();
    }
}