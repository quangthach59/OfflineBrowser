import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Scanner;

public class OffBrowser {
    //Format https://www.example.com/ or https://example.com/
    private String url;
    private String homepageWithSlash;
    private ArrayList<String> listURLs;
    private final String DEFAULT_FOLDER_OUTPUT = "output";
    private final String DEFAULT_HOMEPAGE_FILENAME = "default.html";

    public OffBrowser(String param) {
        url = param;
        homepageWithSlash = removeProtocol(url);
        listURLs = new ArrayList<>();
    }

    /**
     * Remove prefix http(s):// and www.
     *
     * @param param input URL
     * @return URL without prefix
     */
    private String removeProtocol(String param) {
        String result = param;
        for (String s : new String[]{"http://", "https://", "www."}) {
            if (result.contains(s)) {
                result = result.substring(s.length());
            }
        }
        return result;
    }

    /**
     * Get a string containing children of a standard URL
     *
     * @param param input URL
     * @return a string containing children of a standard URL
     */
    private String getStringChildren(String param) {
        String result = param;
        result = removeProtocol(result);
        int indexOfSlash = result.indexOf("/");
        //For example, https://www.hcmus.edu.vn lacks the last slash
        if (indexOfSlash == -1)
            return "";
        return result.substring(result.indexOf("/") + 1);
    }

    /**
     * Get file name from a given URL
     *
     * @param param input URL
     * @return file name, also creates directories to save this file
     */
    private String getFullFileName(String param) {
        //Filename : \\
        //Directory: /
        String result = getStringChildren(param);
        StringBuilder fullFileName = new StringBuilder();
        StringBuilder folderName = new StringBuilder(DEFAULT_FOLDER_OUTPUT + "/");
        boolean isAFile = result.contains(".");
        //Nothing after homepage && not a file && the same as homepage with slash
        if (!isAFile && result.equals("") && removeProtocol(param).equals(homepageWithSlash)) {
            boolean bool = new File(folderName.toString()).mkdirs();
            return DEFAULT_HOMEPAGE_FILENAME;
        }
        while (result.contains("/")) {
            String part = result.substring(0, result.indexOf("/"));
            fullFileName.append(part).append("\\");
            folderName.append(part).append("/");
            result = result.substring(part.length() + 1);
        }
        if (result.equals("")) {
            fullFileName = new StringBuilder(fullFileName.substring(0, fullFileName.length() - 1));
            fullFileName.append(".html");
        } else if (result.contains(".")) {
            fullFileName.append(result);
        } else {
            fullFileName.append(result).append(".html");
        }
        File folder = new File(folderName.toString());
        boolean bool = folder.mkdirs();
        return fullFileName.toString();
    }

    /**
     * Determine if an URL is available for connection
     *
     * @param param input URL
     * @return true if available, false if not
     */
    private boolean isConnectionAvailable(String param) {
        try {
            URL urlObj = new URL(param);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.connect();
            //Compare response code, connection accepted if code is 200
            //Reference here: https://docs.oracle.com/javase/7/docs/api/java/net/HttpURLConnection.html
            return (con.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

    private void DownloadPage(String param) throws Exception {
        Scanner in = new Scanner(new URL(param).openStream());
        String fileOutput = getFullFileName(param);
        //Content will be saved to this file
        FileOutputStream fos = new FileOutputStream(DEFAULT_FOLDER_OUTPUT + "\\" + fileOutput);
        ArrayList<String> list = new ArrayList<>();
        //Scan each line for href
        while (in.hasNext()) {
            String line = in.next();
            //Start of an URL
            if (line.contains("href=\"")) {
                //Split from href=" to the closest "
                line = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                //If this URL is necessary and not duplicated, add to list
                if (LinkNeedsAdding(line) && !list.contains(line)) {
                    list.add(line);
                    saveFile(line);
                }
            }
        }
        //Close scanner and renew
        //Read the whole content for find and replace, then save
        in.close();
        in = new Scanner(new URL(param).openStream());
        while (in.hasNext()) {
            //The whole content of the page as plain text
            String line = in.nextLine();
            line = ReplaceURLWithURL(line, list);
            fos.write(line.getBytes());
        }
        fos.close();
    }

    private void saveFile(String param) throws IOException {
        try {
            if (param.startsWith("/"))
                param = url + param.substring(1);
            URL url = new URL(param);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            String destName = getFullFileName(param);
            FileOutputStream fos = new FileOutputStream(DEFAULT_FOLDER_OUTPUT + "\\" + destName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            System.out.println("Saved to " + DEFAULT_FOLDER_OUTPUT + "\\" + destName);
        } catch (IOException e) {
            System.out.println("Error saving file " + param);
        }

    }

    private String ReplaceURLWithURL(String line, ArrayList<String> list) {
        line = line.replaceAll("href=\"" + url, "href=\"" + getFullFileName(url));
        for (String s : list) {
            //System.out.println("Replacing " + s);
            line = line.replaceAll("href=\"" + s, "href=\"" + getFullFileName(s));
        }
        return line;
    }

    /**
     * Determine if a link should be added for browsing
     *
     * @param param input URL
     * @return true if needed, false if not
     */
    private boolean LinkNeedsAdding(String param) {
        //URL is empty or URL starts with # (fragment), don't add
        if (param.equals("") || param.equals("/") || param.contains("#") || param.contains("?"))
            return false;
        //URL is sub link, add
        if (param.startsWith("/"))
            return true;
        //Try to download and replace .css file may leads to crashing UI
        if (param.endsWith(".css"))
            return false;
        //Sub-string after the last slash contains a dot, a present of file extension, add
        //if (param.substring(param.lastIndexOf("/")).contains("."))
        if (getStringChildren(param).contains("."))
            return true;
        try {
            URL urlHome = new URL(url);
            URL urlParam = new URL(param);
            //URL and homepage share the same host, but not equal, add
            if (urlHome.getHost().equals(urlParam.getHost()) && !removeProtocol(url).equals(removeProtocol(param)))
                return true;
        } catch (Exception e) {
            return false;
        }
        //Return for fun, or any unknown kind of URL
        return false;
    }

    /**
     * Start browsing
     */
    void StartBrowsing() {
        if (!isConnectionAvailable(url)) {
            JOptionPane.showMessageDialog(null, "Error parsing input to URL");
        } else {
            try {
                long startTime = System.currentTimeMillis();
                DownloadPage(url);
                long endTime = System.currentTimeMillis();
                JOptionPane.showMessageDialog(null, "Done! Time lapsed: " + (endTime - startTime) + "ms");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("Error with StartBrowsing");
            }
        }
    }
}
