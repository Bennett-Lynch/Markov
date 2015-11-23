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
    private static MIDIData[] midis = new MIDIData[128];

    /** The first MIDI note recorded when parsing the text file. */
    private static Integer firstNote = null;

    /** The last (or most recent) MIDI note recorded when parsing the text file. */
    private static Integer finalNote = null;

    public static void main( String[] args ) throws FileNotFoundException
    {
	/* At the start of the program, initialize 128 different MIDI notes. */
	for ( int i = 0; i < midis.length; ++i )
	{
	    midis[i] = new MIDIData( i );
	}

	/* Define custom behavior here. */
	parseMIDITextFile( "C:/Users/BBL/Documents/School/MUS306/PD/Time2/violin.txt" );
	composeScore( "C:/Users/BBL/Documents/Pure Data/violin-score.txt", 20000, 1f, 1f ); // used 150000 for piano
    }

    private static void parseMIDITextFile( String filePath ) throws FileNotFoundException
    {
	Scanner scanner = new Scanner( new File( filePath ) );

	/*
	 * Example of how text file must be formatted:
	 * 192 On ch=1 n=36 v=23
	 */
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

		    midis[note].logNoteOn( time, velocity );

		    finalNote = note;
		}
		else
		{
		    midis[note].logNoteOff( time );
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

	// printData();
    }

    /** Prints the note probability for each midi note (creating a 128x128 grid).
     * This should only be called AFTER parseMIDITextFile has been called.
     * Data can be more easily visualized by pasting into Excel and using "Data" > "Text to Columns" (with a Space delimiter). */
    private static void printData()
    {
	for ( MIDIData md : midis )
	{
	    System.out.println( md );
	}
    }

    private static void composeScore( String filePath, int durationMS, float startDelayModifier, float endDelayModifier ) throws FileNotFoundException
    {
	System.out.println( "Beginning composition..." );

	PrintWriter writer = new PrintWriter( filePath );

	int time = 0;
	int note = firstNote;

	// Loop while time is < than specified time OR we're not on final note and still under 2x the specified time
	while ( time < durationMS || ( note != finalNote && time < 2 * durationMS ) )
	{
	    note = midis[note].getNextRandomNote();
	    float percent = Util.Lerp( startDelayModifier, endDelayModifier, (float) time / durationMS );
	    int delay = Math.round( percent * midis[note].getAverageDelay() );

	    writer.printf( "%d %s %d;%n", time == 0 ? 0 : delay, "vel", midis[note].getAverageVelocity() );
	    writer.printf( "%d %s %d;%n", 0, "dur", midis[note].getAverageDuration() );
	    writer.printf( "%d %s %d;%n", 0, "note", note );

	    time += delay;
	}

	System.out.println( "Composition complete: " + filePath );

	writer.close();
    }

}
