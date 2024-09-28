
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