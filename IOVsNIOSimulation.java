
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Random;

public class IOVsNIOSimulation {

    private static final int MAX_BUF_SIZE = 1024 * 40;
    private String mode; // Either "io" or "nio"
    private Long totalTime;

    public IOVsNIOSimulation(String mode) {
        this.mode = mode;
        this.totalTime = 0L;
    }

    public void playSimulation(int rounds) throws IOException {
        int[] results = new int[40];
        for (int i = 0; i < results.length; i++) {
            int size = 1024 * (i + 1);
            int result = getRoundTripTime(rounds, size);
            results[i] = result;
            totalTime = 0L;
        }
        System.out.println("Mode: " + mode + " Results: " + Arrays.toString(results));
    }

    private int getRoundTripTime(int rounds, int size) throws IOException {
        if (mode.equals("io")) {
            return runIOSimulation(rounds, size);
        } else {
            return runNIOSimulation(rounds, size);
        }
    }

    // IO Simulation using InputStream and OutputStream
    private int runIOSimulation(int rounds, int size) throws IOException {
        byte[] data = generateRandomData(size);
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(inputStream);

        for (int i = 0; i < rounds; i++) {
            Long start = System.nanoTime();

            // Ping writes to OutputStream (sends data)
            outputStream.write(data);
            outputStream.flush();

            // Pong reads from InputStream (receives data)
            inputStream.read(data);

            Long end = System.nanoTime();
            if (i != 0) {
                totalTime += end - start;
            }
        }

        outputStream.close();
        inputStream.close();

        return (int) (totalTime / (rounds - 1));
    }

    // NIO Simulation using ByteBuffer and SocketChannels
    private int runNIOSimulation(int rounds, int size) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(generateRandomData(size));
        Pipe pipe = Pipe.open();
        Pipe.SourceChannel sourceChannel = pipe.source();
        Pipe.SinkChannel sinkChannel = pipe.sink();

        for (int i = 0; i < rounds; i++) {
           buffer.clear();
            sinkChannel.write(buffer);
            buffer.clear();
            sourceChannel.read(buffer);
        }

        for (int i = 0; i < rounds; i++) {
            Long start = System.nanoTime();

            // Ping writes to SinkChannel (sends data)
            buffer.clear();
            sinkChannel.write(buffer);

            // Pong reads from SourceChannel (receives data)
            buffer.clear();
            sourceChannel.read(buffer);

            Long end = System.nanoTime();
            if (i != 0) {
                totalTime += end - start;
            }
        }

        sinkChannel.close();
        sourceChannel.close();

        return (int) (totalTime / (rounds - 1));
    }

    private static byte[] generateRandomData(int size) {
        byte[] data = new byte[size];
        new Random().nextBytes(data);
        return data;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java IOVsNIOSimulation <io|nio> <rounds>");
            return;
        }

        String mode = args[0];
        int rounds = Integer.parseInt(args[1]);

        IOVsNIOSimulation simulation = new IOVsNIOSimulation(mode);
        simulation.playSimulation(rounds);
    }
}
