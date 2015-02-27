package Process;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.imageio.ImageIO;

public class Processing 
{
	public void checkSQS(String SQSname) throws Exception
	{
		AmazonSQS sqs;
		   
	   	AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
	   	sqs = new AmazonSQSClient(credentialsProvider);
	   
	   	String url = "...";

	   	while(true)
	   	{
	   		List <com.amazonaws.services.sqs.model.Message> messages = 
	   				sqs.receiveMessage(new ReceiveMessageRequest(url).withMaxNumberOfMessages(1)).getMessages();
	   		
	   		if (messages.size() > 0)
	   		{
	   			com.amazonaws.services.sqs.model.Message message = messages.get(0);
	   			System.out.println("Message: " + message.getBody());
	   			
	   			convert(message.getBody(),SQSname);
	   			sqs.deleteMessage(new DeleteMessageRequest(url, message.getReceiptHandle()));
	   		}
	   		else
	   		{
	   			System.out.println("Waiting...");
	   			Thread.sleep(10000);
	   		}
	   	}
	}

	private void convert(String message, String sqsName) throws Exception 
	{
		// TODO Auto-generated method stub	
		String bucket = "kamil-projekt";
		String key = message;
		
		BufferedImage image = GetImageFromS3(bucket, key);
		image = ImageConverter.reverseImage(image);
		SaveThistoS3(image, bucket, key);
	}

	private void SaveThistoS3(BufferedImage image, String bucket, String key) throws Exception 
	{
		// TODO Auto-generated method stub
		File file_temp2 = new File("Another_temporary");
		ImageIO.write(image, "jpg", file_temp2);
		
		AmazonS3 s3;
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        s3 = new AmazonS3Client(credentialsProvider);
        PutObjectRequest por = new PutObjectRequest(bucket, key, file_temp2);
        
        por.setCannedAcl(CannedAccessControlList.PublicRead);
        
        s3.putObject(por);
        
        System.out.println("Uploaded: " + por.getKey());
	}

	private BufferedImage GetImageFromS3(String bucket, String key) throws Exception 
	{
		// TODO Auto-generated method stub
		AmazonS3 s3;
		AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        s3 = new AmazonS3Client(credentialsProvider);
        
        S3Object object = s3.getObject(bucket, key);
        System.out.println("Downloaded image: " + key);
        
        File file_temp = new File("temporary.jpg");
        if (!file_temp.exists())
        {
        	file_temp.createNewFile();
        }
        
        InputStream in = object.getObjectContent();
		byte[] buf = new byte[1024];
		OutputStream out = new FileOutputStream(file_temp);
		int count;
		while ((count = in.read(buf)) != -1) 
		{
			if (Thread.interrupted()) 
			{
				throw new InterruptedException();
			}
			out.write(buf, 0, count);
		}
		out.close();
		in.close();

		File image = file_temp;
		BufferedImage in2 = ImageIO.read(image);
		return in2;
	}
}
