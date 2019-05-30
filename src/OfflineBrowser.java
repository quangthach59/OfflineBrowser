import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OfflineBrowser {
    private String url;
    private String urlHomepage;
    private String urlWithoutProtocol;

    public OfflineBrowser(String param) {
        url = param;
        if (!url.endsWith("/"))
            url += "/";
        urlHomepage = getURLWithoutProtocol(url);
        urlWithoutProtocol = urlHomepage;
    }

    private boolean isConnectionAvailable() {
        try {
            URL urlObj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
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

    private String getURLWithoutProtocol(String param) {
        String result = param;
        for (String s : new String[]{"http://", "https://", "www."}) {
            if (result.contains(s)) {
                result = result.substring(s.length());
            }
        }
        return result;
    }

    private String getURLWithProtocol(String urlFile) {
        //If file has no protocol, remove the first slash of the urlFile and the homepage (already contains slash)
        if (urlFile.startsWith("/"))
            return url + urlFile.substring(1);
        //If urlFile already contains protocol
        return urlFile;
    }

    private boolean URLContainsFileExt(String urlFile) {
        int index = getURLWithoutProtocol(urlFile).lastIndexOf("/");
        if (index == -1)
            return false;
        String fileName = getURLWithoutProtocol(urlFile).substring(index);
        if (fileName.length() == 1)
            return false;
        return fileName.contains("/") && fileName.contains(".");
    }

    private void saveFile(String urlFile) throws IOException {
        try {
            URL url = new URL(getURLWithProtocol(urlFile));
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            String destName = getFileNameWithPath(urlFile);
            FileOutputStream fos = new FileOutputStream(destName);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            System.out.println("Saved to " + destName);
        } catch (IOException e) {
            System.out.println("Error saving file " + urlFile);
        }

    }

    private void newGetAllLinks(String urlParam, ArrayList<String> urlList) throws IOException {
        URL pageLocation = new URL(urlParam);
        Scanner in = new Scanner(pageLocation.openStream());
        while (in.hasNext()) {
            String line = in.next();
            if (line.contains("href=\"")) {
                try {
                    int from = line.indexOf("\"");
                    int to = line.lastIndexOf("\"");
                    urlList.add(line.substring(from + 1, to));
                } catch (Exception e) {
                    System.out.print("Error occurred with URL(s). Offline site may not work properly.");
                }
            }
        }
    }

    private void DownloadHomePage(String urlParam) throws IOException {
        URL page = new URL(urlParam);
        Scanner in = new Scanner(page.openStream());
        String fileName = "default.html";
        FileOutputStream fos = new FileOutputStream("output\\" + fileName);
        while (in.hasNext()) {
            String line = in.nextLine();
            line = line.replaceAll("https://www." + urlWithoutProtocol + "\"", fileName + "\"");
            line = line.replaceAll("https://" + urlWithoutProtocol + "\"", fileName + "\"");
            line = line.replaceAll("http://" + urlWithoutProtocol + "\"", fileName + "\"");
            line = line.replaceAll("https://www." + urlWithoutProtocol, "");
            line = line.replaceAll("https://" + urlWithoutProtocol, "");
            line = line.replaceAll("http://" + urlWithoutProtocol, "");
            fos.write(line.getBytes());
        }
        fos.close();
    }

    private String getFileName(String urlParam) throws IOException
    {
        String result = urlParam;
        result = getURLWithoutProtocol(result);
        result = result.substring((getParentpage(urlParam) + "/").length());

        return result;
    }

    private String getParentpage(String urlFile) {
        String file = getURLWithoutProtocol(urlFile);
        int index = file.indexOf("/");
        if (index == -1)
            return file;
        return file.substring(0, file.indexOf("/"));
    }

    private String getFileNameWithPath(String urlFile) {
        String output = "output\\";
        String folderPath = "output/";
        String fileName = getFileNameWithoutSite(urlFile);
        System.out.println(fileName);
        while (fileName.contains("/")) {
            output += fileName.substring(0, fileName.indexOf("/")) + "\\";
            folderPath += fileName.substring(0, fileName.indexOf("/")) + "/";
            fileName = fileName.substring(fileName.indexOf("/") + 1);
        }
        output += fileName;
        if (!fileName.contains("."))
            output += ".html";
        File folder = new File(folderPath);
        boolean bool = folder.mkdirs();
        if (!bool)
            System.out.println("Error creating directory " + folderPath);
        return output;
    }

    private String getFileNameWithoutSite(String urlFile) {
        //Remove site and the first slash
        String output = getURLWithoutProtocol(getURLWithProtocol(urlFile));
        return output.substring(output.indexOf("/") + 1);
    }

    private void exportToOutputFile(ArrayList<String> urlList) throws IOException {
        FileOutputStream fos = new FileOutputStream("output\\output.txt");
        for (String s : urlList) {
            //As long as s is a file || s is child || s and url contains the same homepage
            if (((s.startsWith("/") && s.length() > 1) || URLContainsFileExt(s) || getParentpage(s).equals(urlHomepage)) && !s.contains("#")) {
                fos.write(s.getBytes());
                fos.write('\n');
                System.out.print(getURLWithProtocol(s) + " ");
                saveFile(s);
            }
        }
        fos.close();
    }

    public void StartBrowsing() {
        //No connection, finish
        if (!isConnectionAvailable()) {
            JOptionPane.showMessageDialog(null, "Error parsing input to URL");
        } else {
            try {
                DownloadHomePage(url);
//                saveFile(url);
//                ArrayList<String> urlList = new ArrayList<String>();
//                newGetAllLinks(url, urlList);
//                exportToOutputFile(urlList);
                JOptionPane.showMessageDialog(null, "Done!");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.print("Error with StartBrowsing");
            }
        }
    }
}