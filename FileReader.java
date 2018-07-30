package MyChordPackage;

import java.util.ArrayList;
import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;


public class FileReader {
	
	public ArrayList<String> fileContent = new ArrayList<String>();
	private Scanner input;
	
	public String chooseFileToRead(int number)
	{
		if(number==1)
		{
			return "/home/nick/Desktop/DistrFiles/insert.txt";
		}
		else if(number==2)
		{
			return "/home/nick/Desktop/DistrFiles/query.txt";
		}
		else
		{
			return "/home/nick/Desktop/DistrFiles/requests.txt";
		}
	}
	
	
	public void openFile(int whatFile)
	{
		try
		{
			String fileName=chooseFileToRead(whatFile);
			input=new Scanner(new File(fileName));
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Error Opening File");
			System.exit(1);
		}
	}
	
	/*
	public void readFile()
	{
		try{
			while(input.hasNext())
			{
				String lineOfFile = input.nextLine();
				fileContent.add(lineOfFile);
			}
		}
		catch(NoSuchElementException e1)
		{
			System.err.println("The format of the file is not correct");
			input.close();
			System.exit(1);
		}
		catch(IllegalStateException e2)
		{
			System.err.println("Error reading from the file");
			System.exit(1);
		}
	}
	*/
	
	public String readFile()
	{
		String lineOfFile="";
		try{
			if(input.hasNext())
			{
				lineOfFile = input.nextLine();
				fileContent.add(lineOfFile);
			}
		}
		catch(NoSuchElementException e1)
		{
			System.err.println("The format of the file is not correct");
			input.close();
			System.exit(1);
		}
		catch(IllegalStateException e2)
		{
			System.err.println("Error reading from the file");
			System.exit(1);
		}
		
		return lineOfFile;
	}
	
	public void closeFile()
	{
		if(input!=null)
		{
			input.close();
		}
		return ;
	}
	
	
	public String getLine(int number)
	{
		return fileContent.get(number);
	}
	
	
}