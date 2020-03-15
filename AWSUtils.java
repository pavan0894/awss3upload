package awsdownload;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import io.github.cdimascio.dotenv.Dotenv;

class AWSUtils {
  private static TransferManager transferManager = null;

  private AWSUtils() {}

  public static TransferManager getTransferManager() {
    if (transferManager == null) {
      Dotenv dotenv = Dotenv.load();
      BasicAWSCredentials creds = new BasicAWSCredentials(
        dotenv.get("AWS_ACCESS_KEY"),
        dotenv.get("AWS_SECRET_KEY")
      );

      AmazonS3 s3Client = AmazonS3Client
        .builder()
        .standard()
        .withCredentials(new AWSStaticCredentialsProvider(creds))
        .withRegion(Regions.US_EAST_1)
        .build();

      transferManager = TransferManagerBuilder
        .standard()
        .withS3Client(s3Client)
        .build();
    }
    return transferManager;
  }
}