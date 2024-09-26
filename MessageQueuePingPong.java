import java.io.IOException;

public class MessageQueuePingPong {
    
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
        
        getRoundTripTime(rounds);
        closeMessageQueue();
    }

    

    private void getRoundTripTime(int rounds) throws IOException {
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                Long start = System.nanoTime();
                messageQueue.writeLong(8L);
                messageQueue.readLong();
                Long end = System.nanoTime();
                Long timing = end - start;
                if (i != 0) {
                    Total += timing;
                }
            } else {
                messageQueue.writeLong(messageQueue.readLong());
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
