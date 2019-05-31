public class DownloadThread extends Thread {
    private String urlFile;
    private String outputFileWithoutFolder;
    private Lock lock;

    public DownloadThread(String urlFile, String outputFileWithoutFolder, Lock lock) {
        this.urlFile = urlFile;
        this.outputFileWithoutFolder = outputFileWithoutFolder;
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            lock.addRunningThread();
            new FileDownloader(urlFile, outputFileWithoutFolder).Start();
            lock.removeRunningThread();
            synchronized (lock) {
                lock.notify();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}