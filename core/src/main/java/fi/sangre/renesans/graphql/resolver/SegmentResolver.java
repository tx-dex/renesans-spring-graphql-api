package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.assemble.OrganizationOutputAssembler;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static fi.sangre.renesans.graphql.output.CatalystProxy.toProxies;

@RequiredArgsConstructor

@Component
public class SegmentResolver implements GraphQLResolver<Segment> {
    private final QuestionService questionService;
    private final OrganizationService organizationService;
    private final OrganizationOutputAssembler organizationOutputAssembler;

    @Deprecated
    public List<Question> getQuestions(final Segment segment) {
        return questionService.getOnlySegmentQuestions(segment);
    }

    public List<CatalystProxy> getCatalysts(final Segment segment) {
        return toProxies(questionService.getCatalysts(segment));
    }

    @Deprecated
    public List<Customer> getCustomers(final Segment segment) {
        return organizationService.findAllBySegment(segment, e -> e);
    }

    @Deprecated
    public Long getCustomerCount(final Segment segment) {
        return organizationService.countBySegment(segment);
    }

    @NonNull
    public Collection<OrganizationOutput> getOrganizations(@NonNull final Segment segment) {
        return organizationOutputAssembler.from(organizationService.findAllBySegment(segment));
    }

    @NonNull
    public Long getOrganizationCount(@NonNull final Segment segment) {
        return organizationService.countBySegment(segment);
    }

}
