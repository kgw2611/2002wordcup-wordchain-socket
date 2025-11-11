package serversettings;

import java.io.*;
import java.util.*;
import java.nio.charset.*;

public class Server {
    public static void main(String[] args) throws IOException {
        Set<String> dictionary = new HashSet<String>(370_000);

        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("MiniDictionary.txt"), StandardCharsets.UTF_8)))
        {
            String line;
            while((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                dictionary.add(line);
            }
        }
    }
}
