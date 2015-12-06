# Instructions
These instructions assume you already have the knowledge needed to compile and run Java source files.

1. Convert your .mid file to text using [this free online tool](http://flashmusicgames.com/midi/mid2txt.php) with the "absolute" timestamp option
2. When saving to .txt, only copy the lines that are formatted as follows: `192 On ch=1 n=36 v=23` *(for optimal results, parse and compose different instruments separately)*
3. Edit the absolute file paths for the input/output files in the main function of Main.java
4. Set the composition's desired duration and speed progression by changing the parameters of the `composeScore` call
5. Compile and run the program
6. The score text file can then be used to make MIDI notes with your method of choice; an example of how to play the notes with [Pure Data](https://puredata.info/) is provided below: 

![](https://raw.githubusercontent.com/Bennett-Lynch/Markov/master/samples/pure-data-setup.png)

#### A finished example piece:
Made by using music composed by Hans Zimmer for the Inception and Interstellar soundtracks:

https://www.youtube.com/watch?v=7eUP1Sncifg

#### A visual representation of data parsed from a MIDI text file:
![](https://raw.githubusercontent.com/Bennett-Lynch/Markov/master/samples/visual-representation.png)
