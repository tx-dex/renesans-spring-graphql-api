package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.model.Customer;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.service.CustomerService;
import fi.sangre.renesans.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
public class SegmentResolver implements GraphQLResolver<Segment> {
    private final QuestionService questionService;
    private final CustomerService customerService;

    @Deprecated
    public List<Question> getQuestions(final Segment segment) {
        return questionService.getOnlySegmentQuestions(segment);
    }

    public List<CatalystDto> getCatalysts(final Segment segment) {
        return questionService.getCatalysts(segment);
    }

    public List<Customer> getCustomers(final Segment segment) {
        return customerService.getCustomersAssignedToSegment(segment);
    }

    public Long getCustomerCount(final Segment segment) {
        return customerService.countCustomersForSegment(segment);
    }
}
