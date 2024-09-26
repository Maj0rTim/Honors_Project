import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageQueueChannel 
{
    public static final int QUEUE_TYPE = 9999;
    public static final int MAX_BUF_SIZE = 4096;
    private int msgqid;
    private ByteBuffer buffer;
    private LinuxNIPC ipc = new LinuxNIPC();
    
    public MessageQueueChannel(int key) { 
        
        if ((msgqid = ipc.msgget(key, LinuxNIPC.IPC_CREAT | 0660)) == -1)
        System.err.println("MessageQueueOutput: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        buffer = ByteBuffer.allocateDirect(MAX_BUF_SIZE);
    }

    public int getMessageQueueID() {
        return this.msgqid;
    }

    public Long readLong() throws IOException {
        buffer.clear();
        if ((ipc.msgrcv(msgqid, buffer, 8, QUEUE_TYPE, 0)) == -1) { 
            throw new IOException("MessageQueueInput: read failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); 
        }
        return buffer.getLong();
    }

    public void writeLong(long l) throws IOException { 
        buffer.clear();
        buffer.putLong(l);
        buffer.flip();
        if (ipc.msgsnd(msgqid, buffer, 8, QUEUE_TYPE, 0) == -1) {
            throw new IOException("MessageQueueOutput: msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        }
    } 
    
    public void closeMessageQueue() throws IOException {
        ipc.msgRmid(msgqid);
    }
    
}