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

    public void write(byte[] data) throws IOException {
        int totalBytesWritten = 0;
        int totalBytes = data.length;
        while (totalBytesWritten < totalBytes) {
            buffer.clear();
            int bytesToWrite = Math.min(MAX_BUF_SIZE, totalBytes - totalBytesWritten);
            buffer.put(data, totalBytesWritten, bytesToWrite);
            if (ipc.msgsnd(msgqid, buffer, bytesToWrite, QUEUE_TYPE, 0) == -1) {
                throw new IOException("MessageQueueOutput: msgsnd failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum()));
            }
            totalBytesWritten += bytesToWrite;
        }
    }

    public byte[] read(int totalBytes) throws IOException {
        int totalBytesRead = 0;
        int bytesRead = 0;
        byte[] totalMessage = new byte[totalBytes];
        while (totalBytesRead < totalBytes) {
            buffer.clear();
            int bytesToRead = Math.min(MAX_BUF_SIZE, totalBytes - totalBytesRead);
            if ((bytesRead = ipc.msgrcv(msgqid, buffer, bytesToRead, QUEUE_TYPE, 0)) == -1) { 
                throw new IOException("MessageQueueInput: read failed: errnum = " + ipc.getErrnum() + " " + ipc.strerror(ipc.getErrnum())); 
            }
            buffer.get(totalMessage, totalBytesRead, bytesRead);
            totalBytesRead += bytesRead;
        }
        return totalMessage;
    }
    
    public void closeMessageQueue() throws IOException {
        ipc.msgRmid(msgqid);
    }
    
}