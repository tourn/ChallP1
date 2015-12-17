package ch.trq.carrera.javapilot.akka.history;

import ch.trq.carrera.javapilot.akka.trackanalyzer.Track;
import ch.trq.carrera.javapilot.akka.trackanalyzer.TrackSection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by tourn on 17.12.15.
 */
public class TrackHistory {

    final private List<StrategyParameters[]> history = new ArrayList<>();
    final private int sectionCount;
    private int currentRound;

    public TrackHistory(final Track initialTrack){
        sectionCount = initialTrack.getSections().size();
        history.add(0, new StrategyParameters[sectionCount]);
        history.add(1, new StrategyParameters[sectionCount]);

        final int initialPower = initialTrack.getPower();
        for(int i = 0; i < initialTrack.getSections().size(); i++){
            final TrackSection trackSection = initialTrack.getSections().get(i);
            history.get(0)[i] = createInitialEntry(trackSection, initialPower);
            history.get(1)[i] = createInitialEntry(trackSection, initialPower);
        }

        currentRound = 1;
    }

    public void addEntry(StrategyParameters entry){
        try {
            final int sectionId = entry.getSection().getId();
            if (sectionId == 0) {
                currentRound += 1;
                history.add(currentRound, new StrategyParameters[sectionCount]);
            }
            history.get(currentRound)[sectionId] = entry;
        } catch (Exception e){
            int i = 1;
        }
    }

    public StrategyParameters getValidHistory(final int sectionId){
        return getParams(currentRound, sectionId);
    }

    private StrategyParameters getParams(final int round, final int sectionId) {
        StrategyParameters params = history.get(round)[sectionId];
        if(params != null && params.isValid()){
            return params;
        } else {
            return getParams(round-1, sectionId);
        }
    }

    private StrategyParameters createInitialEntry(TrackSection section, int initialPower){
        StrategyParameters entry = new StrategyParameters();
        entry.setSection(section);
        entry.setBrakePercentage(calculateBreakPercentage(section));
        entry.setDuration(section.getDuration());
        entry.setPower(initialPower);
        return entry;
    }

    //FIXME is this supposed to be in a _HISTORY_ ?
    private double calculateBreakPercentage(TrackSection section) {
        return 0.7;
    }


}
