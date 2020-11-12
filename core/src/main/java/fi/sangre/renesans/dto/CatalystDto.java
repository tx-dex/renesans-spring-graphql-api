package fi.sangre.renesans.dto;

import com.google.common.collect.Lists;
import fi.sangre.renesans.graphql.output.CatalystOutput;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CatalystDto implements CatalystOutput {
    private Long id;
    private Long titleId;
    private String pdfName;
    private Customer customer;
    private Segment segment;
    private Double weight;
    @Builder.Default
    private List<Question> questions = Lists.newArrayList();
}
