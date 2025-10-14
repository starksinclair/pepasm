# PEP Assembly Language Assembler

A Java-based assembler for the PEP (Pep/8) assembly language. This tool converts PEP assembly instructions into their corresponding hexadecimal machine code representations.

## Project Overview

This assembler supports a subset of the PEP assembly language instructions and converts them to hexadecimal opcodes. It's designed for educational purposes in computer architecture and assembly language programming courses.

## Supported Instructions

The assembler currently supports the following PEP assembly instructions:

- **STBA** - Store Byte to Accumulator (F0 for immediate, F1 for direct addressing)
- **LDBA** - Load Byte to Accumulator (D0 for immediate, D2 for direct addressing)
- **STWA** - Store Word to Accumulator (E0 for immediate, E1 for direct addressing)
- **LDWA** - Load Word to Accumulator (C0 for immediate, C1 for direct addressing)
- **ANDA** - AND to Accumulator (80 for immediate, 81 for direct addressing)
- **ASLA** - Arithmetic Shift Left Accumulator (0A)
- **ASRA** - Arithmetic Shift Right Accumulator (0C)
- **ADDA** - Add to Accumulator (60 for immediate, 61 for direct addressing)
- **CPBA** - Compare Byte to Accumulator (B0 for immediate, B1 for direct addressing)
- **BRNE** - Branch if Not Equal (1A for immediate)
- **STOP** - Stop execution (00)
- **.END** - End of program marker (zz)

## How to Run

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- A text editor to create PEP assembly files

### Compilation

1. Navigate to the project directory:

   ```bash
   cd src
   ```

2. Compile the Java source file:
   ```bash
   javac pepasm.java
   ```

### Execution

Run the assembler with a PEP assembly file:
```bash
    java pepasm <filename.pep>
  ```

### Example Usage

```bash
  # Compile the program
javac pepasm.java

# Run with a sample program
java pepasm src/program1.pep

# Expected output for program1.pep:
# D0 00 48 F1 FC 16 D0 00 69 F1 FC 16 00 zz
```

## Input File Format

The assembler expects PEP assembly files with the following format:

- One instruction per line
- Instructions can have immediate (i) or direct (d) addressing modes
- Comments (starting with ;) are supported
- Empty lines are ignored
- Files should end with `.END`

### Example Input File (program1.pep):

```
LDBA 0x0048, i
STBA 0xFC16, d
LDBA 0x0069, i
STBA 0xFC16, d
STOP
.END
```

## Sample Programs

The project includes several sample PEP assembly programs:

- `program1.pep` - Basic load and store operations
- `program2.pep` - Word operations with shift instructions
- `program3.pep` - Additional examples
- `program4.pep` - More complex operations

## Output Format

The assembler outputs hexadecimal opcodes separated by spaces. Each instruction is converted to its corresponding machine code representation, with immediate values and addresses included as specified in the PEP architecture.

## Error Handling

The program includes basic error handling for:

- Missing command line arguments
- File not found errors
- Invalid instruction recognition

## Course Information

This project is part of CS230 coursework, focusing on assembly language programming and computer architecture concepts.
