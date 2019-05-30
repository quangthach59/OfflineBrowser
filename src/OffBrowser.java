import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import com.sun.org.apache.xpath.internal.functions.FuncStringLength;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class OffBrowser {
    //Format https://www.example.com/ or https://example.com/
    private String url;
    private String homepageWithSlash;

    public OffBrowser(String param) {
        url = param;
        homepageWithSlash = removeProtocol(url);
    }

    private String removeProtocol(String param) {
        String result = param;
        for (String s : new String[]{"http://", "https://", "www."}) {
            if (result.contains(s)) {
                result = result.substring(s.length());
            }
        }
        return result;
    }

    private String getStringChildren(String param) {
        String result = param;
        result = removeProtocol(result);
        return result.substring(result.indexOf("/") + 1);
    }

    private String getFullFileName(String param) {
        //Filename : \\
        //Directory: /
        String result = getStringChildren(param);
        String fullFileName = "";
        String folderName = "output/";
        boolean isAFile = result.contains(".");
        //Nothing after homepage
        if (!isAFile && result.equals("") && removeProtocol(param).equals(homepageWithSlash)) {
            boolean bool = new File(folderName).mkdirs();
            return "default.html";
        }
        while (result.contains("/")) {
            String part = result.substring(0, result.indexOf("/"));
            fullFileName += part + "\\";
            folderName += part + "/";
            result = result.substring(part.length() + 1);
        }
        if (result.equals("")) {
            fullFileName = fullFileName.substring(0, fullFileName.length() - 1);
            fullFileName += ".html";
        } else if (result.contains(".")) {
            fullFileName += result;
        } else {
            fullFileName += result + ".html";
        }
        File folder = new File(folderName);
        boolean bool = folder.mkdirs();

        return fullFileName;
    }

    private boolean isConnectionAvailable(String param) {
        try {
            URL urlObj = new URL(param);
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

    private void DownloadHomePage(String param) throws IOException {
        URL page = new URL(param);
        Scanner in = new Scanner(page.openStream());
        String fileName = getFullFileName(param);
        String currentPage = removeProtocol(param);
        String fileDefault = getFullFileName(url);
        if (!currentPage.endsWith("/")) {
            currentPage += "/";
        }
        FileOutputStream fos = new FileOutputStream("output\\" + fileName);
        while (in.hasNext()) {
            String line = in.nextLine();
//            //Replace current URL with current file path
            line = line.replaceAll("https://www." + currentPage + "\"", fileName + "\"");
            line = line.replaceAll("https://" + currentPage + "\"", fileName + "\"");
            line = line.replaceAll("http://" + currentPage + "\"", fileName + "\"");

            //Remove homepage with default file
            line = line.replaceAll("https://www." + homepageWithSlash + "\"", fileDefault + "\"");
            line = line.replaceAll("https://" + homepageWithSlash + "\"", fileDefault + "\"");
            line = line.replaceAll("http://" + homepageWithSlash + "\"", fileDefault + "\"");

            //
            line = line.replaceAll("https://www." + homepageWithSlash, "");
            line = line.replaceAll("https://" + homepageWithSlash, "");
            line = line.replaceAll("http://" + homepageWithSlash, "");
//            ArrayList<String> list = new ArrayList<String>();
//            GetAllLinks(param,list);
//            ReplaceInLocalFile(line, list);
            fos.write(line.getBytes());
        }
        fos.close();
        System.out.println("output\\" + fileName);
    }

    private void ReplaceInLocalFile(String line, ArrayList<String> list) {
        for (String s : list) {
            String currentPage = removeProtocol(s);
            String fileName = getFullFileName(s);
            //Replace current URL with current file path
            line = line.replaceAll("https://www." + currentPage + "\"", fileName + "\"");
            line = line.replaceAll("https://" + currentPage + "\"", fileName + "\"");
            line = line.replaceAll("http://" + currentPage + "\"", fileName + "\"");

            line = line.replaceAll("https://www." + homepageWithSlash, "");
            line = line.replaceAll("https://" + homepageWithSlash, "");
            line = line.replaceAll("http://" + homepageWithSlash, "");

        }
    }

    private void GetAllLinks(String param, ArrayList<String> list) throws IOException {
        URL pageLocation = new URL(param);
        Scanner in = new Scanner(pageLocation.openStream());
        while (in.hasNext()) {
            String line = in.next();
            if (line.contains("href=\"")) {
                try {
                    int from = line.indexOf("\"");
                    int to = line.lastIndexOf("\"");
                    String result = line.substring(from + 1, to);
                    if (removeProtocol(result).indexOf(homepageWithSlash) == 0 && !list.contains(result)) {

                        list.add(result);
                    }
                } catch (Exception e) {
                    System.out.print("Error occurred with URL(s). Offline site may not work properly.");
                }
            }
        }
    }

    public void StartBrowsing() {
        if (!isConnectionAvailable(url)) {
            JOptionPane.showMessageDialog(null, "Error parsing input to URL");
        } else {
            try {
                DownloadHomePage(url);
                ArrayList<String> list = new ArrayList<String>();
                GetAllLinks(url, list);
                for (String s : list) {
                    DownloadHomePage(s);
                    System.out.println(s);
                }
                JOptionPane.showMessageDialog(null, "Done!");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("Error with StartBrowsing");
            }
        }
    }
}
