import java.io.IOException;
import java.util.Arrays;

public class MessageQueuePingPong {
    
    private static final int PING_TYPE = 99;
    private static final int PONG_TYPE = 66;
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
        int[] results = new int[40];
        synchronize();
        for (int i=0; i<results.length; i++) {
            int size = 1024*(i+1);
            int result = getRoundTripTime(rounds, size);
            results[i] = result;
            Total = 0L;
        }
        closeMessageQueue();
        System.out.println(Arrays.toString(results));
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

    private int getRoundTripTime(int rounds, int size) throws IOException {
        byte[] data = new byte[size];
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                messageQueue.write(data, PING_TYPE);
                messageQueue.read(data.length, PONG_TYPE);
                Long end = System.nanoTime();
                if (i != 0) {
                    Total += end - start;
                }
            } else {
                messageQueue.write(messageQueue.read(data.length, PING_TYPE), PONG_TYPE);
            }
        }
        return (int) (Total/(rounds-1));
        
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
