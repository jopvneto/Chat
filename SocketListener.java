//SocketListener.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SocketListener implements Runnable {
	 private Socket socket;
	    private PrintWriter out;
	    private BufferedReader in;

	    public SocketListener(Socket socket) throws IOException {
	        super();
	        this.socket = socket;
	        this.out = new PrintWriter(socket.getOutputStream(), true);
	        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    }

	    @Override
	    public void run() {
	        try {
	            String inputLine;

	            while ((inputLine = in.readLine()) != null) {
	                String[] command = inputLine.split(":");

	                if ("login".equals(command[0])) {
	                    String currentUser = command[1];
	                    ChatServer.USERS.put(currentUser, this);
	                    // Avisa ao usuario que acabou de logar os demais usuarios online
	                    out.println("users:" + String.join(";",
	                            ChatServer.USERS.keySet()
	                                    .stream()
	                                    .filter(x -> !currentUser.equals(x))
	                                    .collect(Collectors.toList())));

	                    // Avisa aos demais usuarios que o novo usuario acabou de entrar
	                    ChatServer.USERS.keySet()
	                            .stream()
	                            .filter(x -> !currentUser.equals(x))
	                            .forEach(x -> ChatServer.USERS.get(x).getOut().println("users:" + currentUser));
	                } else {
	                    SocketListener otherUser = ChatServer.USERS.get(command[0]);
	                    String message = String.join(":", Arrays.stream(command).skip(1).collect(Collectors.toList()));

	                    if (otherUser != null) {
	                        otherUser.getOut().println(message);
	                    } else {
	                        System.out.println("User " + command[0] + " not found");
	                    }
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    public Socket getSocket() {
	        return socket;
	    }

	    public void setSocket(Socket socket) {
	        this.socket = socket;
	    }

	    public PrintWriter getOut() {
	        return out;
	    }

	    public void setOut(PrintWriter out) {
	        this.out = out;
	    }

	    public BufferedReader getIn() {
	        return in;
	    }

	    public void setIn(BufferedReader in) {
	        this.in = in;
	    }
}
