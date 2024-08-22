import java.io.IOException;
import java.nio.ByteBuffer;

public class SharedMemoryChannel {
    
    ByteBuffer buffer;
    private LinuxNIPC ipc = new LinuxNIPC();

    public SharedMemoryChannel() {

    }

    public byte[] read() throws IOException {
        return null;
    }

    public void write() throws IOException {

    }

}
