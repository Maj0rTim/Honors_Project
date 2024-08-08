
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NamedPipeChannel
{
    private LinuxIPC ipc = new LinuxIPC();
    private static final String PATH = "/tmp/fifo_temp";
    public static final int PERMISSIONS = 0660;

    public NamedPipeChannel() {
        if (ipc.mkfifo(PATH, PERMISSIONS) == 0) { System.out.println("mkfifo succeeded for ping2pong"); }
        else { System.out.println("mkfifo failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
    }   
    
    public String read() throws IOException {
        String data = null;
        Path filePath = Paths.get(PATH);
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            if (buffer.hasArray()) { data = new String(buffer.array()); }
            else { throw new IOException("No data to read"); }
        }
        return data;
    }

    public void write(String data) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
        Path filePath = Paths.get(PATH);
        try (FileChannel channel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) 
        {
            while (buffer.hasRemaining()) { channel.write(buffer); }
        }  
    }
}