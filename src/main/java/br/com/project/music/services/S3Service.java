package br.com.project.music.services; // Verifique se este pacote está correto

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection; // Importe esta classe
import java.net.URL;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final AmazonS3 s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.profile-images-dir}")
    private String profilePicDir;

    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(String fileName, InputStream inputStream, String contentType, long contentLength) {
        try {
            String key = profilePicDir + fileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(contentLength);

            s3Client.putObject(new PutObjectRequest(bucketName, key, inputStream, metadata));
            logger.info("Arquivo carregado com sucesso para o S3: {}/{}", bucketName, key);

            return s3Client.getUrl(bucketName, key).toString();

        } catch (Exception e) {
            logger.error("Falha ao carregar arquivo para o S3: {}", fileName, e);
            return null;
        }
    }

    public String uploadImageFromUrl(String imageUrl, String s3FileName) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            String contentType = connection.getContentType();
            long contentLength = connection.getContentLengthLong();

            if (contentType == null || (!contentType.startsWith("image/"))) {
                logger.warn("Tipo de conteúdo não é imagem ou não pôde ser determinado para URL: {}. Tentando inferir.", imageUrl);
                String fileExtension = getFileExtension(imageUrl);
                if (fileExtension != null) {
                    contentType = "image/" + fileExtension;
                } else {
                    contentType = "application/octet-stream";
                }
            }
            if (contentLength == -1) {
                logger.warn("Content-Length desconhecido para URL: {}. O arquivo será bufferizado em memória.", imageUrl);
            }

            try (InputStream is = connection.getInputStream()) {
                return uploadFile(s3FileName, is, contentType, contentLength);
            } finally {
                connection.disconnect(); 
            }
        } catch (Exception e) {
            logger.error("Falha ao baixar imagem da URL {} ou carregar para o S3 para o arquivo {}", imageUrl, s3FileName, e);
            return null;
        }
    }

    private String getFileExtension(String url) {
        int dotIndex = url.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < url.length() - 1) {
            String extension = url.substring(dotIndex + 1);
            if (extension.matches("^(jpg|jpeg|png|gif|bmp|webp)$")) {
                return extension;
            }
        }
        return null;
    }
}