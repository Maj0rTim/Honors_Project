import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageQueueChannel 
{
    public static final int QUEUE_TYPE = 9999;
    public static final int MAX_BUF_SIZE = 4096;
    private int key;
    private int msgqid;
    private int dataSize;
    private ByteBuffer buffer;
    private LinuxNIPC ipc = new LinuxNIPC();
    
    public MessageQueueChannel(String path) { 
        if ((key = ipc.ftok(path, 'a')) != -1) { 
            System.out.println("ftok succeeded.  key = " + key);
        } else { System.out.println("ftok failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
        if ((msgqid = ipc.msgget(key, LinuxNIPC.IPC_CREAT | 0660)) == -1)
        System.err.println("MessageQueueOutput: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
    }

    public int getMessageQueueID() {
        return this.msgqid;
    }

    public Long readLong() throws IOException {
        buffer.clear();
        if ((dataSize = ipc.msgrcv(msgqid, QUEUE_TYPE, buffer, MAX_BUF_SIZE, 0)) == -1) { 
            throw new IOException("MessageQueueInput: read failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); 
        }
        buffer.flip();
        Long data = buffer.getLong();
        return data;
    }

    public void writeLong(long l) throws IOException { 
        buffer.clear();
        buffer.putLong(l);
        if (buffer.hasArray()) {
            if (ipc.msgsnd(msgqid, QUEUE_TYPE, buffer, buffer.capacity(), 0) == -1) {
                throw new IOException("MessageQueueOutput: msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            }
        } else { 
            throw new IOException("Buffer is empty"); 
        }
        
    } 
    
    public void closeMessageQueue() throws IOException {
        ipc.msgRmid(msgqid);
    }
    
}