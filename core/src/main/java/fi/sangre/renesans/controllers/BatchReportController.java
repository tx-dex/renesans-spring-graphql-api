package fi.sangre.renesans.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.dto.BatchReportParametersDto;
import fi.sangre.renesans.dto.FiltersBaseDto;
import fi.sangre.renesans.exception.InternalServerErrorException;
import fi.sangre.renesans.handlers.ZipStreamSynchronousHandler;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import fi.sangre.renesans.service.AthenaPdfService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Slf4j

@RestController
public class BatchReportController {
    private final AthenaPdfService athenaPdfService;
    private final ExecutorService executorService;
    private final RespondentGroupRepository respondentGroupRepository;
    private final RespondentRepository respondentRepository;
    private final ObjectMapper mapper;

    @Value("${fi.sangre.report.generator.uri}")
    private String generatorUri;

    @Autowired
    public BatchReportController(
            final AthenaPdfService athenaPdfService,
            final ExecutorService zipExecutorService,
            final RespondentGroupRepository respondentGroupRepository,
            final RespondentRepository respondentRepository
    ) {
        this.athenaPdfService = athenaPdfService;
        this.executorService = zipExecutorService;
        this.respondentGroupRepository = respondentGroupRepository;
        this.respondentRepository = respondentRepository;
        this.mapper = new ObjectMapper()
                .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)
                .enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_LONG_FOR_INTS);
    }

    @RequestMapping(
            path = "/survey/report/batch",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/octet-stream")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> handleRequest (
            @RequestBody final BatchReportParametersDto reportParameters,
            @RequestHeader("Authorization") final String authorization
    ) {
        final String groupName = respondentGroupRepository.findById(reportParameters.getRespondentGroupId())
                .orElseThrow(InternalServerErrorException::new).getTitle();

        final Set<String> respondents = ImmutableSet.copyOf(reportParameters.getRespondentIds());
        final String token = authorization.replace("Bearer ", "");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", String.format("%1$2s %2$tF %2$tH-%2$tM-%2$tS.zip", groupName, LocalDateTime.now()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream -> {
                    final Map<String, CompletableFuture<InputStream>> athenaTasks = new ConcurrentHashMap<>();
                    final List<CompletableFuture> zipTasks = Collections.synchronizedList(new ArrayList<>());

                    try(final ZipStreamSynchronousHandler handler = new ZipStreamSynchronousHandler(this.executorService, outputStream)) {

                        for (final String respondentId : respondents) {
                            final Respondent respondent = respondentRepository.findById(respondentId)
                                    .orElseThrow(InternalServerErrorException::new);

                            final String entryName = buildEntryName(respondent);
                            final URL reportUrl = buildReportUrlForRespondent(reportParameters, respondentId, token);

                            athenaTasks.put(respondentId, athenaPdfService.executeAsync(reportUrl , stream -> {
                                if (stream != null) {
                                    zipTasks.add(handler.writeEntryAsync(entryName, stream));
                                    log.debug("New zip task added");
                                } else {
                                    log.warn("Input stream was null, should never reach this place...");
                                }
                                return stream;
                            }));
                        }

                        CompletableFuture.allOf(athenaTasks.values().toArray(new CompletableFuture[0])).join();
                        CompletableFuture.allOf(zipTasks.toArray(new CompletableFuture[0])).join();
                        log.debug("Finished");
                    } catch (final Exception ex) {
                        log.warn("Unexpected exception", ex);
                        athenaTasks.values().forEach(e -> e.cancel(true));
                        zipTasks.forEach(e -> e.cancel(true));
                        throw new InternalServerErrorException();
                    } finally {
                        athenaTasks.clear();
                        zipTasks.clear();
                    }
                });
    }

    private String buildEntryName(final Respondent respondent) {
        return String.format("weCan-report - %s_%s.pdf", respondent.getName(), respondent.getId().substring(0, 4));
    }

    private URL buildReportUrlForRespondent(final BatchReportParametersDto reportParameters, final String respondentId, String token) throws JsonProcessingException {
        final HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(generatorUri)).newBuilder();

        final FiltersBaseDto filters = FiltersBaseDto.builder()
                .respondentIds(ImmutableList.of(respondentId))
                .build();

        urlBuilder.addQueryParameter("token", token)
                .addQueryParameter("filters", mapper.writeValueAsString(filters))
                .addQueryParameter("partnerDetails", mapper.writeValueAsString(reportParameters.getPartnerDetails()))
                .addQueryParameter("languageCode", reportParameters.getLanguageCode());

        return urlBuilder.build().url();
    }
}
