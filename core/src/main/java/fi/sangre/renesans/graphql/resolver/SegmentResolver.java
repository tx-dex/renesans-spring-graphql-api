package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.service.CustomerService;
import fi.sangre.renesans.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static fi.sangre.renesans.graphql.output.CatalystProxy.toProxies;

@RequiredArgsConstructor

@Component
public class SegmentResolver implements GraphQLResolver<Segment> {
    private final QuestionService questionService;
    private final CustomerService customerService;

    @Deprecated
    public List<Question> getQuestions(final Segment segment) {
        return questionService.getOnlySegmentQuestions(segment);
    }

    public List<CatalystProxy> getCatalysts(final Segment segment) {
        return toProxies(questionService.getCatalysts(segment));
    }

    public List<Customer> getCustomers(final Segment segment) {
        return customerService.getCustomersAssignedToSegment(segment);
    }

    public Long getCustomerCount(final Segment segment) {
        return customerService.countCustomersForSegment(segment);
    }
}
