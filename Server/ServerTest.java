import javax.swing.JFrame;

public class ServerTest {

    public static void main(String[] args) {
        Server admin = new Server();
        // pressing the close button on the JFrame exits the application
        admin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        admin.startRunning();
    }

}