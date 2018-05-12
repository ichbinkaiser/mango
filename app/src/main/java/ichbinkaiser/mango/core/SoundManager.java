package ichbinkaiser.mango.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseIntArray;

import ichbinkaiser.mango.R;
import ichbinkaiser.mango.entity.SoundType;
import lombok.Getter;

public class SoundManager
{
	@Getter
	private static SoundManager instance = new SoundManager();

	private SoundPool soundpool;
	private SparseIntArray sounds;
	private AudioManager  audiomanager;
	private Context context;

	private int soundsLoaded = 0;
	private int[] soundLibrary = {R.raw.pop, R.raw.lifeup, R.raw.ding, R.raw.popwall, R.raw.down, R.raw.hit, R.raw.restart, R.raw.spawn};

	private SoundManager() {
		// This is not to be instantiated externally
	}

	public void initSounds(Context context) {
		this.context = context;
		soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sounds = new SparseIntArray();
		audiomanager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void loadSounds() // load sounds to IntArray
	{
		soundsLoaded = 0;
		soundpool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            soundsLoaded++;
            Log.i("SoundManager", "Sample" + Integer.toString(sampleId) + " loaded");
        });

		for (int soundindex = 0; soundindex < soundLibrary.length; soundindex++) {
			sounds.put(soundindex + 1, soundpool.load(context, soundLibrary[soundindex], 1));
		}
	}

	public void playSound(SoundType sound) {
        float streamVolume = audiomanager.getStreamVolume(AudioManager.STREAM_MUSIC);
		streamVolume = streamVolume / audiomanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		soundpool.play(sounds.get(sound.ordinal()), streamVolume, streamVolume, 1, 0, 1f);
	}

	public void doCleanup() {
		soundpool.release();
	}

	public int getSoundsLoaded() {
		return soundsLoaded;
	}

	public int[] getSoundLibrary() {
		return soundLibrary;
	}
}