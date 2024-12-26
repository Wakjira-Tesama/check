import java.util.Vector;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.PriorityQueue;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
// ServerSocket provides a mechanism for the server program to listen for clients and establish connections with them
import java.net.Socket; // represents a Socket
// import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.lang.Thread;

public class Server extends JFrame {

    // defining private variables to be used further
    private JTextField userMessage;
    public JTextArea chatBox;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private ServerSocket server;
    private Socket clientSocket;
    // private Thread thread;
    public Vector<Queue<String>> Deliverables = new Vector<Queue<String>>();
    private Integer i = 0;

    public Server() {

        super("Messenger Server");
        userMessage = new JTextField();
        userMessage.setEditable(false);
        userMessage.setText("Connect upto 100 clients and chat :)");
        userMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendMessage(event.getActionCommand());
                // sendMessage(userMessage.getText());
                userMessage.setText(""); // after every text input box is cleared
            }
        });
        add(userMessage, BorderLayout.SOUTH); // userMessage gets added at SOUTH / BASE
        chatBox = new JTextArea();
        chatBox.setEditable(false);
        add(new JScrollPane(chatBox));
        setSize(500, 300);
        setVisible(true);
    }

    public void startRunning() {

        // change Frontend of the JTextArea and JTextFrame
        try {
            chatBox.setLineWrap(true);

            // Sets JTextArea font and color.
            Font font = new Font("Franklin Gothic Medium", Font.PLAIN, 14);
            chatBox.setFont(font);
            userMessage.setFont(font);

            // bg color
            Color color = new Color(192, 237, 199);
            // Set JTextArea background color to color that you choose
            chatBox.setBackground(color);

            System.out.println("Successfully updated the frontend");
        } catch (Exception e) {
            System.out.println("Exceptions in the frontend occured");
        }

        try {
            server = new ServerSocket(6789, 100); // backlog*****
            // can be used to set a timeout for the .accept() method
            // server.setSoTimeout(3600);
            this.setLocation(0, 0);
            while (true) {
                try {
                    waitForConnection();
                    // setupStreams();
                    // whileChatting();
                } catch (EOFException eofException) {
                    showMessage("\nServer ended the connection!");
                } finally {
                    closeAll();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void waitForConnection() throws IOException {
        showMessage("Waiting for someone to connect!");
        while (true) {

            clientSocket = server.accept();
            System.out.println("A new client is connected : " + clientSocket);

            // obtaining input and out streams
            ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());

            showMessage("Assigning new thread for this client");

            // create a new thread object
            Thread thread = new ClientHandler(this, server, clientSocket, ois, oos, i);
            Queue<String> string_q = new PriorityQueue<>();
            Deliverables.add(string_q);
            i++;
            // Invoking the start() method
            thread.start();
            // clientSocket = server.accept();
            // showMessage("\nConnected to " + clientSocket.getInetAddress().getHostName() +
            // "!");
        }
    }

    public void setupStreams() throws IOException {
        // Creates an ObjectOutputStream that writes to the specified OutputStream which
        // in this case is clientSocket.getOutputStream()
        output = new ObjectOutputStream(clientSocket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(clientSocket.getInputStream());
        showMessage("\nStreams are setup!\n");
    }

    public void whileChatting() throws IOException {
        String message = "\nConnected to " + clientSocket.getInetAddress().getHostName();
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String) input.readObject(); // reads from inputstream
                System.out.println("message " + message);
                showMessage(message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\nError recieving message from user.");
            }
        } while (message.substring(message.length() - 3, message.length()) != "END");
    }

    public void sendMessage(String message) {
        try {
            if (message == null)
                message = "";
            output.writeObject("ADMIN >> " + message); // written into the output stream
            output.flush(); // flushes to inputstream of client
            showMessage("\nADMIN >> " + message);
        } catch (Exception e) {
            chatBox.append("\nConnect multiple Clients to communicate. Server is not meant to send messages.");
        }
    }

    public void closeAll() {
        showMessage("\nClosing Connections!\n");
        ableToType(false);
        try {
            output.close();
            input.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public void showMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                chatBox.append("\n" + message);
                for (int i = 0; i < Deliverables.size(); i++) {
                    Queue<String> temp = Deliverables.get(i);
                    temp.add(message);
                    Deliverables.set(i, temp);
                    System.out.println(temp);
                }
            }
        });
    }

    public void ableToType(final boolean b) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                userMessage.setEditable(b);
            }
        });
    }
}

class ClientHandler extends Thread {
    ObjectInputStream ois;
    ObjectOutputStream oos;
    Socket clientSocket;
    ServerSocket servSocket;
    Server s;
    Integer i;

    public ClientHandler(Server server, ServerSocket ss, Socket cs, ObjectInputStream ois, ObjectOutputStream oos,
            Integer n) {
        this.servSocket = ss;
        this.clientSocket = cs;
        this.ois = ois;
        this.oos = oos;
        this.s = server;
        this.i = n;
    }

    @Override
    public void run() {
        Thread t = new ClientMessenger(s, servSocket, clientSocket, ois, oos, i);
        t.start();
        try {
            // whileChatting();
            if (i < 10)
                oos.writeObject("@#$%^YourNameIs0" + i);
            else
                oos.writeObject("@#$%^YourNameIs" + i);
            String message = "\nConnected to " + clientSocket.getInetAddress().getHostName();
            s.showMessage(message);
            do {
                try {
                    message = (String) ois.readObject(); // reads from inputstream
                    s.showMessage(message);
                    // sendMessage();
                } catch (ClassNotFoundException classNotFoundException) {
                    s.showMessage("\nError recieving message from user.");
                } catch (IOException ioException) {
                    s.showMessage("\nError recieving message from user.");
                }
            } while (message.substring(message.length() - 3, message.length()) != "END");
        } catch (Exception IOException) {
            System.out.println("Error in run of thread " + i);
        }
    }

    public void sendMessage() {
        try {
            // s.setVisible(true);
            Queue<String> temp = s.Deliverables.get(i);
            System.out.println(temp);
            System.out.println("inside thread " + i);

            while (temp.peek() != null) {
                oos.writeObject(temp.remove());
            }
            temp.clear();
            s.Deliverables.set(i, temp);
            // oos.writeObject(message);
            // oos.writeObject("ADMIN >> " + message); // written into the output stream
            oos.flush(); // flushes to inputstream of client
            // s.showMessage("\nADMIN >> " + message);

        } catch (IOException ioException) {
            s.chatBox.append("\nError: Can't send that message");
        }
    }
}

class ClientMessenger extends Thread {
    ObjectInputStream ois;
    ObjectOutputStream oos;
    Socket clientSocket;
    ServerSocket servSocket;
    Server s;
    Integer i;

    public ClientMessenger(Server server, ServerSocket ss, Socket cs, ObjectInputStream ois, ObjectOutputStream oos,
            Integer n) {
        this.servSocket = ss;
        this.clientSocket = cs;
        this.ois = ois;
        this.oos = oos;
        this.s = server;
        this.i = n;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Queue<String> temp = s.Deliverables.get(i);
                while (temp.peek() != null) {
                    oos.writeObject(temp.remove());
                }
                temp.clear();
                s.Deliverables.set(i, temp);
                oos.flush(); // flushes to inputstream of client
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (Exception e) {
            System.out.println("\nerror in client messenger " + i);
        }
    }
}