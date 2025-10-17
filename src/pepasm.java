import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class pepasm {
    static String cleanString(String s) {
        return s.replaceAll("[: ,]+$", ""); // drops trailing ':', ',' and spaces
    }
    
    static String toBigEndianBytes(String hex) {
        if (hex.isEmpty()) {
            throw new IllegalArgumentException("Please enter a valid hexadecimal string");
        }
        int v = Integer.parseUnsignedInt(hex.replaceFirst("^0x", ""), 16) & 0xFFFF;
        int hi = (v >>> 8) & 0xFF;
        int lo = v & 0xFF;
        return String.format("%02X %02X", hi, lo);
    }

     static int conversion(Map<String, Map<String, String>> instructionsMap, String line, Map<String, String> branchesMap, Map<String, String> binaryCounter, int counter) {
        String[] words = line.trim().split("\\s+");
        Map<String, String> row = instructionsMap.get(words[0]);
        String label = "";
        String opcodeHex;
        String operandHex = "";
        if (row == null) {
            label = words[0];
            String opcode = words[1];
            row = instructionsMap.get(opcode);
            if (row == null) {
                System.out.println("Cmd not found");
                return counter;
            }
            String addressMode = words[3];
            opcodeHex = row.get(addressMode);
            if (opcodeHex == null) {
                System.out.println("Addressing mode not supported for " + opcode + ": " + addressMode);
                return counter;
            }
            String rawHex = cleanString(words[2]);
            operandHex = toBigEndianBytes(rawHex);
            branchesMap.put(cleanString(label), opcodeHex);
        }else {
            if (words.length > 1){
                String addressMode = words[2];
                opcodeHex = row.get(addressMode);
                String operand = cleanString(words[1]);
                if (operand.contains("0x")) {
                    operandHex = toBigEndianBytes(operand);
                }else {
                    operandHex = operand;
                }
            } else {
                opcodeHex = row.get("default");
            }
        }
        String foundBranch = branchesMap.get(cleanString(operandHex));
        if (foundBranch != null) {
            operandHex = binaryCounter.get(foundBranch);
        }
         String length = opcodeHex + " " + operandHex;
        String[] codes = length.trim().split("\\s+");
         for (String code : codes) {
             binaryCounter.put(code, "00 " + String.format("%02X", counter & 0xFF));
             counter = (counter + 1) & 0xFF;
         }
         System.out.print(opcodeHex + " " + operandHex.replace(",", "") + " ");
        return counter;
     }
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("No file provided");
            System.exit(1);
        }
        Map<String, Map<String, String>> instructionsMap = new HashMap<>();
        Map<String, String> binaryCounter = new HashMap<>();
        Map<String, String> branchesMap = new HashMap<>();

        instructionsMap.put("STBA", Map.of("i", "F0", "d", "F1"));
        instructionsMap.put("LDBA", Map.of("i", "D0", "d", "D2"));
        instructionsMap.put("STWA", Map.of("i", "E0", "d", "E1"));
        instructionsMap.put("LDWA", Map.of("i", "C0", "d", "C1"));
        instructionsMap.put("ANDA", Map.of("i", "80", "d", "81"));
        instructionsMap.put("ASLA", Map.of("default", "0A"));
        instructionsMap.put("ASRA", Map.of("default", "0C"));
        instructionsMap.put("ADDA", Map.of("i", "60", "d", "61"));
        instructionsMap.put("CPBA", Map.of("i", "B0", "d", "B1"));
        instructionsMap.put("BRNE", Map.of("i", "1A"));
        instructionsMap.put("STOP", Map.of("default", "00"));
        instructionsMap.put(".END", Map.of("default", "zz"));

        String filePath = args[0];
        File file = new File(filePath);
        int counter = 0;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
             if (line.isEmpty()) continue;
             counter = conversion(instructionsMap, line, branchesMap, binaryCounter, counter);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found at " + filePath);
        } catch (Exception e) {
            System.err.println("An error occurred while reading the file.");
        }
    }
}