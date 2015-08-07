import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;



public class TimeLapse {

	//public static Path dir = Paths.get("/Users/gonsalves-admin/Documents/Tech/TimeLapse");
	public static Path dir = Paths.get("/Volumes/TIMELAPSE");
	public ArrayList<File> list = new ArrayList<File>();

	//Print contents only inside single directory
	public void printTopContents()
	{
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) 
		{
			for (Path file: stream) {
				System.out.println(file.getFileName());
			}
		} 
		catch (IOException | DirectoryIteratorException x) {
			// IOException can never be thrown by the iteration.
			// In this snippet, it can only be thrown by newDirectoryStream.
			System.err.println(x);
		}
	}

	//Print contents recursively
	public void printRecursiveContents()
	{
		//Code from internet
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) 
		{
			for (Path file: stream) {
				//Check to see if the current file we see is a directory
				if (Files.isDirectory(file))
				{
					//Set current directory
					dir = file;
					//Search through for more directories OR print contents
					this.printRecursiveContents();
				}
				BasicFileAttributes attr = Files.readAttributes(file,BasicFileAttributes.class);
				String out = String.format("Filename:%s, Creation Date:%s",file.getFileName(),attr.creationTime().toString());
				System.out.println(out);
			}
		} 
		catch (IOException | DirectoryIteratorException x) {
			// IOException can never be thrown by the iteration.
			// In this snippet, it can only be thrown by newDirectoryStream.
			System.err.println(x);
		}
	}

	public void contentsToList()
	{
		//Code from internet
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) 
		{
			for (Path file: stream) {
				//Check to see if the current file we see is a directory
				if (Files.isDirectory(file))
				{
					//Set current directory
					dir = file;
					//Search through for more directories OR print contents
					this.contentsToList();
				}
				if (file.toString().endsWith(".jpg"))
				{
					list.add(file.toFile());
				}
			}
		} 
		catch (IOException | DirectoryIteratorException x) {
			// IOException can never be thrown by the iteration.
			// In this snippet, it can only be thrown by newDirectoryStream.
			System.err.println(x);
		}
	}

	public void printList()
	{
		for (int a =0; a < list.size(); a++)
		{
			Path f = list.get(a).toPath();
			try {
				BasicFileAttributes attr = Files.readAttributes(f,BasicFileAttributes.class);
				String out = String.format("Modified: %s, Creation Date:%s, Filename:%s ",attr.lastModifiedTime(), attr.creationTime().toString(),f.toString());
				System.out.println(out);
				//System.out.println(list.get(a).toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void sortList()
	{
		Collections.sort(list, new CreationDateComparator());
	}

	static class CreationDateComparator implements Comparator<File>
	{
		@Override
		public int compare(File o1, File o2) {
			// TODO Auto-generated method stub
			return Long.compare(o1.lastModified(),o2.lastModified());
		}
	}

	public void oneDay()
	{
		contentsToList();
		sortList();

		int counter = 0;
		int num_of_days = 500;
		Path output_dir = Paths.get("/Users/gonsalves-admin/Desktop/TimeJava/OneDay");
		SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss");
		Date begin_time;
		Date end_time;
		long span_time; //seconds
		long current_time = 0;
		long buffer_time = 60*5; //5 minutes in seconds
		long increment = 0;
		ArrayList <File> time_list = new ArrayList<File>();

		try {
			begin_time=df.parse("06:00:00");
			end_time=df.parse("20:00:00");
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(begin_time);
			Calendar cal3 = Calendar.getInstance();
			cal3.setTime(end_time);

			long begin_time_hour=cal2.get(Calendar.HOUR_OF_DAY);
			long begin_time_min=cal2.get(Calendar.MINUTE);
			long begin_time_sec=cal2.get(Calendar.SECOND);
			long begin_time_seconds=(begin_time_hour*60*60)+(begin_time_min*60)+begin_time_sec;

			long end_time_hour=cal3.get(Calendar.HOUR_OF_DAY);
			long end_time_min=cal3.get(Calendar.MINUTE);
			long end_time_sec=cal3.get(Calendar.SECOND);
			long end_time_seconds=(end_time_hour*60*60)+(end_time_min*60)+end_time_sec;

			span_time = end_time_seconds-begin_time_seconds;
			current_time = begin_time_seconds;
			increment = span_time/num_of_days;
			System.out.println("Current Time: "+current_time);
			System.out.println("Span time (seconds): "+span_time);
			System.out.println("Increment time (seconds/day): "+increment);

		} catch (ParseException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		try 
		{
			Files.createDirectory(output_dir);
		}
		catch (FileAlreadyExistsException x) 
		{
			System.err.format("file named %s" +
					" already exists%n", output_dir);
		}
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			System.err.format("createFile error: %s%n", e1);
		}

		for (int a =0; a < list.size(); a++)
		{
			Path f = list.get(a).toPath();
			try {
				BasicFileAttributes attr = Files.readAttributes(f,BasicFileAttributes.class);
				Date current_file_time = new Date(attr.creationTime().toMillis());

				Calendar cal=Calendar.getInstance();
				cal.setTime(current_file_time);

				long current_file_hour=cal.get(Calendar.HOUR_OF_DAY);
				long current_file_min=cal.get(Calendar.MINUTE);
				long current_file_sec=cal.get(Calendar.SECOND);
				long current_file_seconds=(current_file_hour*60*60)+(current_file_min*60)+current_file_sec;

				if ((current_file_seconds >= (current_time - buffer_time)) && (current_file_seconds <= current_time ))
				{
					counter++;
					//					System.out.println("Current Time: "+current_time);
					//					System.out.println("Curent File Hour: "+current_file_hour+" Min: "+current_file_min+" Sec: "+current_file_sec);
					//					System.out.println("Current File Time: "+current_file_seconds);
					String out = String.format("Creation Date:%s, Filename:%s ",attr.creationTime().toString(),f.toString());
					System.out.println(out);
					time_list.add(f.toFile());

					//String out = String.format("Creation Date:%s, Filename:%s",attr.creationTime().toString(),f.toString());
					Path path_out = Paths.get(output_dir.toString()+"/image"+counter+".jpg");
					System.out.println(f.toString());
					System.out.println(path_out.toString());
					Files.copy(f, path_out);
					current_time = current_time + increment;
				}


			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//Print to File
		printToFile(time_list,"TimeList.txt");
	}

	public void All_photos()
	{
		contentsToList();
		sortList();

		int counter_all = 0;
		int counter_sparse = 0;
		Path output_dir = Paths.get("/Users/gonsalves-admin/Desktop/TimeJava/NoonPhotos_ALL");
		ArrayList <File> time_list = new ArrayList<File>();

		int begin_time_AM=12;
		int end_time_PM=1;
		begin_time_AM=begin_time_AM*60*60;
		end_time_PM=(end_time_PM+12)*60*60;


		try 
		{
			Files.createDirectory(output_dir);
		}
		catch (FileAlreadyExistsException x) 
		{
			System.err.format("file named %s" +
					" already exists%n", output_dir);
		}
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			System.err.format("createFile error: %s%n", e1);
		}

		for (int a =0; a < list.size(); a++)
		{
			++counter_all;
			Path f = list.get(a).toPath();

			try {
				BasicFileAttributes attr = Files.readAttributes(f,BasicFileAttributes.class);
				Date current_file_time = new Date(attr.creationTime().toMillis());

				Calendar cal=Calendar.getInstance();
				cal.setTime(current_file_time);

				long current_file_hour=cal.get(Calendar.HOUR_OF_DAY);
				long current_file_min=cal.get(Calendar.MINUTE);
				long current_file_sec=cal.get(Calendar.SECOND);
				long current_file_seconds=(current_file_hour*60*60)+(current_file_min*60)+current_file_sec;


				if (current_file_seconds <= end_time_PM && current_file_seconds >= begin_time_AM) 
				{
					++counter_sparse;
					//String out = String.format("Creation Date:%s, Filename:%s",attr.creationTime().toString(),f.toString());
					Path path_out = Paths.get(output_dir.toString()+"/image"+counter_sparse+".jpg");
					System.out.println(f.toString());
					System.out.println(path_out.toString());
					try {
						Files.copy(f, path_out);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}


		}

		//Print to File
		printToFile(time_list,"ALL_TimeList.txt");
		System.out.println("Counter All: "+counter_all);
		System.out.println("Counter Sparse: "+counter_sparse);
	}


	public void printToFile(ArrayList<File> list, String output_name)
	{

		Path output_path = Paths.get("/Users/gonsalves-admin/Desktop/"+output_name);
		byte[] buffer;
		try 
		{
			Files.createFile(output_path);
		}
		catch (FileAlreadyExistsException x) 
		{
			System.err.format("file named %s" +
					" already exists%n", output_path);
		}
		catch (IOException e1) 
		{
			// TODO Auto-generated catch block
			System.err.format("createFile error: %s%n", e1);
		}

		for (int a =0; a < list.size(); a++)
		{

			Path f = list.get(a).toPath();
			try {
				BasicFileAttributes attr = Files.readAttributes(f,BasicFileAttributes.class);
				String out = String.format("Modified: %s, Creation Date:%s, Filename:%s",attr.lastModifiedTime(), attr.creationTime().toString(),f.toString());
				buffer=out.getBytes();
				Files.write(output_path, buffer,StandardOpenOption.APPEND);
				String newline = "\n";
				buffer=newline.getBytes();
				Files.write(output_path, buffer,StandardOpenOption.APPEND);
				//System.out.println(list.get(a).toString());
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
