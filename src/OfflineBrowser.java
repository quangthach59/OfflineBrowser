import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class OfflineBrowser {
    private String url;

    public OfflineBrowser(String param) {
        url = param;
    }

    public void Start() {
        if (url.equals("")) {
            JOptionPane.showMessageDialog(null, "Empty input!");
        } else if (!isConnectionAvailable()) {
            JOptionPane.showMessageDialog(null, "Connection failed, URL may not exist or network is not available!");
        } else {
            try {
                URL website = new URL(url);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                String fileName = "homepage.html";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                JOptionPane.showMessageDialog(null, "This works out! Site saved to " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isConnectionAvailable() {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            // Set connection timeout
            con.setConnectTimeout(3000);
            con.connect();
            int responseCode = con.getResponseCode();
            if (responseCode == 200) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
