package lk.lakmalz.audiolist;

/**
 * Created by A Lakmal Weerasekara (Lakmalz) on 1/9/17.
 * alrweerasekara@gmail.com
 */

public class AudioFile {

    public final static int STOP = 0;
    public final static int PLAYING = 1;

    public int id;
    public String name;
    public String fileName;
    public int progress;
    public boolean isSelected;
    public boolean isPlaying;
}
