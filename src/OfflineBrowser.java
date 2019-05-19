import javax.swing.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class OfflineBrowser {
    private String url;
    private String urlHomepage;

    public OfflineBrowser(String param) {
        url = param;
        urlHomepage = getHomepage(url);
    }

    /**
     * Get URL without prefix
     *
     * @param param input URL, possibly containing http:// || https:// || www.
     * @return remove URL prefix
     */
    private String getHomepage(String param) {
        String result = param;
        if (result.contains("http://")) {
            result = result.substring(new String("http://").length());
        }
        if (result.contains("https://")) {
            result = result.substring(new String("https://").length());
        }
        if (result.contains("www.")) {
            result = result.substring(new String("www.").length());
        }
        return result;
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
                ArrayList<String> urlList = new ArrayList<String>();
                getAllLinks(url, urlList);
                exportURLListToFile(urlList);
                JOptionPane.showMessageDialog(null, "This works out! Site saved to " + fileName);
                //saveImage("https://live.staticflickr.com/7907/46834888674_025daa71ef_o.jpg");
                //saveImage("https://www.carifred.com/quick_any2ico/Quick_Any2Ico.exe");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        String fileName = url.getFile();
        String destName = "output\\" + fileName.substring(fileName.lastIndexOf("/"));
        System.out.println(destName);

        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destName);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
        JOptionPane.showMessageDialog(null, destName);
    }

    private void getAllLinks(String urlParam, ArrayList<String> urlList) throws IOException {
        URL pageLocation = new URL(urlParam);
        Scanner in = new Scanner(pageLocation.openStream());
        while (in.hasNext()) {
            String line = in.next();
            if (line.contains("href=\"")) {
                int from = line.indexOf("\"");
                int to = line.lastIndexOf("\"");
                urlList.add(line.substring(from + 1, to));
            }
        }
    }

    private void exportURLListToFile(ArrayList<String> urlList) throws IOException {
        FileOutputStream fs1 = new FileOutputStream("output\\outputhttp.txt");
        FileOutputStream fs2 = new FileOutputStream("output\\output.txt");
        for (String s : urlList) {
            //Assure child URL is not duplicated
            if (!getHomepage(s).equals(urlHomepage)) {
                //URL is complete
                if (s.contains("http://") || s.contains("https://")) {
                    if (getHomepage(s).indexOf(urlHomepage) == 0) {
                        fs1.write(s.getBytes());
                        fs1.write('\n');
                    }
                } else if (!s.equals("")) {
                    fs2.write(s.getBytes());
                    fs2.write('\n');
                }
            }
        }
        fs1.close();
        fs2.close();
    }

}
