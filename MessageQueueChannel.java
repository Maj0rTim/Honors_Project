import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageQueueChannel 
{
    public static final int QUEUE_TYPE = 9999;
    public static final int MAX_BUF_SIZE = 4096;

    private int key;
    private int msgqid;
    private int dataSize;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
    private LinuxIPC ipc = new LinuxIPC();
    
    public MessageQueueChannel() { 
        if ((key = ipc.ftok("/home/csgw/JavaProgs/JNI/MessageQueue", 'a')) != -1) { 
            System.out.println("ftok succeeded.  key = " + key);
        } else { System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
        if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) == -1)
        System.err.println("MessageQueueOutputStream: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
    }

    public byte[] read() throws IOException {
        if ((dataSize = ipc.msgrcv(msgqid, buffer, MAX_BUF_SIZE, QUEUE_TYPE, 0)) == -1) { 
            throw new IOException("MessageQueueInputStream: fillBuffer failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); 
        }
        return buffer.array();
    }

    private void sendData(ByteBuffer buffer) throws IOException {
        if (buffer.hasArray()) {
            if (ipc.msgsnd(msgqid, QUEUE_TYPE, buffer, buffer.capacity(), 0) == -1) {
              throw new IOException("MessageQueueOutputStream: msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            }
        } else { throw new IOException("Buffer is empty"); }
    } 

    public void write(byte[] data) throws IOException { 
        buffer = ByteBuffer.wrap(data);
        sendData(buffer);
    } 

    public void write(byte[] data, int off, int len) throws IOException { 
        buffer = ByteBuffer.wrap(data);
        sendData(buffer.get(data, off, len));
    } 
    
    
}