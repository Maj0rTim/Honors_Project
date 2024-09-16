import java.io.IOException;
import java.nio.ByteBuffer;

public class SharedMemoryChannel {
    
    ByteBuffer buffer;
    private LinuxNIPC ipc = new LinuxNIPC();
    public static final int DEF_BUF_SIZE = 4092;

    public SharedMemoryChannel() {
        ipc.initStream(key, size, initSems ? 1 : 0);
    }

    public byte[] read() throws IOException {
        return null;
    }

    public void write() throws IOException {

    }

}
