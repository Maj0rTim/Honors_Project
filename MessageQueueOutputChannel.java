import java.io.IOException;

public class MessageQueueOutputChannel 
{
    public static final int QUEUE_TYPE = 9999;
    public static final int MAX_BUF_SIZE = 4096;

    private int msgqid; // The internal message queue identifier
    private LinuxIPC ipc = new LinuxIPC(); // The native IPC library
    private byte[] maxBuffer = new byte[MAX_BUF_SIZE];
    
    public MessageQueueOutputChannel (int key)
    { 
        if ((msgqid = ipc.msgget(key, LinuxIPC.IPC_CREAT | 0660)) == -1)
        System.err.println("MessageQueueOutputStream: msgget failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
    } // constructor

    
    
}