
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
    private final int PERMISSIONS = 0660;
    private ByteBuffer buffer;
    private FileChannel channel;
    

    public NamedPipeChannel() throws IOException {
        if (ipc.mkfifo(PATH, PERMISSIONS) == 0) { System.out.println("mkfifo succeeded"); }
        else { System.out.println("mkfifo failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
        Path filePath = Paths.get(PATH);
        channel = FileChannel.open(filePath, StandardOpenOption.WRITE, StandardOpenOption.READ);
    }   
    
    public void fillBuffer() throws IOException {
        buffer = ByteBuffer.allocate((int) channel.size());
        channel.read(buffer);
    }

    public byte[] readBuffer() throws IOException{
        if (buffer.hasArray()) { return buffer.array(); }
        else { throw new IOException("No data in buffer"); }        
    }

    public void writeBuffer(byte[] data) throws IOException {
        buffer = ByteBuffer.wrap(data);
        if  (buffer.hasRemaining()) { channel.write(buffer); }
        else { throw new IOException("Supplied no data"); }
        
    }
}