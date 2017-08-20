package br.uece.crs4gae;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**  
* CRS4BluemixWritingFiles.java - a class for refactor legacy code that make use of the file system and than enable this code to use the service Google Cloud Storage.  
* @author Erick Barros
* @version 1.0 
*/ 

public class CRS4BluemixWritingFiles {
	private static final String containerName = "OBJECT_STORAGE_CONTAINER_NAME";
	private static final String USERNAME = "OBJECT_STORAGE_USERNAME";
	private static final String PASSWORD = "OBJECT_STORAGE_PASSOWORD";
	private static final String DOMAIN_ID = "OBJECT_STORAGE_DOMAIN_ID";
	private static final String PROJECT_ID = "OBJECT_STORAGE_PROJECT_ID";
	
	/**  
	* Retrieve the GcsOutputChannel instance used in this class to write files in Google Cloud Storage.  
	* @return A instance of the GcsOutpuChannel class.
	*/ 
	public static GcsOutputChannel getOutputChannel() {
		return CRS4GAEWritingFiles.outputChannel;
	}
	
	/**  
	* Update the GcsOutputChannel instance.  
	* @parameter A instance of the GcsOutpuChannel class.
	*/ 
	public static void setOutputChannel(GcsOutputChannel outputChannel) {
		CRS4GAEWritingFiles.outputChannel = outputChannel;
	}
	
	/**  
	* Retrieve the GcsService instance used in this class to communicate with the Google Cloud Storage.  
	* @return A instance of the GcsService class.
	*/ 
	public static GcsService getGcsService() {
		return gcsService;
	}
	
	/**  
	* Retrieve a GcsFilename instance that represents the complete name of a file in the Google Cloud Storage.  
	* @parameter A string with the file name.
	* @return A instance of the GcsFilename class.
	*/ 
	public static GcsFilename newCloudFile(String nameFile) {
		return new GcsFilename(CRS4GAEWritingFiles.nameBucket, nameFile);
	}
	
	/**  
	* Create a file in the Google Cloud Storage.  
	* @parameter A string with the file name.
	* @return A instance of the GcsOutputChannel class.
	*/ 
	public static GcsOutputChannel create(GcsFilename fileName) throws IOException {
		return getGcsService().createOrReplace(fileName, GcsFileOptions.getDefaultInstance());
	}
	
	/**  
	* Write a array of bytes in the GcsOutputChannel instance used in this class.  
	* @parameter A array of bytes.
	*/ 
	public static void write(byte[] bytes) {
		try {
			getOutputChannel().write(ByteBuffer.wrap(bytes));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**  
	* Close the connection established through the GcsOutputChannel instance of this class.  
	*/ 
	public static void close() throws IOException  {
		getOutputChannel().close();
	}
	
	/**  
	* Convert a regular file of the file system to a byte array.
	* @parameter A InputStream instance.
	*/ 
	public static byte[] convertFileToByteArray(InputStream fs) throws FileNotFoundException, IOException {
		byte[] bytes = ByteStreams.toByteArray(fs);
		return bytes;
	}


	private ObjectStorageService authenticateAndGetObjectStorageService() {
		String OBJECT_STORAGE_AUTH_URL = "https://identity.open.softlayer.com/v3";

		Identifier domainIdentifier = Identifier.byId(DOMAIN_ID);

		OSClientV3 os = OSFactory.builderV3()
				.endpoint(OBJECT_STORAGE_AUTH_URL)
				.credentials(USERNAME,PASSWORD, domainIdentifier)
				.scopeToProject(Identifier.byId(PROJECT_ID))
				.authenticate();

		ObjectStorageService objectStorage = os.objectStorage();

		return objectStorage;
	}

	public void doPost(String containerName, String fileName, InputStream fileStream) throws IOException {
		ObjectStorageService objectStorage = authenticateAndGetObjectStorageService();

		if(containerName == null || fileName == null){ 
			System.out.println("File not found.");
			return;
		}

		Payload<InputStream> payload = new PayloadClass(fileStream);

		objectStorage.objects().put(containerName, fileName, payload);
	}

	private class PayloadClass implements Payload<InputStream> {
		private InputStream stream = null;

		public PayloadClass(InputStream stream) {
			this.stream = stream;
		}

		@Override
		public void close() throws IOException {
			stream.close();
		}

		@Override
		public InputStream open() {
			return stream;
		}

		@Override
		public void closeQuietly() {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}

		@Override
		public InputStream getRaw() {
			return stream;
		}
	}
}
