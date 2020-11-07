package fi.sangre.renesans.service;

import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.QuestionInput;
import fi.sangre.renesans.graphql.input.SegmentInput;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.QuestionGroup;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.model.SegmentQuestionGroupPhrase;
import fi.sangre.renesans.repository.QuestionGroupRepository;
import fi.sangre.renesans.repository.SegmentQuestionGroupPhraseRepository;
import fi.sangre.renesans.repository.SegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@RequiredArgsConstructor

@Slf4j

@Service
public class SegmentService {
    private final SegmentRepository segmentRepository;
    private final QuestionService questionService;
    private final QuestionGroupRepository questionGroupRepository;
    private final SegmentQuestionGroupPhraseRepository segmentQuestionGroupPhraseRepo;
    private final MultilingualService multilingualService;

    @Transactional
    public Segment storeSegment(final String languageCode, final SegmentInput input) {
        checkArgument(input != null, "Input segment dto is required");

        final Segment segment;
        if (input.getId() == null) {
            segment = Segment.builder().build();
            segment.setSegmentQuestionGroupPhrases(saveDefaultSegmentQuestionGroups(segment));
        } else {
            segment = getSegmentById(input.getId());
        }

        if (input.getName() != null) {
            segment.setName(input.getName());
        }

        if (input.getQuestions() != null) {
            List<Question> questions = new ArrayList<>();
            for (QuestionInput questionInput : input.getQuestions()) {
                questions.add(questionService.storeSegmentQuestion(languageCode, segment, questionInput));
            }
            segment.setQuestions(questions);
        }

        return segmentRepository.save(segment);
    }

    public List<Segment> getAllSegments() {
        return segmentRepository.findAll();
    }

    public Segment getSegmentById(final Long segmentId) {
        return segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid segment id", segmentId));
    }

    private List<SegmentQuestionGroupPhrase> saveDefaultSegmentQuestionGroups(Segment segment) {
        List<QuestionGroup> defaultQuestionGroups = questionGroupRepository.findAll();
        List<SegmentQuestionGroupPhrase> segmentQuestionGroups = new ArrayList<>();
        for (QuestionGroup defaultQuestionGroup : defaultQuestionGroups) {
            segmentQuestionGroups.add(SegmentQuestionGroupPhrase.builder()
                    .segment(segment)
                    .questionGroup(defaultQuestionGroup)
                    .title(multilingualService.copyKeyWithPhrases(defaultQuestionGroup.getTitle()))
                    .build());
        }

        return segmentQuestionGroupPhraseRepo.saveAll(segmentQuestionGroups);
    }
}
