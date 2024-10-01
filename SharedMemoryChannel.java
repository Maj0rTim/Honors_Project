import java.io.IOException;
import java.nio.ByteBuffer;

public class SharedMemoryChannel {
    
    private LinuxNIPC ipc = new LinuxNIPC();
    public static final int MAX_BUF_SIZE = 4092;
    ByteBuffer buffer;
    private int shmid;
    private int shmaddr;
    private int semid;
    
    public SharedMemoryChannel(int key, int size, boolean initSems) throws IOException {
        createSharedMemorySegment(key, size, initSems);
    }

    public SharedMemoryChannel(int key, Boolean initSems) throws IOException {
        createSharedMemorySegment(key, MAX_BUF_SIZE, false);
    }

    private void createSharedMemorySegment(int key, int size, boolean initSems) throws IOException {
        ipc.initShrSem(key, size, initSems ? 1 : 0);
    }

    public void initFields (int shmid, int shmaddr, int semid) {
        this.shmid = shmid;
        this.shmaddr = shmaddr;
        this.semid = semid;
        System.out.println("Shared memory segment setup complete");
    }

    public void write(byte[] data) throws IOException {
        int totalBytesWritten = 0;
        int totalBytes = data.length;
        while (totalBytesWritten < totalBytes) {
            buffer.clear();
            int bytesToWrite = Math.min(MAX_BUF_SIZE, totalBytes - totalBytesWritten);
            buffer.put(data, totalBytesWritten, bytesToWrite);
            ipc.sendMsg(shmaddr, semid, buffer, totalBytesWritten, bytesToWrite);
            totalBytesWritten += bytesToWrite;
        }
    }

    public byte[] read(int totalBytes) throws IOException {
        int totalBytesRead = 0;
        byte[] totalMessage = new byte[totalBytes];
        while (totalBytesRead < totalBytes) {
            buffer.clear();
            int bytesRead = ipc.getMsg(shmaddr, semid, buffer);
            buffer.flip();
            int bytesToRead = Math.min(bytesRead, totalBytes - totalBytesRead);
            buffer.get(totalMessage, totalBytesRead, bytesToRead);
            totalBytesRead += bytesRead;
        }
        return totalMessage;
    }

    public void close(Boolean removeIds) {
        ipc.closeShm(shmid, shmaddr, semid, removeIds ? 1 : 0);
    }

}
