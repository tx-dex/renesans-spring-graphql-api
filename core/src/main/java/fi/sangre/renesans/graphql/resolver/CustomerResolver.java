package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import fi.sangre.renesans.service.CustomerService;
import fi.sangre.renesans.service.QuestionService;
import fi.sangre.renesans.service.RespondentGroupService;
import fi.sangre.renesans.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class CustomerResolver implements GraphQLResolver<Customer> {

    private final RespondentGroupService respondentGroupService;
    private final UserService userService;
    private final RespondentRepository respondentRepository;
    private final RespondentGroupRepository respondentGroupRepository;
    private final CustomerService customerService;
    private final QuestionService questionService;

    @Autowired
    public CustomerResolver(
            RespondentGroupService respondentGroupService,
            UserService userService,
            RespondentRepository respondentRepository,
            RespondentGroupRepository respondentGroupRepository,
            CustomerService customerService,
            QuestionService questionService
    ) {
        this.respondentGroupService = respondentGroupService;
        this.userService = userService;
        this.respondentRepository = respondentRepository;
        this.respondentGroupRepository = respondentGroupRepository;
        this.customerService = customerService;
        this.questionService =questionService;
    }

    public List<RespondentGroup> getRespondentGroups(Customer customer) {
        return respondentGroupService.getRespondentGroups(customer);
    }

    public User getCreatedBy(Customer customer) {
        return userService.findById(customer.getCreatedBy());
    }

    public Long getRespondentCount(Customer customer) {
        return respondentRepository.countByRespondentGroup_Customer(customer);
    }

    public Long getRespondentGroupCount(Customer customer) {
        return respondentGroupRepository.countByCustomer(customer);
    }

    public Segment getSegment(Customer customer) {
        return customerService.getCustomerSegment(customer);
    }

    @Deprecated
    public List<Question> getQuestions(Customer customer) {
        return questionService.getOnlyCustomerQuestions(customer);
    }

    public List<CatalystDto> getCatalysts(final Customer customer) {

        return questionService.getCatalysts(customer);
    }
}
