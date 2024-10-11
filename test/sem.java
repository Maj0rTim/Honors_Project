import java.io.IOException;
import java.nio.ByteBuffer;

public class sem {

    public static final int MAX_SIZE = 1024;
    private ByteBuffer buffer;
    private int shmid;
    private int shmaddr;
    private int semid;
    private int errnum;

    public sem() {
        int key = ((int)"KEY".hashCode() ^ 42);
        this.initShrSem(key,  MAX_SIZE);
        buffer = ByteBuffer.allocateDirect(MAX_SIZE);
    }

    public native void initShrSem (int key, int size);

    public void initFields (int shmid, int shmaddr, int semid) {
        this.shmid = shmid;
        this.shmaddr = shmaddr;
        this.semid = semid;
    }

    public void write() {
        byte[] data = new byte[MAX_SIZE];
        this.buffer.put(data);
        this.sendMsg(shmaddr, semid, buffer, MAX_SIZE);
    }

    public native void sendMsg (int shmaddr, int semid, ByteBuffer buffer, int len);

    public void read() {
        byte[] totalMessage = new byte[MAX_SIZE];
        this.getMsg(shmaddr, semid, buffer, MAX_SIZE);
        buffer.get(totalMessage, 0, MAX_SIZE);
    }

    public native void getMsg (int shmaddr, int semid, ByteBuffer buffer, int len);

    public void close() {
        closeShm(shmid, shmaddr, semid, 1);
    }

    public native void closeShm (int shmid, int shmaddr, int semid, int removeIds);

    public void setErrnum (int errnum) { 
        this.errnum = errnum;
      }

    static { 
        System.loadLibrary("sem");
      } 

    public static void main(String[] args) {
        sem sem = new sem();
        sem.write();
        sem.close();
    }
}