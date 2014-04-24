import java.io.*;
import java.util.*;

public class main {


public static void main(String[] arg) throws IOException
{
	
	    Asm test_assembler = new Asm(arg);// does our Asm have a constructor yet???
	    
	    test_assembler.AssembleCode(arg);
	
   // Asm assembler = new Asm();
   // assembler.initialization();
  //  Instruction Ins = assembler.processInstruction("lw $s2,3($t0)");
   // System.out.println(Ins.operator +" " + Ins.operands[0].name+" " +Ins.operands[1].name + " " +Ins.operands[1].offset);// + " " + Ins.operands[1].name +" " + Ins.operands[1].offset);
   // String Strins = assembler.generateCode(Ins);
  //  System.out.println(Strins);
    //assembler.AssembleCode(arg); 
   // String textt = Integer.toBinaryString(-3);
  //  System.out.println(textt);
}

}