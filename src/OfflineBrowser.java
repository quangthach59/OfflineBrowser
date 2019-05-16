import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class OfflineBrowser {
    private String url;

    public OfflineBrowser(String param) {
        url = param;
    }

    private boolean isConnectionAvailable() {
        try {
            //Create new URL
            URL urlObj = new URL(url);
            //Create new connection
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            //Set request method
            con.setRequestMethod("GET");
            //Set connection timeout
            con.setConnectTimeout(5000);
            //Connect
            con.connect();
            //Compare response code, connection accepted if code is 200
            //Reference here: https://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html
            if (con.getResponseCode() == 200) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void Start() {
        if (!isConnectionAvailable()) {
            JOptionPane.showMessageDialog(null, "Connection failed, URL may not exist or network is not available!");
        } else {
            try {
                URL website = new URL(url);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                String fileName = "output\\homepage.html";
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.close();
                getAllLinks(url);
                JOptionPane.showMessageDialog(null, "This works out! Site saved to " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getAllLinks(String urlParam) throws IOException {
        ArrayList<String> al = new ArrayList<String>();
        URL pageLocation = new URL(urlParam);
        Scanner in = new Scanner(pageLocation.openStream());
        while (in.hasNext()) {
            String line = in.next();
            // "href=\"https://"
            if (line.contains("href=\"")) {
                int from = line.indexOf("\"");
                int to = line.lastIndexOf("\"");
                al.add(line.substring(from + 1, to));
            }
        }
        FileOutputStream fs = new FileOutputStream("output\\output.txt");
        for (String s : al) {
            fs.write(s.getBytes());
            fs.write('\n');
        }
        fs.close();
    }

}
