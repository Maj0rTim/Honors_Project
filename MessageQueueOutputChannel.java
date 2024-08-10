import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageQueueOutputChannel 
{
    public static final int QUEUE_TYPE = 9999;

    private int msgqid;
    private ByteBuffer buffer;
    private LinuxIPC ipc = new LinuxIPC();
    
    public MessageQueueOutputChannel(int key)
    { 
        if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) == -1)
        System.err.println("MessageQueueOutputStream: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
    }

    private void sendData(ByteBuffer buffer) throws IOException
    {
        if (buffer.hasArray()) {
            if (ipc.msgsnd(msgqid, QUEUE_TYPE, buffer.array(), buffer.capacity(), 0) == -1)
              throw new IOException("MessageQueueOutputStream: msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
        } else { throw new IOException("Buffer is empty"); }
    } 

    public void write(byte[] data) throws IOException
    { 
        buffer = ByteBuffer.wrap(data);
        sendData(buffer);
    } 

    public void write(byte[] data, int off, int len) throws IOException
    { 
        buffer = ByteBuffer.wrap(data);
        sendData(buffer.get(data, off, len));
    } 
    
    
}