package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.persistence.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
@Transactional
@Deprecated
public class CustomerResolver implements GraphQLResolver<Customer> {

    public List<RespondentGroup> getRespondentGroups(Customer customer) {
        return ImmutableList.of();
    }

    public User getCreatedBy(Customer customer) {
        return null;
    }

    public Long getRespondentCount(Customer customer) {
        return 0L;
    }

    public Long getRespondentGroupCount(Customer customer) {
        return 0L;
    }

    public Segment getSegment(Customer customer) {
        return null;
    }

    public List<Question> getQuestions(Customer customer) {
        return ImmutableList.of();
    }

    public List<CatalystProxy> getCatalysts(final Customer customer) {
        return ImmutableList.of();
    }
}
