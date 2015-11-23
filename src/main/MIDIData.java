package main;

import java.text.DecimalFormat;
import java.util.Random;

public class MIDIData
{
    /** All associated data is for this particular MIDI note number. */
    public int noteNumber;

    /** The average duration for this MIDI note. */
    private double averageDuration;
    /** The total number of durations recorded thus far (needed to calculate average). */
    private int numDurationRecorded;

    /** The average velocity for this MIDI note. */
    private double averageVelocity;
    /** The total number of velocities recorded thus far (needed to calculate average). */
    private int numVelocityRecorded;

    /** When this note was last turned on. */
    private int onTimestamp;

    /** Enabled after a note is logged as on, so that this MIDI note will listen for its next sequential note. */
    private boolean waitingSequentialNote;

    /** The average delay (time until another MIDI note is played) for this MIDI note. */
    private double averageDelay;
    /** The total number of delays recorded thus far (needed to calculate average). */
    private int numDelayRecorded;

    /** The percent chance for all 128 different notes to follow this one. */
    private double[] otherNoteProbabilities = new double[128];
    /** The total number of notes recorded thus far (needed to calculate percent chances). */
    private int numNotesRecorded;

    /** Java's random number generator. */
    private Random rng = new Random();

    public MIDIData( int noteNumber )
    {
	this.noteNumber = noteNumber;
    }

    /** Turn this note on and instruct it to await a following note to record as well. */
    public void enableNote( int time, int velocity )
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
		otherNoteProbabilities[i] = ( otherNoteProbabilities[i] * numNotesRecorded + ( currentNote == i ? 1 : 0 ) ) / ( numNotesRecorded + 1 );
	    }

	    numNotesRecorded++;

	    waitingSequentialNote = false;
	}
    }

    /** Turn this note off. */
    public void disableNote( int time )
    {
	int duration = time - onTimestamp;

	averageDuration = ( averageDuration * numDurationRecorded + duration ) / ++numDurationRecorded;
    }

    /** Generate a new random note to follow this note.
     * Should only be called after all parsing is complete. */
    public int getRandomSequentialNote()
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
