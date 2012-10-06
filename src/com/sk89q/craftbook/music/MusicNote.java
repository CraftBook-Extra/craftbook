package com.sk89q.craftbook.music;

public class MusicNote
{
	public static final String HARP = "harp";
	public static final String BASS_DRUM = "bd";
	public static final String SNARE = "snare";
	public static final String HAT = "hat";
	public static final String BASS_ATTACK = "bassattack";
	
	public final int type;
	public final float pitch;
	public final int pitchIndex;
	public final float volume;
	
	public MusicNote(int type, float pitch, int pitchIndex, float volume)
	{
		this.type = type;
		this.pitch = pitch;
		this.pitchIndex = pitchIndex;
		this.volume = volume;
	}
	
	public MusicNote(int type, byte pitch, float volume)
	{
		this(type, (float)Math.pow(2.0D, (double)(pitch - 12) / 12.0D), pitch, volume);
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getTypeName()
	{
		return MusicNote.getTypeName(type);
	}
	
	public float getPitch()
	{
		return pitch;
	}
	
	public float getPitchIndex()
	{
		return pitchIndex;
	}
	
	public static String getTypeName(int type)
	{
		switch(type)
		{
			case 1:
				return BASS_DRUM;
			case 2:
				return SNARE;
			case 3:
				return HAT;
			case 4:
				return BASS_ATTACK;
			default:
				return HARP;
		}
	}
}
