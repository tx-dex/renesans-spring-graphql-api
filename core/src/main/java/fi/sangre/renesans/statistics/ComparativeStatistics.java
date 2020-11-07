package fi.sangre.renesans.statistics;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ComparativeStatistics {

    @NonNull private List<Statistics> customers;
    @NonNull private List<Statistics> respondentGroups;
    @NonNull private List<Statistics> respondents;
}