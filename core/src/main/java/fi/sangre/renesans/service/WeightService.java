package fi.sangre.renesans.service;

import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.aaa.UserPrincipalService;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.WeightInput;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Weight;
import fi.sangre.renesans.repository.QuestionRepository;
import fi.sangre.renesans.repository.WeightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
//TODO: weights are question related, so we're handling it in the question service
@Service
public class WeightService {
    private final WeightRepository weightRepository;
    private final QuestionRepository questionRepository;
    private final UserPrincipalService userPrincipalService;

    @Transactional
    public List<Weight> storeWeights(final List<WeightInput> weights) {

        final List<Weight> weightsToSave = new ArrayList<>();
        weights.forEach(e -> {
            final Weight weight = weightRepository.findByQuestionIdAndQuestionGroupId(
                        e.getQuestionId(), e.getQuestionGroupId())
                    .orElse(Weight.builder()
                    .question(questionRepository.findById(e.getQuestionId()).orElseThrow(() -> new ResourceNotFoundException("InvalidQuestionId", e.getQuestionId())))
                    .questionGroupId(e.getQuestionGroupId())
                    .questionId(e.getQuestionId())
                    .build());

            final UserPrincipal user = userPrincipalService.getLoggedInPrincipal();
            if (!userPrincipalService.isSuperUser(user) && weight.getQuestion().getSourceType() != Question.SourceType.ORGANISATION) {
                throw new RuntimeException("User not allowed to edit weights for this type of question");
            }

            weight.setWeight(e.getWeight());

            weightsToSave.add(weight);
        });

        return weightRepository.saveAll(weightsToSave);
    }
}
