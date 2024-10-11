import java.io.IOException;
import java.nio.ByteBuffer;


public class SharedMemoryChannel {
    
    public static final int MAX_BUF_SIZE = 1024;
    private ByteBuffer buffer;
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
    }

    public void write(byte[] data) throws IOException {
            sendMsg(shmaddr, semid, buffer, 0, MAX_BUF_SIZE);
    }

    public byte[] read(int totalBytes) throws IOException {
        byte[] totalMessage = new byte[totalBytes];
        getMsg(shmaddr, semid, buffer, totalBytes);
        buffer.get(totalMessage, 0, totalBytes);
        return totalMessage;
    }

    public void close(Boolean removeIds) {
        closeShm(shmid, shmaddr, semid, removeIds ? 1 : 0);
    }

    public native void initShrSem (int key, int size, int initSems);

    public native int sendMsg (int shmaddr, int semid, ByteBuffer buffer, int offset, int len);

    public native int getMsg (int shmaddr, int semid, ByteBuffer buffer, int len);

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
