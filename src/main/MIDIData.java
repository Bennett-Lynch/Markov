package main;
import java.text.DecimalFormat;
import java.util.Random;

public class MIDIData
{
    public int noteNumber;

    /** The average duration for this MIDI note. */
    private double averageDuration;
    /** The total number of durations recorded thus far (needed to calculate average). */
    private int numDurationRecorded;

    /** The average velocity for this MIDI note. */
    private double averageVelocity;
    /** The total number of velocities recorded thus far (needed to calculate average). */
    private int numVelocityRecorded;

    /** A private record of when this note was last turned on. */
    private int onTimestamp;

    /** Enabled after a note is logged as on, so that this MIDI note will listen for its next sequential note. */
    private boolean waitingSequentialNote;

    /** The average delay (time until another MIDI note is played) for this MIDI note. */
    private double averageDelay;
    private int numDelayRecorded;

    /** The percent chance for all 128 different notes to follow this one. */
    private double[] otherNoteProbabilities = new double[128];
    /** The total number of notes recorded thus far (needed to calculate percent chances). */
    private int totalOtherNoteCounts;

    private Random rng = new Random();

    public MIDIData( int noteNumber )
    {
	this.noteNumber = noteNumber;
    }

    public void logNoteOn( int time, int velocity )
    {
	onTimestamp = time;

	averageVelocity = ( averageVelocity * numVelocityRecorded + velocity ) / ++numVelocityRecorded;

	waitingSequentialNote = true;
    }

    /** This is called on every MIDI entry whenever a new MIDI note is played */
    public void recordNote( int time, int currentNote )
    {
	if ( waitingSequentialNote )
	{
	    int delay = time - onTimestamp;

	    averageDelay = ( averageDelay * numDelayRecorded + delay ) / ++numDelayRecorded;

	    for ( int i = 0; i < otherNoteProbabilities.length; ++i )
	    {
		otherNoteProbabilities[i] = ( otherNoteProbabilities[i] * totalOtherNoteCounts + ( currentNote == i ? 1 : 0 ) ) / ( totalOtherNoteCounts + 1 );
	    }

	    totalOtherNoteCounts++;

	    waitingSequentialNote = false;
	}
    }

    public void logNoteOff( int time )
    {
	int duration = time - onTimestamp;

	averageDuration = ( averageDuration * numDurationRecorded + duration ) / ++numDurationRecorded;
    }

    /** Generate a new random note to follow this note. */
    public int getNextRandomNote()
    {
	double random = rng.nextDouble();

	for ( int i = 0; i < otherNoteProbabilities.length - 1; ++i )
	{
	    if ( otherNoteProbabilities[i] > random )
	    {
		return i;
	    }
	    else
	    {
		random -= otherNoteProbabilities[i];
	    }
	}

	return otherNoteProbabilities.length - 1;
    }

    public int getAverageDelay()
    {
	return (int) Math.round( averageDelay );
    }

    public int getAverageDuration()
    {
	return (int) Math.round( averageDuration );
    }

    public int getAverageVelocity()
    {
	return (int) Math.round( averageVelocity );
    }

    @Override
    public String toString()
    {
	String temp = "";

	for ( double prob : otherNoteProbabilities )
	{
	    DecimalFormat df = new DecimalFormat( "#.##" );
	    double percent = Double.valueOf( df.format( prob * 100 ) );

	    temp += percent + "% ";
	}

	return temp;
    }

}
