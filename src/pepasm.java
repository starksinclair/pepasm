import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class pepasm {
    //Currently, this function only works for ascii values formatted as 0x0034, so if you put 0x34 or 0x034, it will not format correctly.
    // TODO: Add support for hex values formatted as 0x34 or 0x034.
    //TODO: it doesn't also support labels, and branch instructions like BRNE because BRNE references the memory location of the next instruction, which i don't know how to handle yet but i will ask the prof, tomorrow.
     static void conversion(Map<String, Map<String, String>> map, String line) {
        String[] words = line.trim().split("\\s+");
        Map<String, String> main = map.get(words[0]);
        if (main == null) System.out.println("Cmd not found");
        assert main != null;
         String sub;
         String ascii = "";
        if (words.length > 1){
             sub = main.get(words[2]);
             ascii = words[1].replace("0x", "")
                     .replaceAll("(.{2})", "$1 ")
                     .trim();
         } else {
            sub = main.get("default");
        }
        System.out.print(sub + " " + ascii.replace(",", ""));
     }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No file provided");
            System.exit(1);
        }
        Map<String, Map<String, String>> map = new HashMap<>();
        map.put("STBA", Map.of("i", "F0", "d", "F1"));
        map.put("LDBA", Map.of("i", "D0", "d", "D2"));
        map.put("STWA", Map.of("i", "E0", "d", "E1"));
        map.put("LDWA", Map.of("i", "C0", "d", "C1"));
        map.put("ANDA", Map.of("i", "80", "d", "81"));
        map.put("ASLA", Map.of("default", "0A"));
        map.put("ASRA", Map.of("default", "0C"));
        map.put("ADDA", Map.of("i", "60", "d", "61"));
        map.put("CPBA", Map.of("i", "B0", "d", "B1"));
        map.put("BRNE", Map.of("i", "1A"));
        map.put("STOP", Map.of("default", "00"));
        map.put(".END", Map.of("default", "zz"));

        String filePath = args[0];
        File file = new File(filePath);

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
//                The following instructions should be supported STBA, LDBA, STWA, LDWA, ANDA, ASLA, ASRA, STOP, CPBA, BRNE.
             if (line.isEmpty()) continue;
             conversion(map, line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found at " + filePath);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An error occurred while reading the file.");
            e.printStackTrace();
        }
    }
}