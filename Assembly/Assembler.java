//  2009 Spring CSE 141 Project #1
//  Assembler Framework
//  Written by Hung-Wei Tseng

import java.io.*;
import java.util.*;

class MemoryEntry
{
  String data;  //A string stores the data word in an memory entry. 
  int address;  //The data memory address of the object. 
}

class Memory
{
  int size,j;      //size: Number of elements in the Memory class object. 
  MemoryEntry[] entries;  //entries: An array stores the data in the memory. 

  Memory()        //Construct an Memory object with the number of entries initialized to 1024.
  {
    entries = new MemoryEntry[1024];
    for(int j=0;j<1024;j++)
      entries[j] = new MemoryEntry();
    size=0;
  }

  Memory(int n)   //Construct an Memory object with the number of entries initialized to n.
  {
    entries = new MemoryEntry[n];
    for(int j=0;j<n;j++)
      entries[j] = new MemoryEntry();
    size=0;
  }

  public void add(String data,int address)  //Add the data into the Memory object and bookkeeping its data memory address.
  {
    entries[size].data = data;
    entries[size++].address = address;
  }

  public String find(int address)  //Find the data stored in input address. 
  {
    for(j=0;j<size;j++)
      if(address == entries[j].address)
        return entries[j].data;
    return null;
  }

  public void print()   //Print the content of current Memory object. 
  {
    for(j=0;j<size;j++)
      System.out.println(Integer.toHexString(entries[j].address)+"\t"+entries[j].data);
  }

  public String dump()   //Dump the content of current Memory object as the form of 34-bit machine code.
  {
    String output="";
    String[] zeros={"","0","00","000","0000","00000","000000","0000000","00000000","000000000"};
    for(j=0;j<size;j++)
    {
      String tempOutput = entries[j].data.substring(entries[j].data.lastIndexOf("0x")+2,entries[j].data.length());
      if(j<size-1)
        output+=zeros[9 - tempOutput.length()]+tempOutput+",\n";
      else
        output+=zeros[9 - tempOutput.length()]+tempOutput;
    }
    return output;
  }

  public int leng()  //Return the size of current entries. 
  {
    return size;
  }

}

class Operand
{
  public String name;  //A string stores the name of a operand.
  public int offset;  //A offset of this operand.

  Operand()   //Construct an Operand object with their name initialized as "", and offset initialized as 0 
  {
    name = "";
    offset = 0;
  }
  Operand(String i_name, int i_offset)  //Construct an Operand object with their name initialized as i_name, and offset initialized as i_offset 
  {
    name = i_name;
    offset = i_offset;
  }

  public int extractRegisterNumber() //If the operand is a register, this method will return an integer value of the register number. 
  {                                  //Otherwise, the method will return -1.
    if(name.startsWith("$"))
      return Integer.valueOf(name.substring(name.lastIndexOf("$")+1,name.length())).intValue();
    else
      return -1;
  }

  public long extractImmediate()  // If the operand is an immediate value, this method will return an integer value of the immediate value.
  {                               //Otherwise, the method will return Integer.MIN_VALUE. 
    //    if(name.startsWith("0x"))
    //        return Integer.valueOf(name.substring(name.lastIndexOf("0x")+2,name.length()),16).intValue();
    if(name.startsWith("0x"))
      return Long.valueOf(name.substring(name.lastIndexOf("0x")+2,name.length()),16).longValue();
    else
      return Integer.MIN_VALUE;
  }

  public String getOperandType()   //Examine if the input operand string is a meaningful term.
  {
    if(name.startsWith("$"))   //The returned String will be "register" if the operand starts with "$".
    {
      return "register";
    }
    else if(name.startsWith("0x"))  //The returned String will be "immediate" if the operand starts with "0x".
    {
      return "immediate";
    }
    else    //The returned String will be "label" if the operand cannot be categorize into the last two categories. 
    {
      return "label";
    }
  }
}
class Instruction   //For each instruction in the text section, the assembler framework will parse them into an array of Instruction objects.
{
  public String operator;  //A string stores the operator of the instruction.Take the code sw $5, 1($1) as an example, the operator will be the string "sw".
  public Operand operands[]; //An array of Operand objects stores the arguments of the instruction. 

  Instruction(String i_operator, Operand i_operands[])  //Construct an Instruction object using the assigned operator and operands. 
  {
    operator = i_operator;
    operands = new Operand[i_operands.length];
    for(int i=0;i<i_operands.length;i++)
    {
      operands[i] = new Operand();
      operands[i].name = i_operands[i].name;
      operands[i].offset = i_operands[i].offset;
    }
  }
  Instruction(String sourceCodeLine)   //Construct an Instruction object using the scanned source code. 
  {
    StringTokenizer st = new StringTokenizer(sourceCodeLine," ,\t");
    int numberOfTokens = st.countTokens();
    if(numberOfTokens > 0) // The first argument is operator
    {
      operator=st.nextToken();
      numberOfTokens--;
      operands = new Operand[numberOfTokens];
      for(int i = 0; i < numberOfTokens; i++)
      {
        operands[i] = new Operand();
        operands[i].name = st.nextToken();
        if(operands[i].name.lastIndexOf("(") >= 0)
        {
          operands[i].offset = Integer.valueOf(operands[i].name.substring(0,operands[i].name.lastIndexOf("("))).intValue();
          operands[i].name = operands[i].name.substring(operands[i].name.lastIndexOf("(")+1, operands[i].name.lastIndexOf(")"));
        }
      }
    }
  }


  public void print()   //Print the operartor and operands of the current Instruction object.
  {
    String output="";
    for(int i=0;i<operands.length;i++)
    {
      output += i+":"+operands[i].name+" "+operands[i].offset+"\t";
    }
    System.out.println(operator+"\t"+output);
  }

}

public abstract class Assembler
{
  Assembler() {}   //Construct an Assembler object without any initialization.
  Assembler(String[] args) throws IOException  //Construct an Assembler object with the file I/O handlers initialized. 
  {
    sourceFile = new BufferedReader(new FileReader(args[0]));
    out_code = new BufferedWriter(new FileWriter(args[1]+"_i.coe"));
    out_data = new BufferedWriter(new FileWriter(args[1]+"_d.coe"));
  }
  public BufferedReader sourceFile; //The file reader of the input source file. You need to intialize it before processing the source code. 
  public BufferedWriter out_code, out_data; //out_code:The file writer for machine code output (*_i.coe). You need to initialize it before generating any code output.
                                          //out_data: The file writer for data code output (*_d.coe). You need to initialize it before generating any data output. 
  /* keywords of your asseblemly language, and of course, you may override it.*/
  public String[] keywords;
  //The array stores the keywords used by the Assembler. You may override it in your class to extend the assembly language with more keywords. 
  /* memory table*/
  public Memory memory = new Memory();  //The object stores the parsed data words.
  /* instructions */
  public Instruction instructions[] = new Instruction[1024]; //An array of Instruction objects that stores the parsed instructions.
  /* number of scanned instructions */
  public int instructionCount = 0;  //The number of scanned instructions. 
  /* where are we now. */
  public int currentCodeSection = 0; // 0 for text, 1 for data //The current code section. By default, 0 stands for text section, and 1 for data section.
  /* The next program counter */
  public int programCounter = 0;  //The current program counter. You may need it while processing labels. 
  /* The next data memory address */
  public int dataMemoryAddress = 0;  //The current data memory address. You may need it while processing labels. 
  /* The number of lines scanned */
  int currentSourceCodeline = 0;  //How many source code lines has been scanned. You may not need it since the framework only uses for generating error messages. 
  // Get the next line from input file
  public String getNextInputLine() throws IOException
  {            //Get the next non-comment or non-empty line from the source code file, and return the scanned line with a String.
    // All contents after "//" will be ignored and will not be contained in the returned String. Once the source code reaches an EOF, the method will return a "null". 
	if(sourceFile == null)
      System.out.println("The source code file handler is not initialized");

    if(out_code == null)
      System.out.println("The output code file handler is not initialized");

    if(out_data == null)
      System.out.println("The output memory file handler is not initialized");

    while(sourceFile.ready())
    {
      currentSourceCodeline++;
      // get the next line.
      String sourceCodeLine = sourceFile.readLine().trim();
      // get rid of the comments
      if(sourceCodeLine.startsWith("//"))
      {
        continue;
      }
      if(sourceCodeLine.indexOf("//") != -1)
      {
        sourceCodeLine = sourceCodeLine.substring(0,sourceCodeLine.indexOf("//")).trim();
      }
      // trim the leading spaces and return the source code line.
      sourceCodeLine = sourceCodeLine.trim();
      /* remove the comments */
      if(sourceCodeLine.length() == 0)
      {
        continue;
      }
      return sourceCodeLine;
    }
    return null;
  }
  // Check if the input line contains a keyword
  boolean isKeyword(String sourceCodeLine)
  {  //Examine if the input line from source code contains a keyword.
    if(sourceCodeLine.startsWith("."))
      return true;
    else
      return false;
  }
  // Extract the input line with the keywords stored in keywords array
  String extractKeyword(String sourceCodeLine)  //Extract the keyword from the input string.
  {
    for(int i = 0; i< keywords.length; i++)
    {
      if(sourceCodeLine.startsWith(keywords[i]))
      {
        return keywords[i];
      }
    }
    outputErrorMessage("Hey! The line does not contain any keyword!");
    return null;
  }
  // check if the input contains a label
  boolean isLabel(String sourceCode)
  {
    if(sourceCode.lastIndexOf(":") >= 0)
      return true;
    else
      return false;
  }
  // extract the label from a source code input
  String extractLabel(String sourceCode)  //Extract the label from the input string. 
  {
    if(sourceCode.lastIndexOf(":") >= 0)
    {
      String label = sourceCode.substring(0,sourceCode.lastIndexOf(":"));
      if(label.length()!=0)
        return label;
      else
        return null;
    }
    else
      return null;
  }
  // process the instruction
  Instruction processInstruction(String sourceCode) //Parse the input source code into an Instruction object.
  {
    Instruction instruction = new Instruction(sourceCode);
    return instruction;
  }
  // process the data.
  void processData(String sourceCode)  //Parse the source code from the data section into the memory object of Assembler.
  {
    if(sourceCode.startsWith(".word"))
    {
      StringTokenizer st = new StringTokenizer(sourceCode," ,\t");
      int numberOfRemainingTokens = st.countTokens();
      /* Fill the words into memory */
      while(numberOfRemainingTokens > 0)
      {
        numberOfRemainingTokens--;
        String data = st.nextToken();
        if(data.startsWith(".word"))
          continue;
        memory.add(data, dataMemoryAddress);
        dataMemoryAddress++;
      }
    }
    /* Process the .fill keyword */
    else if(sourceCode.startsWith(".fill"))
    {
      StringTokenizer st = new StringTokenizer(sourceCode," ,\t");
      int numberOfRemainingData;
      if(st.countTokens() !=3 )
      {
        outputErrorMessage("Error: .fill should be in the form of .fill n data");
      }
      String data = st.nextToken();
      if(data.startsWith(".fill"))
      {
        int numberOfRemainingElements = Integer.valueOf(st.nextToken()).intValue();
        String dataToFill = st.nextToken();
        for(int i = 0;i<numberOfRemainingElements;i++)
        {
          memory.add(dataToFill, dataMemoryAddress);
          dataMemoryAddress++;
        }
      }
    }
  }

  // The static function returns the operand type.
  public static String getOperandType(String operand)  //Examine if the input operand string is a meaningful term.
  {
    if(operand.startsWith("$"))  // The returned String will be "register" if the operand starts with "$".
    {
      return "register";
    }
    else if(operand.startsWith("0x")) //The returned String will be "immediate" if the operand starts with "0x".
    {
      return "immediate";
    }
    else  //The returned String will be "label" if the operand cannot be categorize into the last two categories. 
    {
      return "label";
    }
  }

  public void outputErrorMessage(String errorMessage)  //Output the error message and show the incorrect source code line
  {
    System.out.println("Line "+currentSourceCodeline+": "+errorMessage);
  }

  // You need to override it if you have new keywords!
  void processAdditionalKeywords(String sourceCode)  //You may override to process additional keywords. 
  {
    outputErrorMessage("Sorry, we don't know how to process it");
    return;
  }

  // The student has to implement it for processing the labels.
  abstract void processLabel(String sourceCode);  //You have to implement it to process the labels.

  /**
   * The student has to implement it for generating the machine codes.
   * In general, one instruction translates to a single line of machine
   * code, but a psuedo instruction may generate multiple lines of
   * machine code (one string with several newlines in it)
   */
  abstract String generateCode(Instruction instruction);

  /**
   * The student has to implement it for updating the program counter
   * For most instructions this is simply pc++, but pseudo-instructions
   * are a bit more complicated
   *
   * If the pseudo-instruction expands to 3 real instructions, then
   * you should increment pc by 3 so that all your addresses following
   * this instructions will be correct.
   */
  abstract void updateProgramCounter(Instruction instruction);

  // The student has to implement it for initializing some of their own
  // variables and data structures.
  abstract void initialization()  throws IOException; //The method is called before the source code is scanned or generated. 
    //You have to implement it to initialize some variables, such as file descriptors or local variables, in your class. 
  /**
   * The student has to implement it for replacing the labels used in
   * instructions
   */
  abstract void replaceInstructionLabel(Instruction instruction);

  /**
   * The student has to implement it for replacing the labels used in memory
   * For example, consider the code
   * LineNum
   * 10      Foo:       .word 0x27
   *
   * 17      Foo_ptr:   Foo
   *
   * Here, Foo_ptr is a label that points to Foo; Foo (on line 17) needs to be
   * replaced with its address so that Foo_ptr is actually a ptr (to Foo)
   */
  abstract void replaceMemoryLabel();
  //The method is called before the data memory is dumped to a $prefix_d.coe file. It will replace the data written in labels with immediate values. 
  
  
  /* The core of our assembler */
  public void AssembleCode(String[] arg) throws IOException
  { //The most important method in Assembler class. This function controls the flow of Assembler. You may not need to change it.
	  //However, you may also override it if you have a better algorithm than our current two-parse algorithm. 
    if(arg.length < 2)
    {
      System.out.println("Usage: java Assembler input_filename output_file_prefix ");
      return;
    }
    String keywordString = ".text .word .data .fill";
    keywords = keywordString.split(" ");
    initialization();
    //Pass 1: Scan the source code line
    String sourceCodeLine = getNextInputLine();
    while(sourceCodeLine != null)
    {
      if(isKeyword(sourceCodeLine))
      {
        /* Extract the keyword from scanned source code */
        String keyword = extractKeyword(sourceCodeLine);
        if(keyword == null)
        {
          outputErrorMessage("Error! It's not a valid keyword!");
        }
        /* Change the current code section to text */
        else if(keyword.equalsIgnoreCase(".text"))
          currentCodeSection = 0;
        /* Change the current code section to data */
        else if(keyword.equalsIgnoreCase(".data"))
          currentCodeSection = 1;
        else if(keyword.equalsIgnoreCase(".word") || keyword.equalsIgnoreCase(".fill"))
        {
          processData(sourceCodeLine);
        }
        else
        {
          processAdditionalKeywords(sourceCodeLine);
        }
      }
      else if(isLabel(sourceCodeLine))
      {
        String label = extractLabel(sourceCodeLine);
        if(label != null)
          processLabel(label);
        else
          outputErrorMessage("The input line does not contains a label");
      }
      else
      {
        instructions[instructionCount] = processInstruction(sourceCodeLine);
        updateProgramCounter(instructions[instructionCount]);
        instructionCount++;
      }
      sourceCodeLine = getNextInputLine();
    }
    // Pass 2: Replace labels and output the code and memory.
    // output code
    out_code.write("MEMORY_INITIALIZATION_RADIX=16;\nMEMORY_INITIALIZATION_VECTOR=\n");
    for(int i=0; i < instructionCount; i++)
    {
      replaceInstructionLabel(instructions[i]);
      String tempOutput = generateCode(instructions[i]);
      if(i < instructionCount-1)
      {
        out_code.write(tempOutput+",\n");
      }
      else
        out_code.write(tempOutput);
    }
    // replace labels in data field.
    replaceMemoryLabel();
    // output the memory states.
    out_data.write("MEMORY_INITIALIZATION_RADIX=16;\nMEMORY_INITIALIZATION_VECTOR=\n");
    out_data.write(memory.dump());
    out_code.close();
    out_data.close();
  }
}

