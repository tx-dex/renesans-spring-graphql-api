package fi.sangre.renesans.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.sangre.renesans.dto.ComparativeReportParametersDto;
import fi.sangre.renesans.exception.InternalServerErrorException;
import fi.sangre.renesans.service.AthenaPdfService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

@Slf4j

@RestController
public class ComparativeController {
    private final AthenaPdfService athenaPdfService;
    private final ObjectMapper mapper;

    @Value("${fi.sangre.comparativeReport.generator.uri}")
    private String generatorUri;

    @Value("${fi.sangre.comparativeReport.filename}")
    private String defaultFilename;

    @Autowired
    public ComparativeController(final AthenaPdfService athenaPdfService) {
        this.athenaPdfService = athenaPdfService;
        this.mapper = new ObjectMapper()
                .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
                .enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_LONG_FOR_INTS);
    }

    @RequestMapping(
            path = "/survey/report/comparative",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/octet-stream"
    )
    public ResponseEntity<InputStreamResource> generate(
            @RequestBody ComparativeReportParametersDto reportParameters,
            @RequestHeader("Authorization") String authorization
    ) {
        try {
            final URL url = buildReportUrl(reportParameters, authorization);

            final InputStream inputStream = athenaPdfService.execute(url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", defaultFilename);

            InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

            return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
        } catch (final Exception e) {
            log.warn("Cannot generate report: ", e);
            throw new InternalServerErrorException();
        }
    }

    private URL buildReportUrl(ComparativeReportParametersDto reportParameters, String authorization) throws JsonProcessingException {
        String token = authorization.replace("Bearer ", "");

        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(generatorUri));

        HttpUrl.Builder urlBuilder = url.newBuilder()
                .addQueryParameter("token", token)
                .addQueryParameter("filters", mapper.writeValueAsString(reportParameters.getFilters()))
                .addQueryParameter("partnerDetails", mapper.writeValueAsString(reportParameters.getPartnerDetails()))
                .addQueryParameter("languageCode", reportParameters.getLanguageCode())
                .addQueryParameter("customerIds", mapper.writeValueAsString(reportParameters.getCustomerIds()))
                .addQueryParameter("respondentGroupIds", mapper.writeValueAsString(reportParameters.getRespondentGroupIds()))
                .addQueryParameter("respondentIds", mapper.writeValueAsString(reportParameters.getRespondentIds()));

        if (reportParameters.getEdit()) {
            urlBuilder.addQueryParameter("edit", "1");
        }

        return urlBuilder.build().url();
    }
}
