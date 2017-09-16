package edu.cmu.isr.casos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;

import javax.swing.JFileChooser;

/**
 * This script creates a very simple Condor script that runs Construct with a params file
 * for each iteration.  It creates a log file for each Construct Run, which is returned to the host machine
 * along with the output of the Construct.exe
 * 
 * This version has been slightly modified, and marked as such, because it includes an external Knowledge Network
 * File.
 * 
 *@author Geoff Morgan
 *
 */
public class ConverterTableToCondorScript {

	static String fullHashBar = "###########################################";
	
	/**
	 * @param Empty arguments, this method does not, for now, take advantage of arguments
	 */
	public static void main(String[] args) {
		
		/* Example Output that runs R
		 * 	universe = vanilla
			requirements = ((ARCH == "INTEL") && ((OPSYS == "WINNT52") || (OPSYS == "WINNT61")) && (Machine =!= LastRemoteHost) && (Memory >= 8000))
			rank = ((Memory>=8000) * (100*Mips + 20*KFlops + 4*Memory + 4*VirtualMemory))
			should_transfer_files = YES
			when_to_transfer_output = ON_EXIT
			executable = C:\Program Files\R\R-2.11.0-x64\bin\Rterm.exe
			transfer_executable = false
			notification = ERROR
			transfer_input_files = ../multisepp.R,../condor_run.R
			output = condorout.txt
			error = condorerr.txt
			log = condorlog.txt
			initialdir = condor_$(Process)
			arguments = "--vanilla"
			input = ../condor_run.R
			queue 20
		 */
		
		JFileChooser newFileChooser = new JFileChooser();
		newFileChooser.setDialogTitle("Identify Tab-Delimited Table to Convert for Condor!");
		int returnVal = newFileChooser.showOpenDialog(null);
		
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			JFileChooser writeFileChooser = new JFileChooser();
			writeFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
			writeFileChooser.setDialogTitle("Save the CONDOR Submit file.  Source File: " + newFileChooser.getSelectedFile().getName());
			writeFileChooser.setApproveButtonText("Save");
			returnVal =  writeFileChooser.showSaveDialog(null);

		// The approach we're going to take for now, because it allows maximum customization, is to simply parse a tab-delimited file
		
		if(returnVal == JFileChooser.APPROVE_OPTION)
		try {
			int jobNumber = 0;
			BufferedReader reader = new BufferedReader(new FileReader(newFileChooser.getSelectedFile()));
			BufferedWriter writer = new BufferedWriter(new FileWriter(writeFileChooser.getSelectedFile()));
			writer.write(fullHashBar+"\n# Condor Job Submission File\n");
			writer.write("# Generator Written By: Geoffrey P. Morgan (gmorgan@cs.cmu.edu), June 2010\n");
			writer.write("# File Converted: " + newFileChooser.getSelectedFile().getName() + "\n");
			writer.write("# Log Written To: " + writeFileChooser.getSelectedFile().getName() + "\n#\n" + fullHashBar + "\n\n");
			
			String header = reader.readLine();
			//System.out.println(header);
			String[] headerPieces = header.split("\t");
			HashMap<Integer, String> headerMap = new HashMap<Integer, String>();
			for(int i = 0; i < headerPieces.length; ++i) {
				//System.out.println(headerPieces[i]);
				if(!headerPieces[i].equals("Run")) {
					headerMap.put(i, headerPieces[i]);
				}
			}
			
			while(reader.ready()) {
				writer.write(fullHashBar + "\n#\n# Run Number " + ++jobNumber + "\n#\n" + fullHashBar + "\n" );
				String[] runPieces = reader.readLine().split("\t");
				for(int i = 0; i < runPieces.length; ++i) {
					String debugString = runPieces[i];
					runPieces[i] = runPieces[i].trim();
					if(runPieces[i].startsWith("\"")) {
						runPieces[i] = runPieces[i].substring(1, runPieces[i].length());
					}
					if(runPieces[i].endsWith("\"")) {
						runPieces[i] = runPieces[i].substring(0, (runPieces[i].length()-1));
					}
					System.out.println("Original: " + debugString + ", Converted: " + runPieces[i]);
					
					while(runPieces[i].contains("\"\"")) {
						char[] runPieceArray = runPieces[i].toCharArray();
						//System.out.println("CONVERSION of: " + runPieces[i]);
						String tempString = "";
						for(int j = 0; j < runPieces[i].length(); ++j) {
							if(runPieceArray[j] == '\"' && runPieceArray[(j+1)] == '\"') {
								tempString += runPieceArray[j];
								++j;
							}
							else {
								tempString += runPieceArray[j];
							}
							//System.out.println(tempString);
						}
						runPieces[i] = tempString;
						
					}
					
					
					if(headerMap.containsKey(i) && !runPieces[i].equals("")) {
						if(headerMap.get(i).equals("queue")) {
							writer.write((headerMap.get(i) + " " + runPieces[i] + "\n"));
						}
						else {
							writer.write((headerMap.get(i) + " = " + runPieces[i] + "\n"));
						}
						//System.out.println(headerMap.get(i) + " = " + runPieces[i]);
					}
				}
				writer.write("\n");
				writer.flush();
			}
			reader.close();
			writer.close();
		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
			e.printStackTrace();
		}
		}
	}

}
