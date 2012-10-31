import java.util.List;

/*
 * Minecraft Sitting Mod
 * Copyright (C) 2012  M4411K4
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


public class EntitySittingTracker extends OEntityTrackerEntry
{
	private int tick = 0;
	
	public EntitySittingTracker(OEntity trackedEntity, int trackingDistanceThreshold, int updateFrequency, boolean sendVelocityUpdates)
	{
		super(trackedEntity, trackingDistanceThreshold, updateFrequency, sendVelocityUpdates);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	//updatePlayerList
	public void a(List paramList)
	{
		tick++;
		if(tick >= 200)
		{
			OEntity oentity = UtilEntity.riddenByEntity(this.a);
			if(oentity == null || !(oentity instanceof OEntityPlayerMP))
			{
				return;
			}
			
			OEntityPlayerMP oplayer = (OEntityPlayerMP)oentity;
			Player player = oplayer.getPlayer();
			
			int rot = OMathHelper.d(player.getRotation() * 256.0F / 360.0F);
			int pit = OMathHelper.d(player.getPitch() * 256.0F / 360.0F);
			OPacket opacket = new OPacket32EntityLook(player.getId(), (byte)rot, (byte)pit);
			
			BaseEntity entity = new BaseEntity(this.a);
			etc.getMCServer().ad().a(oplayer, entity.getX(), entity.getY(), entity.getZ(), 64.0D, entity.getWorld().getType().getId(), opacket, entity.getWorld().getName());
			
			int headMotion = OMathHelper.d(oplayer.ap() * 256.0F / 360.0F);
			if(Math.abs(headMotion - this.i) >= 4)
			{
				opacket = new OPacket35EntityHeadRotation(player.getId(), (byte)headMotion);
				etc.getMCServer().ad().a(oplayer, entity.getX(), entity.getY(), entity.getZ(), 64.0D, entity.getWorld().getType().getId(), opacket, entity.getWorld().getName());
				this.i = headMotion;
			}
			
			tick = 0;
		}
	}
}
