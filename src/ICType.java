// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.Signal;

/**
 * IC types.
 * 
 * @author Lymia
 */
public enum ICType {
    /**
     * Zero input, single output
     */
    ZISO("ZISO", true) {
        void think(CraftBookWorld cbworld, Vector pt, SignText signText, Sign sign, IC zisoIC, Object extra) {
        	
        	basicThink(cbworld, pt, null, signText, sign, zisoIC, null, ' ', new int[]{0, 1, 2}, new int[]{0, 1, 2}, 0, 1, extra);
        }
    },
    /**
     * Single input, single output
     */
    SISO("SISO") {
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, IC sisoIC, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
        	
        	basicThink(cbworld, pt, changedRedstoneInput, signText, sign, sisoIC, r, mode, orderIn, orderOut, 1, 1, extra);
        }
    },
    /**
     * Single input, triple output
     */
    SI3O("SI3O") {
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, IC si3oIC, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
            
        	basicThink(cbworld, pt, changedRedstoneInput, signText, sign, si3oIC, r, mode, orderIn, orderOut, 1, 3, extra);
        }
    },
    /**
     * Triple input, single output
     */
    _3ISO("3ISO") {
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, IC _3isoIC, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
            
        	basicThink(cbworld, pt, changedRedstoneInput, signText, sign, _3isoIC, r, mode, orderIn, orderOut, 3, 1, extra);
        }
    },
    /**
     * Triple input, triple output
     */
    _3I3O("3I3O") {
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, IC _3i3oIC, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
            
        	basicThink(cbworld, pt, changedRedstoneInput, signText, sign, _3i3oIC, r, mode, orderIn, orderOut, 3, 3, extra);
        }
    },
    /**
     * Variable input, variable output
     */
    VIVO("VIVO") {
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, IC vivoIC, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
        	
        	World world = CraftBook.getWorld(cbworld);
        	
            Vector backVec = Util.getWallSignBack(world, pt, 1);
            if (backVec == null) return;
            Vector backShift = backVec.subtract(pt);

            Vector out0 = Util.getWallSignBack(world, pt, 2);
            if (out0 == null) return;
            Vector out1 = Util.getWallSignSide(world, pt, 1).add(backShift);
            if (out1 == null) return;
            Vector out2 = Util.getWallSignSide(world, pt, -1).add(backShift);
            if (out2 == null) return;

            Vector in0 = Util.getWallSignBack(world, pt, -1);
            if (in0 == null) return;
            Vector in1 = Util.getWallSignSide(world, pt, 1);
            if (in1 == null) return;
            Vector in2 = Util.getWallSignSide(world, pt, -1);
            if (in2 == null) return;
            
            boolean hasOut1 = CraftBook.getBlockID(world, out1) == BlockType.LEVER;
            boolean hasOut2 = CraftBook.getBlockID(world, out2) == BlockType.LEVER;

            Signal[] in = new Signal[3];
            Signal[] out = new Signal[3];

            out[0] = new Signal(Redstone.getOutput(cbworld, out0));
            in[0] = new Signal(Redstone.isHighBinary(cbworld, in0, true),
                    changedRedstoneInput.equals(in0));

            if (hasOut1) {
                out[1] = new Signal(Redstone.getOutput(cbworld, out1));
                in[1] = new Signal(false);
            } else {
                out[1] = new Signal(false);
                in[1] = new Signal(Redstone.isHighBinary(cbworld, in1, true),
                        changedRedstoneInput.equals(in1));
            }

            if (hasOut2) {
                out[2] = new Signal(Redstone.getOutput(cbworld, out2));
                in[2] = new Signal(false);
            } else {
                out[2] = new Signal(false);
                in[2] = new Signal(Redstone.isHighBinary(cbworld, in2, true),
                        changedRedstoneInput.equals(in2));
            }

            ChipState chip = new ChipState(cbworld, pt, backVec.toBlockVector(), in, out, signText, world.getTime());

            // The most important part...
            vivoIC.think(chip);

            if (chip.isModified()) {
                Redstone.setOutput(cbworld, out0, chip.getOut(1).is());
                if (hasOut1)
                    Redstone.setOutput(cbworld, out1, chip.getOut(2).is());
                if (hasOut2)
                    Redstone.setOutput(cbworld, out2, chip.getOut(3).is());
            }

            if (chip.hasErrored()) {
                signText.setLine2(Colors.Gold + signText.getLine2());
                signText.allowUpdate();
            }
        }
    },
    
    UISO("UISO", false, true) {
    	void think(CraftBookWorld cbworld, Vector pt, SignText signText, Sign sign, IC zisoIC, Object extra) {
        	
        	basicThink(cbworld, pt, null, signText, sign, zisoIC, null, ' ', new int[]{0, 1, 2}, new int[]{0, 1, 2}, 0, 1, extra);
        }
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, IC sisoIC, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
        	
        	basicThink(cbworld, pt, changedRedstoneInput, signText, sign, sisoIC, r, mode, orderIn, orderOut, 1, 1, extra);
        }
    },
    
    MISO("MISO", false, true) {
    	void think(CraftBookWorld cbworld, Vector pt, SignText signText, Sign sign, IC zisoIC, Object extra) {
        	
        	basicThink(cbworld, pt, null, signText, sign, zisoIC, null, ' ', new int[]{0, 1, 2}, new int[]{0, 1, 2}, 0, 1, extra);
        }
        void think(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
                Sign sign, IC sisoIC, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
        	
        	basicThink(cbworld, pt, changedRedstoneInput, signText, sign, sisoIC, r, mode, orderIn, orderOut, 3, 1, extra);
        }
    };

    public final String name;
    public final boolean isSelfTriggered;
    public final boolean updateOnce;

    private ICType(String name) {
        this.name = name;
        this.isSelfTriggered = false;
        this.updateOnce = false;
    }

    private ICType(String name, boolean torchUpdate) {
        this.name = name;
        this.isSelfTriggered = torchUpdate;
        this.updateOnce = false;
    }
    
    private ICType(String name, boolean torchUpdate, boolean updateOnce) {
        this.name = name;
        this.isSelfTriggered = torchUpdate;
        this.updateOnce = updateOnce;
    }

    void think(CraftBookWorld cbworld, Vector v, Vector c, SignText t, Sign s, IC i, TickDelayer r, char mode, int[] orderIn, int[] orderOut, Object extra) {
    }

    void think(CraftBookWorld cbworld, Vector v, SignText t, Sign s, IC i, Object extra) {
    }

    public static ICType forName(String name) {
        if (name.equals("ziso"))
            return SISO;
        else if (name.equals("siso"))
            return SISO;
        else if (name.equals("si3o"))
            return SI3O;
        else if (name.equals("3iso"))
            return _3ISO;
        else if (name.equals("3i3o"))
            return _3I3O;
        else if (name.equals("vivo"))
            return VIVO;
        else if (name.equals("uiso"))
            return UISO;
        else if (name.equals("miso"))
            return MISO;
        else
            return null;
    }
    
    private static void basicThink(CraftBookWorld cbworld, Vector pt, Vector changedRedstoneInput, SignText signText,
            Sign sign, IC ic, TickDelayer r, char mode, int[] orderIn, int[] orderOut, int inputs, int outputs, Object extra)
    {
    	
    	World world = CraftBook.getWorld(cbworld);
    	
    	Vector backVec = Util.getWallSignBack(world, pt, 1);
    	if (backVec == null) return;
    	
    	Vector[] inVec = null;
    	Vector[] outVec = new Vector[outputs];
    	
    	if(inputs > 0)
    	{
    		inVec = new Vector[3];
    		inVec[orderIn[0]] = Util.getWallSignBack(world, pt, -1);
    		if (inVec[orderIn[0]] == null) return;
    		inVec[orderIn[1]] = Util.getWallSignSide(world, pt, 1);
    		if (inVec[orderIn[1]] == null) return;
    		inVec[orderIn[2]] = Util.getWallSignSide(world, pt, -1);
    		if (inVec[orderIn[2]] == null) return;
    	}
    	
    	if(outputs == 3)
    	{
    		Vector backShift;
    		if(inputs == 3)
    		{
    			backShift = Util.getWallSignBack(world, pt, 2).subtract(pt);
    			if (backShift == null) return;
    			outVec[orderOut[0]] = Util.getWallSignBack(world, pt, 3);
    			if (outVec[orderOut[0]] == null) return;
    		}
    		else
    		{
    			backShift = backVec.subtract(pt);
        		outVec[orderOut[0]] = Util.getWallSignBack(world, pt, 2);
        		if (outVec[orderOut[0]] == null) return;
    		}
    		outVec[orderOut[1]] = Util.getWallSignSide(world, pt, 1).add(backShift);
    		if (outVec[orderOut[1]] == null) return;
    		outVec[orderOut[2]] = Util.getWallSignSide(world, pt, -1).add(backShift);
    		if (outVec[orderOut[3]] == null) return;
    	}
    	else
    	{
    		outVec[0] = Util.getWallSignBack(world, pt, 2);
    	}

    	Signal[] in = new Signal[inputs];
    	
    	if(inputs == 1)
    	{
    		in[0] = new Signal(Redstone.isHighBinary(cbworld, inVec[0], true) ||
					Redstone.isHighBinary(cbworld, inVec[1], true) ||
					Redstone.isHighBinary(cbworld, inVec[2], true),
					changedRedstoneInput.equals(inVec[0]) ||
					changedRedstoneInput.equals(inVec[1]) ||
					changedRedstoneInput.equals(inVec[2]) ||
					changedRedstoneInput.equals(pt.subtract(0, 1, 0))
    		);
    		
    	}
    	else
    	{
    		for(int i = 0; i < inputs; i++)
    		{
    			in[i] = new Signal(Redstone.isHighBinary(cbworld, inVec[i], true),
    			    	changedRedstoneInput.equals(inVec[i]));
    		}
    	}
    	
    	Signal[] out = new Signal[outputs];
    	for(int i = 0; i < outputs; i++)
		{
    		out[i] = new Signal(Redstone.getOutput(world, outVec[i]));
		}
    	
    	
    	ChipState chip = new ChipState(cbworld, pt, backVec.toBlockVector(), in, out, signText, mode, world.getTime(), extra);

    	// The most important part...
    	ic.think(chip);
    	
    	if (chip.isModified())
    	{
    		for(int i = 0; i < outputs; i++)
    		{
        		Redstone.setOutput(cbworld, outVec[i], chip.getOut(i+1).is());
    		}
    	}
    	
    	if (inputs > 0 && chip.hasErrored())
    	{
    		signText.setLine2(Colors.Gold + signText.getLine2());
    		signText.allowUpdate();
    	}
    }
}