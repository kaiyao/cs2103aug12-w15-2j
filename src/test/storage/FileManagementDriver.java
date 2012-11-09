package test.storage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.joda.time.DateTime;

import main.shared.Task;
import main.storage.FileManagement;
import main.storage.WillNotWriteToCorruptFileException;

public class FileManagementDriver {
	public static void main(String args[])
	{
		ArrayList<Task> taskStoreTest = new ArrayList<Task>();
		
		
		
		FileManagement fileMgr = FileManagement.getInstance();
		fileMgr.prepareDatabaseFile();
		fileMgr.readFileAndDetectCorruption(taskStoreTest);
		System.out.println(fileMgr.getFileAttributes());
		
		Task floatingtask = new Task("Test Floating", true);
		Task floatingtask2 = new Task("Test Floating2", false);
		
		
		Task deadlinetask = new Task("Test deadline", new DateTime(), true);
		Task deadlinetask2 = new Task("Test deadline2", new DateTime().minusYears(5));
		
		Task timedtask = new Task("Test Timed", new DateTime().minusDays(100), new DateTime(2013, 12, 31, 00, 00), true);
		Task timedtask2 = new Task("Test Timed2", new DateTime(2011, 9, 5, 23,59), new DateTime(2013, 12, 31, 00, 00), false);
		
		taskStoreTest.add(floatingtask);
		taskStoreTest.add(floatingtask2);
		
		taskStoreTest.add(deadlinetask);
		taskStoreTest.add(deadlinetask2);
		
		taskStoreTest.add(timedtask);
		taskStoreTest.add(timedtask2);
		
		Collections.sort(taskStoreTest);
		
		
		try {
			fileMgr.writeDataBaseToFile(taskStoreTest);
		} catch (IOException | WillNotWriteToCorruptFileException e) {
			System.out.println("Fail");
			e.printStackTrace();
		} 

		
		
		
		
	}

}
