import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectoryScanner {

    private final File root;
    private final HashSet<String> extensions;
    private final ThreadPoolExecutor executor;
    private final WaitGroup waitGroup;
    private final AtomicInteger counter;

    public DirectoryScanner(String root, String ext, int threads) {
        this.root = new File(root);
        if (ext.equals("all")) {
            extensions = null;
        } else {
            extensions = new HashSet<>();
            extensions.addAll(Arrays.asList(ext.split(",")));
        }
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threads);
        waitGroup = new WaitGroup();
        counter = new AtomicInteger(0);
    }

    public int getCounter() {
        waitGroup.add(1);
        addTasksFrom(root);
        try {
            waitGroup.await();
        } catch (InterruptedException ignored) {}
        return counter.get();
    }

    private void addTasksFrom(File file) {
        if (file.isDirectory()) { // if its a directory
            var files = file.listFiles();
            if (files == null) {
                System.out.format("Failed to process directory \"%s\"\n", file.getName());
            } else {
                Arrays.stream(files).forEach(f -> {
                    if (extensions == null || extensions.contains(getExtension(f))) {
                        waitGroup.add(1);
                        executor.execute(() -> addTasksFrom(f));
                    }
                });
            }
        } else { // if its a file
            var lines = countLines(file);
            System.out.format("File \"%s\": %d lines\n", file.getName(), lines);
            counter.addAndGet(lines);
        }
        waitGroup.done();
    }

    public static int countLines(File file) {
        try {
            var reader = new LineNumberReader(new FileReader(file));
            reader.skip(Long.MAX_VALUE);
            return reader.getLineNumber();
        } catch (Exception e) {
            var filename = file.getName();
            System.out.format("Failed to process \"%s\"\n", filename);
            return 0;
        }
    }

    private static String getExtension(File file) {
        var filename = file.getName();
        var extension = "";
        var i = filename.lastIndexOf('.');
        if (i > 0) {
            extension = filename.substring(i + 1);
        }
        return extension;
    }

}
