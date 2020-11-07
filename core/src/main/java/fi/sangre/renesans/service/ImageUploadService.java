package fi.sangre.renesans.service;

import com.google.common.hash.Hashing;
import fi.sangre.renesans.dto.ImageUploadUrlDto;
import graphql.GraphQLException;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class ImageUploadService {
    @Value("${fi.sangre.s3.endpoint}")
    private String endpoint;

    @Value("${fi.sangre.s3.access.key}")
    private String access;

    @Value("${fi.sangre.s3.secret.key}")
    private String secret;

    @Value("${fi.sangre.s3.bucket}")
    private String bucket;

    @Value("${fi.sangre.s3.secure}")
    private Boolean secure;

    public ImageUploadUrlDto getUploadUrl(String fileName) {
        try {
            log.info("Initializing MinioClient at endpoint {}, secure: {}", endpoint, secure);
            MinioClient minioClient = new MinioClient(endpoint, access, secret, secure);

            String hash = Hashing.sha256().hashString(fileName, StandardCharsets.UTF_8).toString();
            String s3filename = hash + "_" + fileName;
            String presignedPutObject = minioClient.presignedPutObject(bucket, s3filename, 60);

            return ImageUploadUrlDto.builder()
                    .hash(s3filename)
                    .url(presignedPutObject)
                    .build();

        } catch (MinioException | NoSuchAlgorithmException | InvalidKeyException | IOException | XmlPullParserException e) {
            e.printStackTrace();
            throw new GraphQLException(e);
        }
    }
}
