/*
 * 
 * This class converts COE files to HEX files. HEX files are used by Altera tools
 * to initialize memory. COE files serve the same role in Xilinx tools.
 * 
 * Feel free to use the get_hex_line_string as part of your assembler so that you
 * can produce the HEX format natively. Just remember that you must write the hex_eof
 * string as the last line of the HEX file.
 * 
 * Thanks go to Matt Devuyst for creating the python script from which this
 * program was derived.
 * 
 * Author: Sat Garcia (sat@cs.ucsd)
 * Date: 5/18/2011
 * 
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;

public class hex_converter {
	
	static final String hex_eof = ":00000001FF";
	
	static class ProgParams {
		private int data_width_in_bits;
		private int radix;
		private String coe_filename;
		private String hex_filename;
		
		ProgParams()
		{
			data_width_in_bits = 34; // default width is 34
			radix = 16; // default radix is 16 (hex values)
			coe_filename = "";
			hex_filename = "";
		}
		
		void set_data_width_in_bits(int width) { data_width_in_bits = width; }
		int get_data_width_in_bits() { return data_width_in_bits; }
		
		void set_radix(int base) { radix = base; }
		int get_radix() { return radix; }
		
		void set_coe_filename(String filename) { coe_filename = filename; }
		String get_coe_filename() { return coe_filename; }
		
		void set_hex_filename(String filename) { hex_filename = filename; }
		String get_hex_filename() { return hex_filename; }
	}
	
	
	/**
	 * 
	 * @param coe_file
	 */
	static void check_coe_header(Scanner coe_file, int radix)
	{
		assert coe_file.hasNextLine();
		String line1 = coe_file.nextLine();
		assert line1.equals("MEMORY_INITIALIZATION_RADIX=" + radix + ";");
		
		assert coe_file.hasNextLine();
		String line2 = coe_file.nextLine();
		assert line2.equals("MEMORY_INITIALIZATION_VECTOR=");
	}

	static long make_checksum (int data_width_in_bytes, int address, long value)
	{
	    long result = data_width_in_bytes;
	    result += (address >> 8);
	    result += (address & 0xff);
	    
	    for(int i = 0; i < data_width_in_bytes; ++i) {
	        result = result + (value & 0xff);
	        value = value >> 8;
	    }
	    result &= 0xff;
	    result = 0x100 - result;
	    result &= 0xff;
	    
	    return result;
	}

	static String get_hex_line_string(String coe_line, int data_width_in_bytes, int address, int radix)
	{
		// Write the start code (a colon) to the hex file
	    String hex_line = ":";

	    // Write the byte count (of the data) to the hex file
	    hex_line += String.format("%02x", data_width_in_bytes);

	    // Write the address (of the data) to the hex file
	    hex_line += String.format("%04x", address);

	    // Write the record type (data record) to the hex file. This will always be 0 for us.
	    hex_line += String.format("%02x", 0);

	    // Get value from coe_line and write it to the hex file
	    long value = 0;

	    try {
	    	value = Long.parseLong(coe_line.replace(',', ' ').trim(),radix);
	    } catch(NumberFormatException e) {
		System.err.println("ERROR: Input line does not contain a valid number: " + coe_line);
		System.exit(1);
	    }

	    String format_string = "%0" + Integer.toString(data_width_in_bytes*2) + "x";
	    hex_line += String.format(format_string,value);

	    // Write the checksum to the hex file
	    long checksum = make_checksum(data_width_in_bytes, address, value);
	    hex_line += String.format("%02x", checksum);

	    // Write a newline to the hex file
	    hex_line += "\n";
	    
	    return hex_line;
	}

	static void translate_lines(Scanner coe_file, BufferedWriter hex_file, int data_width_in_bytes, int radix)
	{
	    int address = 0;
	    
	    while(coe_file.hasNextLine()) {
	        String coe_line = coe_file.nextLine();
	        
	        String hex_line = get_hex_line_string(coe_line, data_width_in_bytes, address, radix);
	        try {
				hex_file.write(hex_line);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        address++;
	    }
	    
		// write the EOF line
	    try {
			hex_file.write(hex_eof + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static void parse_command_line_args(String[] args, ProgParams params) {
		for(int i = 0; i < args.length; ++i) {
			// set the width
			if(args[i].equals("-w")) {
				params.set_data_width_in_bits(Integer.parseInt(args[i+1]));
				System.err.println("Using user-specified data width: " + params.get_data_width_in_bits() + " bits");
				i++;
			}
			
			// set the output (HEX) filename
			else if(args[i].equals("-o")) {
				System.err.println("Using user-specified output (HEX) filename: " + args[i+1]);
				params.set_hex_filename(args[i+1]);
				i++;
			}

			else if(args[i].equals("-b")) {
				System.err.println("Using user-specified radix: " + args[i+1]);
				params.set_radix(Integer.parseInt(args[i+1]));
				i++;
			}
			
			// print out usage instructions
			else if(args[i].equals("-h")) {
				System.err.println("\nUsage: java hex_converter [options] <input.coe>");
				System.err.println("Options:");
				System.err.println("\t-w: Specify the data width (in bits). Default value is 34.");
				System.err.println("\t-b: Specify the radix of the input file numbers. Default value is 16 (hex).");
				System.err.println("\t-o: Specify the output filename. Default output is the name of the input file with the extension changed to '.hex'.");
				System.err.println("Note: The input file must have a .coe file extension.");
				System.exit(0);
			}
			// check for unknown params
			else if(args[i].startsWith("-")) {
				System.err.println("ERROR: unknown option (" + args[i] + ")");
				System.exit(1);
			}
			else if(params.get_coe_filename().equals("")) {
				System.err.println("Input (COE) filename: " + args[i]);
				if(!args[i].endsWith(".coe")) {
					System.err.println("ERROR: COE file does not end with \".coe\"");
					System.exit(1);
				}
				params.set_coe_filename(args[i]);
			}
			else {
				System.err.println("ERROR: COE file specified twice. Run with \"-h\" option to get usage instructions.");
				System.exit(1);
			}
		}
		
		// check that we specified the input (COE) filename
		if(params.get_coe_filename().equals("")) {
			System.err.println("ERROR: No COE file specified.");
			System.exit(1);
		}
		
		// if we didn't use the -o option, we set the HEX output name to the same as the COE filename but with a ".hex" extension
		if(params.get_hex_filename().equals("")) {
			String hex_filename = params.get_coe_filename().substring(0,(params.get_coe_filename()).length()-3);
			hex_filename += "hex";
			
			System.err.println("Using auto-generated output (HEX) filename: " + hex_filename);
			params.set_hex_filename(hex_filename);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ProgParams params = new ProgParams();
		parse_command_line_args(args,params);
		
		int data_width_in_bytes = (int) Math.ceil(params.data_width_in_bits/8.0);
		
		BufferedWriter hex_file = null;
		try {
			hex_file = new BufferedWriter(new FileWriter(params.hex_filename));
		} catch (IOException e1) {
			System.err.println("ERROR: Could not open file for writing: " + params.hex_filename);
		}
		
		Scanner coe_file = null;
		try {
			coe_file = new Scanner(new File(params.coe_filename));
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: Couldn't find COE file: " + params.coe_filename);
		}
		
		check_coe_header(coe_file,params.get_radix());
		translate_lines(coe_file,hex_file,data_width_in_bytes,params.get_radix());
		
		coe_file.close();
		try {
			hex_file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
