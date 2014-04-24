//package com.cse141;

import java.io.*;
import java.util.*;

public class Asm extends Assembler{
	
	// Here we should put at least one constructor for Asm, to initialize all the member variables declared in Assember
	 Asm(){}
	 Asm(String[] args) throws IOException  //Construct an Assembler object with the file I/O handlers initialized. 
 {
   sourceFile = new BufferedReader(new FileReader(args[0]));
   out_code = new BufferedWriter(new FileWriter(args[1]+"_i.coe"));
   out_data = new BufferedWriter(new FileWriter(args[1]+"_d.coe"));
 }

	// you are right, i should not declare these hashmaps in a method, they should be the filed of a class
	HashMap<String,String> registerMap = new HashMap<String,String>();//registerFile.length);
	
	HashMap<String,String> instructionMap = new HashMap<String,String>();//instructionSetKey.length);
	
	public HashMap<String,Integer> addrMap = new HashMap<String,Integer>();// 
	
	// initialize some universally used tmps
	//int tmp_int;
	//int i;
	
	//overriding abstract methods in Assembler.java
   public void initialization()  throws IOException {
	   
		String[] registerFileKey = {"$a0","$a1","$v0","$s0","$s1","$s2","$s3","$t0","$t1",
									"$sp","$pc","$ra","$bcl","$bcr","$la","$zero"};
		
		String[] registerFileValue = {"0000","0001","0010","0011","0100","0101","0110","0111","1000",
				"1001","1010","1011","1100","1101","1110","1111"};
		
		String[] instructionSetKey = {"add","sub","nor","jr","in","out","lw","sw","move",
									"srl","addi","sloi","halt","be","blt","jal","jmp"};
		
		String[] instructionSetValue = {"000000","000001","000010","1011","000100","000101","0110","0111","0101",
									"0011","0010","0100","1111","1000","1001","101001","101000"}; 	
		
		// init the register hashmap
		for (int i = 0; i < registerFileKey.length; i++)
			registerMap.put(registerFileKey[i],registerFileValue[i]);
		
		// init the instructionSet hashmap
		for (int i = 0; i < instructionSetKey.length; i++)
			instructionMap.put(instructionSetKey[i],instructionSetValue[i]);
		
		// we do not init the addrOfLabel Hashmap here
		
//		System.out.println( registerMap );
//		System.out.println(instructionSetKey[8].charAt(1));
//		System.out.println( instructionMap );
//		System.out.println();
		
   }
   
   public void processLabel(String sourceCode){
	   if ( currentCodeSection == 0 ) // text
			addrMap.put(sourceCode, instructionCount); // 
		else if ( currentCodeSection == 1) // data
			addrMap.put (sourceCode, dataMemoryAddress); // should dataMemo - 1?
   }
   
   
   public void updateProgramCounter(Instruction instruction){
	  if ( instruction.operator.equals("la") )
	   {
		   
		   for ( int i = 0; i < 6; i ++)
		   {
			   instructions[instructionCount++] = processInstruction(new String("sloi $la,"+ instruction.operands[0].name));
			   //System.out.println(instructions[instructionCount-1].operator+" " +instructions[instructionCount-1].operands[0].name+ " " +instructions[instructionCount-1].operands[1].name);
		   }	
	   }
	   else
	   {
			instructionCount++;
	   }
   }
   

   public void replaceInstructionLabel(Instruction instruction){
	   
		  /* Integer tmp_integer = addrMap.get(instruction.operands[0].name);//must use integer, otherwise != null wont work
		   
		   if ( tmp_integer != null)
		   {
			   Integer offset_instruct = tmp_integer.intValue() - currentinstructionCount; // needs verification!
			   instruction.operands[0].name = offset_instruct.toString(); 
			 
		   }
		   else {}*/
	   if (instruction.operator.equals("sloi"))
	   {
		   if(instruction.operands[1].name.startsWith("0x")) return;
		   
		   if ( la_i == 0 )  // we are getting a new "la" pseudo-ins
		   {
			   Integer tmp_la_addr = addrMap.get(instruction.operands[1].name);
			  // System.out.println("warning:" + tmp_la_addr);
			   int offset_la = tmp_la_addr.intValue();// this is a decimal result
			   la_string = Integer.toBinaryString(offset_la);// this is a two's comp
			   
			   
			   //System.out.println("warning1:" + la_string);
			   // make sure that every la_char is 32-bit 
			   int la_length = la_string.length();
			   if (la_length < 36)
			   {
				   for (int i = 0; i < 36 - la_length; i ++)
				   {
					   la_string = "0"+la_string;
				   }
			   }
			  // newla_string = new String(la_string);
			 //  System.out.println("warning2:" + la_string);
			   //la_char = la_string.toCharArray();
			 // System.out.println("warning3:" + newla_string);
		   }
		   
		   for (la_j = 0; la_j < 6; la_j++)
		   {
			   la_sixdigit[la_j] = la_string.charAt(la_j + la_i * 6);//la_char[la_j + la_i * 6];
		   }
		  
		   //System.out.println(la_sixdigit[0]+"!"+la_sixdigit[1]+"!"+la_sixdigit[2]+"!"+la_sixdigit[3]+"!"+la_sixdigit[4]+"!"+la_sixdigit[5]);
		   // assign the new value to operands[1].name
		   
		   String c = new String(la_sixdigit);
		   //System.out.println(c);
		   int a = Integer.parseInt(c, 2);
		   String b = Integer.toHexString(a);
		   if (b.length() < 2)
			   b = "0" + b;
		   //System.out.println(b);
		   b = "0x"+b;
		   
		   instruction.operands[1].name =b;// new String(la_sixdigit);//.toString();
		   //System.out.println("warning5:" +instruction.operands[1].name);
		   // dectect whether have finished translating "la" or not 
		   la_i += 1;
		   if (la_i == 6)
		   {
			   la_i = 0;
		   }
	   }
	   
	   else
	   {
		   for (int i = 0; i < instruction.operands.length; i++)
		   {
			   Integer tmp_integer = addrMap.get(instruction.operands[i].name);//must use integer, otherwise != null wont work
			   //System.out.println(tmp_integer);
			   if ( tmp_integer != null)
			   {
				   Integer offset_instruct = tmp_integer.intValue() - currentinstructionCount; // needs verification!
				   instruction.operands[i].name = offset_instruct.toString(); 
				   //System.out.println(offset_instruct);
				   //System.out.println(instruction.operands[i].name);
			   }
			   else 
			   {
				  // outputErrorMessage("This is not a valid Instruction Label!!!");
			   }
			}
	   }
		
   }
   

   public void replaceMemoryLabel(){

	   for( int i = 0; i < memory.entries.length; i ++ )
		{
		   String[] rzeros={"","0","00","000","0000","00000","000000","0000000","00000000","000000000"};
		   Integer tmp_integer = addrMap.get(memory.entries[i].data);//must use integer, otherwise != null wont work
		  // System.out.println(tmp_integer);
		   if ( tmp_integer != null)
		   {
			   Integer offset_memo = tmp_integer.intValue() - memory.entries[i].address; // wrap int to a Integer type
			  // System.out.println(offset_memo);
			   memory.entries[i].data = Integer.toHexString(offset_memo); 
			   if (offset_memo >= 0)
				   memory.entries[i].data=rzeros[9 - memory.entries[i].data.length()]+memory.entries[i].data;
			  // System.out.println(memory.entries[i].data);
		   }
		   else
		   {
			  // outputErrorMessage("This is not a valid Memory Label!!!");
		   }
		}
   }
   
   /*********************************************************************************
    * This method is used to convert any number to its two's complement
    * As to how to interpret this encoding
    * it would be the simulator's work, we don't need to deal with it here
    ********************************************************************************/
   public char[] get2sComplement(int number, int width)
   {
	   int length,number_twosComp;
	   char[] tmp_charArray;
	   char[] returnArray = new char[width]; // the IDE prompts that returnArray should be initialized using null
	   //System.out.println(number);
	   // initialization of the returnArray
	   for ( int i = 0; i < width; i++)
		   returnArray[i] = '0';
	   
	   // if the nubmer is positive, no modification should be done
	   if ( number > 0 )
	   {
		   tmp_charArray = Integer.toBinaryString(number).toCharArray();
		   length = tmp_charArray.length; 
		   //System.out.println(length);
		   for (int i = 0; i < length; i ++)
			   returnArray[ (width -length) + i] = tmp_charArray[i];	   
	   }
	   else if ( number == 0 ){}
	   else //means number is negative
	   {
		   //number = -number;
		  // number_twosComp = number ^ 0xFFFFFFFF + 1; // int in Java has 32 bits
		   tmp_charArray = Integer.toBinaryString(number).toCharArray();// length would be 32-bit
		   //System.out.println(tmp_charArray);
		   length = tmp_charArray.length;
		  // System.out.println(length);
		   for (int i = 0; i < width; i++)
			   returnArray[i] = tmp_charArray[ (length - width) + i];
	   }
	   return returnArray;
   }
   //****************************************************************************************

   
   public String generateCode(Instruction instruction){
	   String[] gczeros={"","0","00","000","0000"};
	   char[] outputArray = new char[] {'0','0','0','0','0','0','0','0','0','0','0','0','0','0'};
	   char[] tmp_char_Array;
	   char[] firstFourBitArray = {'0','0','0','0'};
	   char[] opcodeArray;
	   String tmp_string;
	
	   // dealing with the opcode
	   String opcode = instructionMap.get(instruction.operator) ;
	  // System.out.println(opcode);
	   opcodeArray = opcode.toCharArray();	   
	   //System.out.println(opcodeArray);
	   if ( opcode.length() == 0 )
			outputErrorMessage("Not a valid operator!!");
		else
		{
			for (int i = 0; i < 4; i++)
			{		
				outputArray[i] = opcodeArray[i];
				firstFourBitArray[i] = opcodeArray[i];
			}
				
			if (opcodeArray.length == 6)
			{
				outputArray[12] = opcodeArray[4];
				outputArray[13] = opcodeArray[5];
			}
		}
		
		// dealing with the operands, use the first 4-bit opcode to determine formats
		//String firstFourBitString = firstFourBitArray
		//String firstFourBitString = Char.toString(firstFourBitArray);
		String firstFourBitString =new String(firstFourBitArray);
		//System.out.println(firstFourBitString);
		// System.out.println(firstFourBitString=="0001");
		// System.out.println(firstFourBitString.equals("0001"));
		
	   if (firstFourBitString.equals("0000") || firstFourBitString.equals("0001"))
		{
			for (int j = 0; j < 2; j++)
			{
				tmp_string = registerMap.get(instruction.operands[j].name);
				//System.out.println(instruction.operands[j].name);
				tmp_char_Array = tmp_string.toCharArray();
				for (int i = 0; i < 4; i++)
					outputArray[i+4+4*j] = tmp_char_Array[i];
			}
		}
		else if (firstFourBitString.equals("0010") || firstFourBitString.equals("0011") )
		{
				tmp_string = registerMap.get(instruction.operands[0].name);
				//System.out.println(instruction.operands[0].name);
				tmp_char_Array = tmp_string.toCharArray();
				
				for (int i = 0; i < 4; i++)
					outputArray[i+4] = tmp_char_Array[i];
				//long tmpimml = instruction.operands[1].extractImmediate();//
				int tmpimm = Integer.parseInt(instruction.operands[1].name);
				if (tmpimm > 31 || tmpimm < -32)
				{
					outputErrorMessage("Error: imm must be within [-32,31]!");
				}

				//this tmpimmArray has a length of 6, and the next thing is simple!
				char[]  tmpimmArray= get2sComplement(tmpimm, 6);
				for (int i = 0; i < 6; i++)
				{
					outputArray[i+8] = tmpimmArray[i];
				}
		}
		else if (firstFourBitString.equals("0100")) //sloi
		{
			tmp_string = registerMap.get(instruction.operands[0].name);
			tmp_char_Array = tmp_string.toCharArray();
			
			for (int i = 0; i < 4; i++)
				outputArray[i+4] = tmp_char_Array[i];
			
			long tmpimml = instruction.operands[1].extractImmediate();
			if (tmpimml > 63 || tmpimml < 0)
			{
				outputErrorMessage("Error: imm must be within [0,63]!");
			}
			int tmpimm = (int)tmpimml;
			char[]  tmpimmArray= get2sComplement(tmpimm, 6);// tmpimm would always be positive, so we would always get its normal binary code
			for (int i = 0; i < 6; i++)
			{
				outputArray[i+8] = tmpimmArray[i];
			}
		}
		else if (firstFourBitString.equals("0101") || firstFourBitString.equals("0110") || firstFourBitString.equals("0111"))
		{
			int tmpimm = instruction.operands[1].offset; // do this check first is much better than later
			switch (tmpimm){
				case 0:
					outputArray[12] = '0';
					outputArray[13] = '0';
					break;
				case 1:
					outputArray[12] = '0';
					outputArray[13] = '1';
					break;
				case 2:
					outputArray[12] = '1';
					outputArray[13] = '0';
					break;
				case 3:
					outputArray[12] = '1';
					outputArray[13] = '1';
					break;
				default:
					outputErrorMessage("Error! The offset here must be between [0,3]!!");
			}
			
			for (int j = 0; j < 2; j++)
			{
				tmp_string = registerMap.get(instruction.operands[j].name);
				//System.out.println(instruction.operands[j].name);
				tmp_char_Array = tmp_string.toCharArray(); // gooood, definitely would be a size of 4 array
				for (int i = 0; i < 4; i++)
					outputArray[i+4+4*j] = tmp_char_Array[i];
			}
			
		}// needs to be verified 
		else if (firstFourBitString.equals("1000") || firstFourBitString.equals("1001"))
		{	
			int tmpimm = instruction.operands[0].offset;// no offset here, the second operand is the offset; this way to need to modify the format of instruction, making the imm comes last
			switch (tmpimm){
			case 0:
				outputArray[12] = '0';
				outputArray[13] = '0';
				break;
			case 1:
				outputArray[12] = '0';
				outputArray[13] = '1';
				break;
			case 2:
				outputArray[12] = '1';
				outputArray[13] = '0';
				break;
			case 3:
				outputArray[12] = '1';
				outputArray[13] = '1';
				break;
			default:
				outputErrorMessage("Error! The offset here must be between [0,3]!!");
			}
			
			//begin process the label
			int label_offset = Integer.parseInt(instruction.operands[0].name);//Parses the string argument as a signed decimal integer
			
			char[] tmpArray = get2sComplement(label_offset, 8);
			for (int i = 0; i < 8; i ++)
				outputArray[4 + i] = tmpArray[i];			
		}// needs to be verified
		
		
		else if (firstFourBitString.equals("1010"))
		{
			//begin process the label
			int label_offset = Integer.parseInt(instruction.operands[0].name);//Parses the string argument as a signed decimal integer
			char[] tmpArray = get2sComplement(label_offset, 8);
			for (int i = 0; i < 8; i ++)
				outputArray[4 + i] = tmpArray[i];
			
		}
		else if (firstFourBitString.equals("1011")) //jr $reg
		{
			tmp_string = registerMap.get(instruction.operands[0].name);
			
			tmp_char_Array = tmp_string.toCharArray();
			for (int i = 0; i < 4; i++)
				outputArray[i+4] = tmp_char_Array[i];
		}
		else if (firstFourBitString.equals("1111"))
		{
			for (int i_halt = 0; i_halt < 14; i_halt ++)
			{
				if (i_halt < 4)
					outputArray[i_halt] = '1';
				else 
					outputArray[i_halt] = '0';
			}
		}
		
	   String outputString = new String(outputArray);
	   outputString = "000000"+outputString;
	   //System.out.println(outputString);
	   int outint = Integer.parseInt(outputString,2);
	   //System.out.println(outint);
	   outputString = Integer.toHexString(outint);
	   
	   while ( outputString.length() < 5 )
	   {
		   outputString = "0" + outputString;
	   }
	   //outputString=gczeros[4 - outputString.length()]+outputString;
	   System.out.println(currentinstructionCount+": "+outputString);
	   return outputString;
   }  
}
