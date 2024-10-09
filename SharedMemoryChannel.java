import java.io.IOException;
import java.nio.ByteBuffer;


public class SharedMemoryChannel {
    
    public static final int MAX_BUF_SIZE = 4092;
    ByteBuffer buffer;
    private int shmid;
    private int shmaddr;
    private int semid;
    private int errnum;
    
    public SharedMemoryChannel(int key, int size, boolean initSems) throws IOException {
        createSharedMemorySegment(key, size, initSems);
    }

    public SharedMemoryChannel(int key, Boolean initSems) throws IOException {
        createSharedMemorySegment(key, MAX_BUF_SIZE, false);
    }

    private void createSharedMemorySegment(int key, int size, boolean initSems) throws IOException {
        initShrSem(key, size, initSems ? 1 : 0);
        buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
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
            System.out.println("1");
            int bytesToWrite = Math.min(MAX_BUF_SIZE, totalBytes - totalBytesWritten);
            buffer.put(data, totalBytesWritten, bytesToWrite);
            System.out.println("2");
            sendMsg(shmaddr, semid, buffer, totalBytesWritten, bytesToWrite);
            System.out.println("3");
            totalBytesWritten += bytesToWrite;
        }
    }

    public byte[] read(int totalBytes) throws IOException {
        int totalBytesRead = 0;
        byte[] totalMessage = new byte[totalBytes];
        while (totalBytesRead < totalBytes) {
            buffer.clear();
            System.out.println("1");
            int bytesRead = getMsg(shmaddr, semid, buffer);
            System.out.println("2");
            buffer.flip();
            int bytesToRead = Math.min(bytesRead, totalBytes - totalBytesRead);
            buffer.get(totalMessage, totalBytesRead, bytesToRead);
            System.out.println("3");
            totalBytesRead += bytesRead;
        }
        return totalMessage;
    }

    public void close(Boolean removeIds) {
        closeShm(shmid, shmaddr, semid, removeIds ? 1 : 0);
    }

    public native void initShrSem (int key, int size, int initSems);

    public native int sendMsg (int shmaddr, int semid, ByteBuffer buffer, int offset, int len);

    public native int getMsg (int shmaddr, int semid, ByteBuffer buffer);

    public native void closeShm (int shmid, int shmaddr, int semid, int removeIds);

    public native String strerror (int errnum);

    public int getErrnum () { 
      return errnum;
    }

    public void setErrnum (int errnum) { 
      this.errnum = errnum;
    }

    static { 
      System.loadLibrary("LinuxNIPC_shm");
    } 

}
