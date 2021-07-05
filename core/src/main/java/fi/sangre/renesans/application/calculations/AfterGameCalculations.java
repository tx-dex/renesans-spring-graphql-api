package fi.sangre.renesans.application.calculations;

public class AfterGameCalculations {
    private AfterGameCalculations() {}

    public static Long calculateEngagementRatio(Long participantsAnsweredCount, Long allParticipantsCount) {
        return Math.round((double) participantsAnsweredCount / (double) allParticipantsCount * 100.0);
    }
}
