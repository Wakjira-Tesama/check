import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Color;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

// import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame {
    private JTextField userMessage;
    private JTextArea chatBox;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP, name = "USER";
    private Socket servSocket;

    public Client(String host) {
        super("Client!");
        serverIP = host;
        userMessage = new JTextField();
        userMessage.setEditable(false);
        userMessage.setText("Type Here...");
        userMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendMessage(event.getActionCommand()); // returns the value of the text field
                userMessage.setText("");
            }
        });
        add(userMessage, BorderLayout.SOUTH);
        chatBox = new JTextArea();
        chatBox.setEditable(false);
        add(new JScrollPane(chatBox), BorderLayout.CENTER);
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
            Color color = new Color(224, 228, 255);
            // Set JTextArea background color to color that you choose
            chatBox.setBackground(color);

            System.out.println("Successfully updated the frontend");
        } catch (Exception e) {
            System.out.println("Exceptions in the frontend occured");
        }
        try {
            this.setLocation(510, 0);
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException eofException) {
            showMessage("\nClient terminated the connection");

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeAll();
        }
    }

    private void connectToServer() throws IOException {
        showMessage("Attempting connection");
        servSocket = new Socket(InetAddress.getByName(serverIP), 6789);
        showMessage("\nConnected to" + servSocket.getInetAddress().getHostName());

    }

    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(servSocket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(servSocket.getInputStream());
        showMessage("\nStreams are setup!");

    }

    private void whileChatting() throws IOException {
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage(message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\nObjectType unkown!");
            }
        } while (!message.equals("ADMIN >> END"));
    }

    private void closeAll() {
        showMessage("\nClosing Connections!");
        ableToType(false);
        try {
            output.close();
            input.close();
            servSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            output.writeObject(name + " >> " + message);
            output.flush();
            // showMessage("\n"+ name + ">> " + message);
        } catch (IOException ioException) {
            chatBox.append("\nIO Error: Something is messed up!");
        }

    }

    private void showMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                System.out.println(message);
                String check = "";
                if (message.length() > 16)
                    check = message.substring(0, 15);
                if (check.equals("@#$%^YourNameIs")) {
                    name = "USER" + message.substring(15, 17);
                    System.out.println("name is changed to " + name);
                } else
                    chatBox.append("\n" + message);
            }
        });
    }

    private void ableToType(final boolean b) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                userMessage.setEditable(b);
            }
        });
    }
}