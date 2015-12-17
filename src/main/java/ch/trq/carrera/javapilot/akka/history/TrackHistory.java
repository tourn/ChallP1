package ch.trq.carrera.javapilot.akka.history;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tourn on 17.12.15.
 */
public class TrackHistory {

    final private List<List<HistoryEntry>> history = new ArrayList<>();
    final private int sectionCount;
    private int currentRound;

    public TrackHistory(final Track initialTrack){
        sectionCount = initialTrack.getSections().size();
        history.add(0, new ArrayList<>(sectionCount));
        history.add(1, new ArrayList<>(sectionCount));

        final int initialPower = initialTrack.getPower();
        for (TrackSection trackSection : initialTrack.getSections()) {
            history.get(0).add(createInitialEntry(trackSection, initialPower));
            history.get(1).add(createInitialEntry(trackSection, initialPower));
        }

        currentRound = 1;
    }

    public void addEntry(HistoryEntry entry){
        final int sectionId = entry.getSection().getId();
        if(sectionId == 0){
            currentRound += 1;
            history.add(currentRound, new ArrayList<>(sectionCount));
        }
        history.get(currentRound).add(sectionId, entry);
    }

    public HistoryEntry getPreviousEntry(final int sectionId){
        return history.get(currentRound).get(sectionId);
    }

    private HistoryEntry createInitialEntry(TrackSection section, int initialPower){
        HistoryEntry entry = new HistoryEntry();
        entry.setSection(section);
        entry.setBrakePercentage(calculateBreakPercentage(section));
        entry.setDuration(section.getDuration());
        entry.setPower(initialPower);
        entry.setPenaltyOcurred(false);
        entry.setRecoveringFromPenalty(false);
        entry.setPowerIncreaseFrozen(false);
        return entry;
    }

    //FIXME is this supposed to be in a _HISTORY_ ?
    private double calculateBreakPercentage(TrackSection section) {
        return 0.7;
    }


}
