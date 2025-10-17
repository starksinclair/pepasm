import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class pepasm {
    static String cleanString(String s) {
        return s.replaceAll("[: ,]+$", ""); // drops trailing ':', ',' and spaces
    }
    /**
     * Converts a 16-bit hexadecimal literal to a big-endian byte string "HH LL".
     * <p>
     * Accepts inputs with or without a {@code 0x} / {@code 0X} prefix and with or without
     * leading zeros, e.g. {@code "0x0040"}, {@code "0x40"}, {@code "0040"}, {@code "40"}.
     * Only the least-significant 16 bits are kept.
     *
     * <p><b>Examples:</b>
     * <pre>{@code
     * toBigEndianBytes("0x0041") -> "00 41"
     * toBigEndianBytes("0xFC16") -> "FC 16"
     * toBigEndianBytes("0040")   -> "00 40"
     * }</pre>
     *
     * @param hex a hex string representing a value (optionally prefixed with {@code 0x}).
     *            Must contain only hexadecimal digits after the optional prefix.
     * @return a two-byte big-endian representation formatted as {@code "HH LL"}.
     * @throws IllegalArgumentException if {@code hex} is empty.
     */
    static String toBigEndianBytes(String hex) {
        if (hex.isEmpty()) {
            throw new IllegalArgumentException("Please enter a valid hexadecimal string");
        }
        int v = Integer.parseUnsignedInt(hex.replaceFirst("^0x", ""), 16) & 0xFFFF;
        int hi = (v >>> 8) & 0xFF;
        int lo = v & 0xFF;
        return String.format("%02X %02X", hi, lo);
    }
    /**
     * Assembles a single source line into machine code.
     * <p>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>Looks up the opcode in {@code instructionsMap} to obtain the hexadecimal opcode byte
     *       for the specified addressing mode (or the {@code "default"} form).</li>
     *   <li>Operands that look like hex literals (e.g., {@code 0x0040}, {@code 0040}, {@code 40})
     *       are converted to a two-byte big-endian string via {@code toBigEndianBytes}, otherwise
     *       the operand is used as-is (e.g., labels).</li>
     *   <li>If the first token is not found in {@code instructionsMap}, it is treated as a label and
     *       the second token must be the opcode. The label is recorded in {@code branchesMap} with
     *       the opcode hex at which it appears.</li>
     *   <li>After formatting, each emitted byte token (opcode and operand bytes) is inserted into
     *       {@code binaryCounter} with the current counter encoded as {@code "00 XX"}. The counter
     *       is then incremented and wrapped to 8 bits: {@code (counter + 1) & 0xFF}.</li>
     *   <li>If {@code operandHex} matches a key in {@code branchesMap}, the operand is replaced
     *       by the corresponding entry from {@code binaryCounter} (label/branch resolution step).</li>
     *   <li>Emits the final bytes to {@code System.out} in the form {@code "OP OPERAND... "}.</li>
     * </ul>
     *
     * <h3>Notes</h3>
     * <ul>
     *   <li>{@code instructionsMap} uses <b>opcode</b> as the key (e.g., {@code "LDBA"}),
     *       and maps addressing modes (e.g., {@code "i"}, {@code "d"}) to the
     *       opcode hex string (e.g., {@code "D0"}).</li>
     *   <li>{@code cleanString} is used to strip punctuation such as commas from tokens.</li>
     *   <li>On lookup failures (unknown opcode or addressing mode), the method prints an error message
     *       and returns the input {@code counter} unchanged.</li>
     * </ul>
     *
     * @param instructionsMap mapping of opcode → (addressMode → opcode hex), e.g. {@code LDBA → { i: D0, d: D2 }}
     * @param line            a single source line to assemble (maybe label-first or opcode-first)
     * @param branchesMap     mapping of label → opcode hex at label location (used for branch/label resolution)
     * @param binaryCounter   mapping of each emitted byte token → its assembled address string {@code "00 XX"}
     * @param counter         current 8-bit program counter; incremented per emitted byte and wrapped via {@code & 0xFF}
     * @return                updated {@code counter} after emitting this line’s bytes
     */
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