package me.zipestudio.talkinghuds.dev;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;

//? if >=1.21.6 {
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.Waypoint;
//?}

final class DevWaypoints {

	private DevWaypoints() {
	}

	static void track(RemotePlayer player) {
		//? if >=1.21.6 {
		ClientWaypointManager manager = manager();
		if (manager != null) {
			manager.trackWaypoint(waypointOf(player));
		}
		//?}
	}

	static void update(RemotePlayer player) {
		//? if >=1.21.6 {
		ClientWaypointManager manager = manager();
		if (manager != null && player.tickCount > 0) {
			manager.updateWaypoint(waypointOf(player));
		}
		//?}
	}

	static void untrack(RemotePlayer player) {
		//? if >=1.21.6 {
		ClientWaypointManager manager = manager();
		if (manager != null) {
			manager.untrackWaypoint(TrackedWaypoint.empty(player.getUUID()));
		}
		//?}
	}

	//? if >=1.21.6 {
	private static TrackedWaypoint waypointOf(RemotePlayer player) {
		return TrackedWaypoint.setPosition(player.getUUID(), new Waypoint.Icon(), player.blockPosition());
	}

	private static ClientWaypointManager manager() {
		Minecraft mc = Minecraft.getInstance();
		return mc.player == null ? null : mc.player.connection.getWaypointManager();
	}
	//?}
}
