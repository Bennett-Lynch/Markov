package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** This class has a main function and can be run to both parse a specified MIDI text file and then output a random composition to a different specified text file. */
public class Main
{
    /** Data-tracking objects for 128 different MIDI notes. */
    private static MIDIData[] midis = new MIDIData[128];

    /** The first MIDI note recorded when parsing the text file. */
    private static Integer firstNote = null;

    /** The last (or most recent) MIDI note recorded when parsing the text file. */
    private static Integer finalNote = null;

    public static void main( String[] args ) throws FileNotFoundException
    {
	/* At the start of the program, initialize the 128 different data-tracking objects. */
	for ( int i = 0; i < midis.length; ++i )
	{
	    midis[i] = new MIDIData( i );
	}

	/* Define custom behavior here. */
	parseMIDITextFile( "C:/Users/BBL/Documents/School/MUS306/PD/Time2/violin.txt" );
	composeScore( "C:/Users/BBL/Documents/Pure Data/violin-score.txt", 20000, 1f, 1f ); // used 150000 for piano
    }

    /** Parse a specially formatted MIDI text file to analyze the sequential note probability (as well as delay, velocity, etc.).
     * A correctly formatted file can be obtained by visiting http://flashmusicgames.com/midi/mid2txt.php and converting a .mid file with the "Absolute" timestamp option, and then only copying the lines that are formatted as follows:
     * 192 On ch=1 n=36 v=23
     *
     * @param filePath
     *            The absolute file path for the output file (should end in .txt).
     * @throws FileNotFoundException */
    private static void parseMIDITextFile( String filePath ) throws FileNotFoundException
    {
	Scanner scanner = new Scanner( new File( filePath ) );

	Pattern p = Pattern.compile( "(\\d+) (\\w+) ch=(\\d+) n=(\\d+) v=(\\d+)" );

	/* "time" must be in scope outside of the loop so that we can use it to terminate the final note. */
	int time = 0;

	do
	{
	    Matcher m = p.matcher( scanner.nextLine() );

	    if ( m.find() )
	    {
		time = Integer.parseInt( m.group( 1 ) );
		boolean enabling = m.group( 2 ).equals( "On" );
		// int channel = Integer.parseInt( m.group( 3 ) );
		int note = Integer.parseInt( m.group( 4 ) );
		int velocity = Integer.parseInt( m.group( 5 ) );

		if ( firstNote == null )
		{
		    firstNote = note;
		}

		if ( enabling )
		{
		    for ( MIDIData md : midis )
		    {
			md.recordNote( time, note );
		    }

		    midis[note].enableNote( time, velocity );

		    finalNote = note;
		}
		else
		{
		    midis[note].disableNote( time );
		}
	    }
	    else
	    {
		System.out.println( "Error: File not formatted correctly." );
	    }

	} while ( scanner.hasNextLine() );

	scanner.close();

	/*
	 * File parsing is complete, but we need to log the "sequential note" data for the last played note.
	 * We'll do this by simply giving it the first played note (increasing the likelihood that it will wrap back around to the start).
	 */
	midis[finalNote].recordNote( time, firstNote );

	printData();
    }

    /** Prints the note probability for each MIDI note (creating a 128x128 grid).
     * This should only be called AFTER parseMIDITextFile has been called.
     * Data can be more easily visualized by pasting into Excel and using "Data" > "Text to Columns" (with a Space delimiter). */
    private static void printData()
    {
	for ( MIDIData md : midis )
	{
	    System.out.println( md );
	}
    }

    /** Use the parsed MIDI data to randomly compose data for a new MIDI text file.
     *
     * @param filePath
     *            The absolute file path for the output file (should end in .txt).
     * @param duration
     *            The desired duration of the composition (in milliseconds). Actual duration may be up to twice as long as algorithm will attempt to end on the input file's final note.
     * @param startSpeed
     *            The speed of the composition at the start. "2" will result in the start of the piece sounding twice as fast as normal.
     * @param endSpeed
     *            The speed of the piece slowly approaches this amount over its entire duration. E.g., startSpeed=1 and endSpeed=2 will result in the piece starting at a normal speed and slowly increasing until it is doubled at the end.
     * @throws FileNotFoundException */
    private static void composeScore( String filePath, int duration, float startSpeed, float endSpeed ) throws FileNotFoundException
    {
	System.out.println( "Beginning composition..." );

	PrintWriter writer = new PrintWriter( filePath );

	int time = 0;
	int note = firstNote;

	/* Loop while time is < than specified time OR we're not on final note and still under 2x the specified time */
	while ( time < duration || ( note != finalNote && time < 2 * duration ) )
	{
	    note = midis[note].getRandomSequentialNote();

	    float speed = Util.Lerp( startSpeed, endSpeed, (float) time / duration );
	    int delay = Math.round( midis[note].getAverageDelay() / speed );

	    writer.printf( "%d %s %d;%n", time == 0 ? 0 : delay, "vel", midis[note].getAverageVelocity() );
	    writer.printf( "%d %s %d;%n", 0, "dur", midis[note].getAverageDuration() );
	    writer.printf( "%d %s %d;%n", 0, "note", note );

	    time += delay;
	}

	System.out.println( "Composition complete: " + filePath );

	writer.close();
    }
}
