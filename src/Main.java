import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        var start = Instant.now();

        var argmap = readArguments(args);
        var path = argmap.get("path");
        var extensions = argmap.get("ext");
        var threads = Integer.parseInt(argmap.get("threads"));

        var scanner = new DirectoryScanner(path, extensions, threads);
        var result = scanner.getCounter();
        System.out.println("The total SLOC is: " + result + " lines.");

        var finish = Instant.now();
        var elapsed = Duration.between(start, finish);
        System.out.println("Time elapsed: " + elapsed.toMillis() + "ms");
        System.exit(0);
    }

    private static final String[] defaultArguments = new String[] {
            "path:./",
            "ext:all",
            "threads:4",
    };

    public static HashMap<String, String> readArguments(String[] args) {
        var pointer = 0;
        var output = new HashMap<String, String>();
        for (var each: defaultArguments) {
            var temp = each.split(":");
            output.put(temp[0], temp[1]);
        }
        while (pointer < args.length) {
            var current = args[pointer];
            if (current.startsWith("--") && current.length() > 2) {
                var argname = current.substring(2);
                pointer++;
                if (pointer < args.length) {
                    var argvalue = args[pointer];
                    pointer++;
                    output.put(argname, argvalue);
                }
            } else pointer++;
        }
        return output;
    }

}
