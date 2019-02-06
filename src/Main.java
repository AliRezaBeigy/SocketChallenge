import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    private static Socket clientSocket;
    private static ObjectInputStream in;
    private static ObjectOutputStream out;
    private static ServerSocket serverSocket;
    private static long[] answers = new long[90];

    public static void main(String[] args) {
        try {
            start();
            stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void start() throws IOException {
        serverSocket = new ServerSocket(15232);
        while (true) {
            clientSocket = serverSocket.accept();
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            new Thread(() -> {
                DataModal inputData;
                while (true) {
                    try {
                        if ((inputData = (DataModal) in.readObject()) == null) break;

                        answers[inputData.getLevel() - 1] = inputData.getAnswer();

                        if (inputData.getLevel() == answers.length && checkChallenge() == answers.length - 1) {
                            out.writeObject(new ResponseModal.Builder().isFinished(true).isSuccessful(true).message("Challenge Done.").build());
                            System.out.println("Challenge Done.");
                            return;
                        }

                        ResponseModal.Builder builder = new ResponseModal.Builder();
                        builder.isSuccessful(inputData.getAnswer() > 0);
                        int completed;
                        builder.message((completed = checkChallenge()) + " level has correct answer so " + (answers.length - completed) + " left");
                        out.writeObject(builder.build());
                    } catch (IOException | ClassNotFoundException ignored) {
                        break;
                    }
                }
            }).start();
        }
    }

    private static int checkChallenge() {
        if (answers[0] != 0)
            return 0;
        if (answers[1] != 1)
            return 1;
        for (int i = 2; i < answers.length; i++)
            if (answers[i] != answers[i - 1] + answers[i - 2])
                return i;
        return answers.length - 1;
    }

    private static void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
}
