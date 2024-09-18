import java.io.IOException;
import java.nio.ByteBuffer;

public class SharedMemoryChannel {
    
    private LinuxNIPC ipc = new LinuxNIPC();
    public static final int MAX_BUF_SIZE = 4092;
    ByteBuffer buffer;
    private int shmid; // Shared memory segment identifier
    private int shmaddr; // Semaphore set identifier
    private int semid; // Semaphore set identifier

    public SharedMemoryChannel(int key, int size, boolean initSems) {
        createSharedMemorySegment(key, size, initSems);
    }

    public SharedMemoryChannel(int key, Boolean initSems) {
        createSharedMemorySegment(key, MAX_BUF_SIZE, false);
    }

    private void createSharedMemorySegment(int key, int size, boolean initSems) throws IOException {
        ipc.initShrSem(key, size, initSems ? 1 : 0);
    }

    public void read() throws IOException {
        
    }

    public byte[] write() throws IOException {
        getMsg();
        return 
    }

    public void close() {
        ipc.close();
    }

}
