package awsdownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;

class Utils {
  public static void zip(Path source, Path destination) throws IOException {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destination.toFile()));
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
      public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) throws IOException {
        zos.putNextEntry(new ZipEntry(source.relativize(file).toString()));
        Files.copy(file, zos);
        zos.closeEntry();
        return FileVisitResult.CONTINUE;
      }
    });
    zos.flush();
    zos.close();
  }
}

@RestController
class FileDownload {
  @RequestMapping("/downloadFile")
  ResponseEntity<Resource> downloadFile() throws IOException  {
    String folderName = "sample"; // S3 CSV folder name
    String folderNameZipped = "sample.zip";
    File outputFolder = new File("./");

    TransferManager transferManager = AWSUtils.getTransferManager();
    MultipleFileDownload d = transferManager.downloadDirectory(
      "bucketName", // S3 bucket name
      folderName, outputFolder);
    try {
      d.waitForCompletion();
      Utils.zip(Paths.get(folderName), Paths.get(folderNameZipped));
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }

    File zippedItem = new File("./sample.zip");

    Path path = Paths.get(zippedItem.getAbsolutePath());
    ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + folderNameZipped);

    return ResponseEntity.ok()
      .headers(headers)
      .contentLength(zippedItem.length())
      .contentType(MediaType.parseMediaType("application/octet-stream"))
      .body(resource);
  }
}

@SpringBootApplication
public class Main {
  public static void main(final String[] args) {
    SpringApplication.run(Main.class, args);
  }
}