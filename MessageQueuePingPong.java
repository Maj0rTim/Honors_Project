import java.io.IOException;

public class MessageQueuePingPong {
    
    private static final String PINGTOPONG = "/home/Timothy/Ping";
    private static final String PONGTOPING = "/home/Timothy/Pong";
    private MessageQueueChannel messageQueue;
    private String myName;
    private int ID;
    private Long Total;
    
    public MessageQueuePingPong(String name) throws IOException {
        this.myName = name;
        this.Total = 0L;
        if (myName.equals("Ping")) {
            messageQueue = new MessageQueueChannel(PINGTOPONG);
            ID = messageQueue.getMessageQueueID();
        } else if (myName.equals("Pong")) {
            messageQueue = new MessageQueueChannel(PONGTOPING);
            ID = messageQueue.getMessageQueueID();
        }
    }

    public void playSimulation(int rounds) throws IOException {
        synchronize();
        getRoundTripTime(rounds);
    }

    private void synchronize() throws IOException {
        if (myName.equals("Ping")) {
            messageQueue.writeLong(0L);
            messageQueue.readLong();
        } else {
            messageQueue.readLong();
            messageQueue.writeLong(0L);
        }
    }

    private void getRoundTripTime(int rounds) throws IOException {
        for (int i=0; i<rounds; i++) {
            if (myName.equals("Ping")) {
                pingThrowBall(System.nanoTime());
                Long start = pingCatchBall();
                Long end = System.nanoTime();
                Total += end - start;
            } else {
                pongThrowBall(pongCatchBall());
            }
        }
        System.out.println(Total/rounds);
    }

    private void pingThrowBall(Long time) throws IOException {
        messageQueue.writeLong(time);
    }

    private Long pingCatchBall() throws IOException {
        return messageQueue.readLong();
    }

    private void pongThrowBall(Long time) throws IOException {
        messageQueue.writeLong(time);
    }

    private Long pongCatchBall() throws IOException {
        return messageQueue.readLong();
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length != 2) {
            System.out.println("Usage: java NamedPipePingPong <Ping|Pong> <rounds>");
            return;
        }

        String myName = args[0];
        int rounds = Integer.parseInt(args[1]);
        
        MessageQueuePingPong simulation = new MessageQueuePingPong(myName);
        simulation.playSimulation(rounds);
    }
}
