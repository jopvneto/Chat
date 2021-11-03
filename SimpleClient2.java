//SimpleClient2.java
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;
import java.util.function.Function;

public class SimpleClient {
	 private static final String DOWNLOADS_FOLDER = "/Users/nicholas/Temp/Downloads";
	    private String connectedUser;
	    private String[] onlineUsers;
	    private Socket socket;
	    private PrintWriter out;
	    private BufferedReader in;

	    public void startConnection(String ip, int port) throws IOException {
	        socket = new Socket(ip, port);
	        out = new PrintWriter(socket.getOutputStream(), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    }

	    public void login(String user, String password) throws IOException {
	        if ("123".equals(password)) {
	            sendMessage("login:" + user);
	            receiveMessage();
	            this.connectedUser = user;
	        } else {
	            throw new RuntimeException("Wrong password");
	        }
	    }
	    
	    public void sendMessageTo(String userToSend, String message) throws IOException {
	        sendMessage(userToSend + ":" + message);
	    }

	    public void sendFileTo(String userToSend, String filePath) throws IOException {
	        Path path = Path.of(filePath);
	        byte[] bytes = Files.readAllBytes(path);
	        String base64 = Base64.getEncoder().encodeToString(bytes);

	        sendMessage(userToSend + ":file:" + path.getFileName().toString() + ";" + base64);
	    }

	    public void sendMessage(String message) throws IOException {
	        out.println(message);
	    }

	    public void receiveMessage() {
	        new Thread(() -> {
	            try {
	                while (true) {
	                    String message = in.readLine();

	                    if (message.contains(":")) {
	                        String[] commands = message.split(":");

	                        if (commands.length == 2) {
	                            switch (commands[0]) {
	                                case "users":
	                                    updateOnlineUsers(commands[1]);
	                                    break;
	                                case "file":
	                                    saveFile(commands);
	                                    break;
	                            }
	                        }
	                    } else {
	                        // TODO: Momento de recepcao de mensagem. Trocar para tela do chat
	                        System.out.println(message);
	                    }
	                }
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }).start();
	    }

	    private void updateOnlineUsers(String users) {
	        this.onlineUsers = users.split(";");
	        // TODO: Momento que retorna os usuarios logados para exibir na lista do chat
	        for (int i = 0; i < this.onlineUsers.length; i++) {
	            System.out.println("Usuario logado: " + this.onlineUsers[i]);
	        }
	    }

	    private void saveFile(String[] fileCommand) throws IOException {
	        String[] fileMessage = fileCommand[1].split(";");
	        String fileName = fileMessage[0];
	        String fileContent = fileMessage[1];
	        byte[] bytes = Base64.getDecoder().decode(fileContent);
	        OutputStream writer = Files.newOutputStream(Path.of(DOWNLOADS_FOLDER, fileName));
	        writer.write(bytes);
	        writer.close();
	    }

	    public String getConnectedUser() {
	        return connectedUser;
	    }

	    public String[] getOnlineUsers() {
	        if (onlineUsers == null) {
	            onlineUsers = new String[0];
	        }

	        return onlineUsers;
	    }

	    public static void main(String[] args) throws IOException {
	        SimpleClient client = new SimpleClient();
	        client.startConnection("127.0.0.1", 4444);

	        System.out.println("Connected to server...");

	        client.login("joao", "123");

	        Scanner scanner = new Scanner(System.in);

	        while (true) {
	            String message = scanner.nextLine();

	            if (message.equals("arquivo")) {
	                System.out.println("Digite o caminho completo do arquivo");
	                String file = scanner.nextLine();
	                client.sendFileTo("bruno", file);
	            } else {
	                client.sendMessageTo("bruno", message);
	            }
	        }
	    }
}
