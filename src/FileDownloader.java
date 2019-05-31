import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader {

    private String urlFile;
    private String OutputFileWithoutFolder;
    public FileDownloader(String urlFile, String outputFile) {
        this.urlFile = urlFile;
        this.OutputFileWithoutFolder = outputFile;
    }
    public void Start() throws Exception {
        try {
            URL url = new URL(urlFile);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(OffBrowser.DEFAULT_FOLDER_OUTPUT + "\\" + OutputFileWithoutFolder);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            System.out.println("Saved to " + OffBrowser.DEFAULT_FOLDER_OUTPUT + "\\" + OutputFileWithoutFolder);
        } catch (IOException e) {
            System.out.println("Error saving " + urlFile);
        }
    }
}
