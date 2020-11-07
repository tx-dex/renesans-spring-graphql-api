package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.model.SegmentQuestionGroupPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentQuestionGroupPhraseRepository extends JpaRepository<SegmentQuestionGroupPhrase, Long> {
    List<SegmentQuestionGroupPhrase> findBySegment(Segment segment);
}