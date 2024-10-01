import java.io.IOException;

public class MessageQueuePingPong {
    
    private static final int MAX_BUF_SIZE = 4096;
    private static final int SIZE = 1024*40;
    private static final int PING_TYPE = 9999;
    private static final int PONG_TYPE = 6666;
    private LinuxNIPC ipc = new LinuxNIPC();
    private MessageQueueChannel messageQueue;
    private String myName;
    private int ID;
    private Long Total;
    
    public MessageQueuePingPong(String name, String path) throws IOException {
        this.myName = name;
        this.Total = 0L;
        int key = 99909;
        
        if (myName.equals("Ping")) {
            messageQueue = new MessageQueueChannel(key);
            ID = messageQueue.getMessageQueueID();
        } else if (myName.equals("Pong")) {
            messageQueue = new MessageQueueChannel(key);
            ID = messageQueue.getMessageQueueID();
        }
    }

    public void playSimulation(int rounds) throws IOException {
        synchronize();
        getRoundTripTime(rounds);
        closeMessageQueue();
    }

    private void synchronize() throws IOException {
        byte[] data = new byte[8];
        if (myName.equals("Ping")) {
            messageQueue.write(data, PING_TYPE);
            messageQueue.read(data.length, PONG_TYPE);
        } else {
            messageQueue.write(messageQueue.read(data.length, PING_TYPE), PONG_TYPE);
        }
    }

    private void getRoundTripTime(int rounds) throws IOException {
        byte[] data = new byte[SIZE];
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                messageQueue.write(data, PING_TYPE);
                messageQueue.read(data.length, PONG_TYPE);
                Long end = System.nanoTime();
                Total += end - start;
            } else {
                messageQueue.write(messageQueue.read(data.length, PING_TYPE), PONG_TYPE);
            }
        }
        if (myName.equals("Ping")) {
            System.out.println(Total/(rounds-1));
        }
    }

    private void closeMessageQueue() {
        ipc.msgRmid(ID);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 2) {
            System.out.println("Usage: java NamedPipePingPong <Ping|Pong> <rounds>");
            return;
        }
        String myName = args[0];
        int rounds = Integer.parseInt(args[1]);
        String path = "/home/Timothy/PingPong";
        MessageQueuePingPong simulation = new MessageQueuePingPong(myName, path);
        simulation.playSimulation(rounds);
    }
}
