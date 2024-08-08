import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageQueueInputChannel 
{
    public static final int QUEUE_TYPE = 9999;
    public static final int MAX_BUF_SIZE = 4096;

    private int msgqid;
    private int messageSize;
    private byte[] buf = new byte[MAX_BUF_SIZE];
    private ByteBuffer buffer = ByteBuffer.wrap(buf);
    private LinuxIPC ipc = new LinuxIPC();
    
    public MessageQueueInputChannel(int key) { 
        if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) == -1)
        System.err.println("MessageQueueOutputStream: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
    }

    private void getMessage()  throws IOException { 
        if ((messageSize = ipc.msgrcv(msgqid, buf, buf.length, QUEUE_TYPE, 0)) == -1)
        { throw new IOException("MessageQueueInputStream: fillBuffer failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
    }

    public void close() throws IOException { 
        buffer.flip();
        if (ipc.msgRmid(msgqid) == -1)
        { throw new IOException("MessageQueueInputStream: close failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); }
    } 
}