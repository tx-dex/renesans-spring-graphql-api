package fi.sangre.renesans.service;

import fi.sangre.renesans.exception.RespondentOptionNotFoundException;
import fi.sangre.renesans.model.RespondentOption;
import fi.sangre.renesans.repository.RespondentOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RespondentOptionService {
    @Autowired
    private RespondentOptionRepository respondentOptionRepository;

    public List<RespondentOption> getRespondentOptions(RespondentOption.OptionType optionType) {
        return respondentOptionRepository.findByOptionType(optionType);
    }

    public RespondentOption getRespondentOption(Long id) {
        return respondentOptionRepository.findById(id)
                .orElseThrow(() -> new RespondentOptionNotFoundException(id));
    }
}
