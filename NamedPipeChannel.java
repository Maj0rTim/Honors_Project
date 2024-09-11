
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NamedPipeChannel
{
    private LinuxNIPC ipc = new LinuxNIPC();
    private static final int PERMISSIONS = 0660;
    private static final int MAX_BUF_SIZE = 4096;
    private ByteBuffer buffer;
    private FileChannel channel;
    
    public NamedPipeChannel(String path) throws IOException {
        if (ipc.mkfifo(path, PERMISSIONS) == 0) { System.out.println("mkfifo succeeded"); }
        else { System.out.println("mkfifo failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
        Path filePath = Paths.get(path);
        buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
        channel = FileChannel.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.READ);
    }   

    public Long read() throws IOException {
        buffer.clear();
        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) { throw new IOException("End of stream reached"); }
        buffer.flip();
        Long data = buffer.getLong();
        return data;
    }

    public void write(Long data) throws IOException {
        int position = 0;
        while (position < buffer.capacity()) {
            buffer.clear();
            buffer.putLong(position, data);
            buffer.flip();
            while (buffer.hasRemaining()) { channel.write(buffer); }
            position += MAX_BUF_SIZE;
        }
    }

    public void close() throws IOException {
        channel.close();
    }
}