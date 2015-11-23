package main;

public class Util
{
    static float Clamp( float value, float min, float max )
    {
	if ( value < min )
	{
	    value = min;
	}

	if ( value > max )
	{
	    value = max;
	}

	return value;
    }

    static float Clamp01( float value )
    {
	return Clamp( value, 0.0f, 1.0f );
    }

    static int Clamp( int value, int min, int max )
    {
	if ( value < min )
	{
	    value = min;
	}

	if ( value > max )
	{
	    value = max;
	}

	return value;
    }

    static float Lerp( float from, float to, float percent )
    {
	percent = Clamp01( percent );

	float distance = to - from;

	return from + distance * percent;
    }
}
