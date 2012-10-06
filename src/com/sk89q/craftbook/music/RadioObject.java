package com.sk89q.craftbook.music;

public class RadioObject
{
	public final int X;
	public final int Y;
	public final int Z;
	
	public final int SIGN_X;
	public final int SIGN_Y;
	public final int SIGN_Z;
	
	public boolean sendMessages;
	public boolean displayNotes;
	
	public RadioObject(int x, int y, int z, int signX, int signY, int signZ, boolean messages, boolean displayNotes)
	{
		X = x;
		Y = y;
		Z = z;
		
		SIGN_X = signX;
		SIGN_Y = signY;
		SIGN_Z = signZ;
		
		sendMessages = messages;
		this.displayNotes = displayNotes;
	}
}
