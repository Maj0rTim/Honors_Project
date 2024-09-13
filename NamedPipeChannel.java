
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
    private FileChannel readChannel;
    private FileChannel writeChannel;
    
    public NamedPipeChannel(String path) throws IOException {
        if (ipc.mkfifo(path, PERMISSIONS) == 0) { System.out.println("mkfifo succeeded"); }
        else { System.out.println("mkfifo failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
        buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
        System.out.println("Direct Buffer Allocated");
    }

    public void setReadChannel(String path) throws IOException {
        Path filePath = Paths.get(path);
        readChannel = FileChannel.open(filePath, StandardOpenOption.READ);
        System.out.println("Read Channel Created");
    }
    
    public void setWriteChannel(String path) throws IOException {
        Path filePath = Paths.get(path);
        writeChannel = FileChannel.open(filePath, StandardOpenOption.WRITE);
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

    public long readLong() throws IOException {
        buffer.clear();
        int bytesRead = readChannel.read(buffer);
        if (bytesRead == -1) {
            throw new IOException("End of stream reached");
        }
        buffer.flip();
        return buffer.getLong();
    }

    public void writeLong(long data) throws IOException {
        buffer.clear();
        buffer.putLong(data);
        buffer.flip();
        while (buffer.hasRemaining()) {
            writeChannel.write(buffer);
        }
    }
}