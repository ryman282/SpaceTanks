package a3;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;
import java.util.Vector;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.networking.client.GameConnectionClient;

public class Client extends GameConnectionClient{
	
	private Game game;
	private UUID id;
	private Vector<GhostAvatar> ghostAvatars;
	private String team;

	public Client(InetAddress remoteAddr, int remotePort, ProtocolType protocolType, Game game) throws IOException {
		super(remoteAddr, remotePort, protocolType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
		sendJoinMessage();
	}
	
	protected void processPacket(Object msg) {
		if(msg != null) {
			String[] msgTokens = msg.toString().split(",");
			if(msgTokens[0].compareTo("join")==0) {
				if(msgTokens[1].compareTo("success")==0) {
					System.out.println("Client joined server");
					game.setIsConnected(true);
					if(msgTokens[2].equals("blue")) {
						team = "blue";
					} else {
						team = "orange";
					}
					sendCreateMessage(game.getPlayerPosition());
					//createGhostAvatar(id, game.getPlayerPosition());
					sendWantsDetailsMessage(id);
					System.out.println("This ID: " + this.id.toString());
				}
				if(msgTokens[1].compareTo("failure")==0) {
					System.out.println("Client could not connect to server");
					game.setIsConnected(false);
				}
			}
			if(msgTokens[0].compareTo("bye")==0) {
				System.out.println("Client left server");
				UUID ghostID = UUID.fromString(msgTokens[1]);
				game.removeGhost(ghostID);
				//removeGhostAvatar(ghostID);
			}
			if(msgTokens[0].compareTo("dsfr")==0) {
				System.out.println("Client recieved details for ghost");
				UUID ghostID = UUID.fromString(msgTokens[1]);
				String x = msgTokens[2];
				String y = msgTokens[3];
				String z = msgTokens[4];
				String teamColor = msgTokens[5];
				Point3D ghostPosition = new Point3D(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
				createGhostAvatar(ghostID, ghostPosition, teamColor);
			}
			if(msgTokens[0].compareTo("wsds")==0) {
				System.out.println("Client wants details");
				UUID clientID = UUID.fromString(msgTokens[1]);
				Float x = (float) game.getPlayerPosition().getX();
				Float y = (float) game.getPlayerPosition().getY();
				Float z = (float) game.getPlayerPosition().getZ();
				Point3D ghostPosition = new Point3D(x, y, z);
				String teamColor = game.getPlayer().getTeam();
				sendTheDeets(clientID, this.id, ghostPosition, teamColor);
			}
			if(msgTokens[0].compareTo("move")==0) {
				System.out.println("Client moved");
				String x = msgTokens[1];
				String y = msgTokens[2];
				String z = msgTokens[3];
				Point3D ghostPosition = new Point3D(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
				sendMoveMessage(ghostPosition);
			}
			if(msgTokens[0].compareTo("ghostmove")==0) {
				//System.out.println("A ghost has moved");
				UUID ghostID = UUID.fromString(msgTokens[1]);
				String x = msgTokens[2];
				String y = msgTokens[3];
				String z = msgTokens[4];
				Point3D ghostPosition = new Point3D(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
				updatePosition(ghostID, ghostPosition);
				game.moveGhost(ghostID, ghostPosition);
			}
			if(msgTokens[0].compareTo("create")==0) {
				System.out.println("Ghost created");
				UUID ghostID = UUID.fromString(msgTokens[1]);
				String x = msgTokens[2];
				String y = msgTokens[3];
				String z = msgTokens[4];
				String teamColor = (String) msgTokens[5];
				Point3D ghostPosition = new Point3D(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
				createGhostAvatar(ghostID, ghostPosition, teamColor);
			}
			if(msgTokens[0].compareTo("tattack")==0) {
				UUID ghostID = UUID.fromString(msgTokens[1]);
				String posX = msgTokens[2];
				String posY = msgTokens[3];
				String posZ = msgTokens[4];
				String vecX = msgTokens[5];
				String vecY = msgTokens[6];
				String vecZ = msgTokens[7];
				Point3D bulletPos = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				Vector3D bulletVec = new Vector3D(Float.parseFloat(vecX), Float.parseFloat(vecY), Float.parseFloat(vecZ));
				String team = msgTokens[8];
				createBullet(bulletPos, bulletVec, team);
			}
			if(msgTokens[0].compareTo("update")==0) {
				UUID ghostID = UUID.fromString(msgTokens[1]);
				String posX = msgTokens[2];
				String posY = msgTokens[3];
				String posZ = msgTokens[4];
				String dirTX = msgTokens[5];
				String dirTY = msgTokens[6];
				String dirTZ = msgTokens[7];
				String dirBX = msgTokens[8];
				String dirBY = msgTokens[9];
				String dirBZ = msgTokens[10];
				int hp = Integer.parseInt(msgTokens[11]);
				boolean boost = Boolean.parseBoolean(msgTokens[12]);
				boolean hurt = Boolean.parseBoolean(msgTokens[13]);
				Point3D ghostPos = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				Vector3D ghostDirT = new Vector3D(Float.parseFloat(dirTX), Float.parseFloat(dirTY), Float.parseFloat(dirTZ));
				Vector3D ghostDirB = new Vector3D(Float.parseFloat(dirBX), Float.parseFloat(dirBY), Float.parseFloat(dirBZ));
				updatePosition(ghostID, ghostPos);
				game.moveGhost(ghostID, ghostPos, ghostDirT, ghostDirB, hp, boost, hurt);
			}
			if(msgTokens[0].compareTo("checknear")==0) {
				String posX = msgTokens[1];
				String posY = msgTokens[2];
				String posZ = msgTokens[3];
				int id = Integer.parseInt(msgTokens[4]);
				Point3D pos = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				game.checkForAvatarNear(pos, id);
			}
			if(msgTokens[0].compareTo("checkMediumNear")==0) {
				String posX = msgTokens[1];
				String posY = msgTokens[2];
				String posZ = msgTokens[3];
				int id = Integer.parseInt(msgTokens[4]);
				Point3D pos = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				game.checkForAvatarMediumNear(pos, id);
			}
			if(msgTokens[0].compareTo("npcInfo")==0) {
				String posX = msgTokens[1];
				String posY = msgTokens[2];
				String posZ = msgTokens[3];
				int id = Integer.parseInt(msgTokens[4]);
				Point3D pos = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				game.updateNPC(pos, id);
			}
			if(msgTokens[0].compareTo("importNPCS")==0) {
				String posX = msgTokens[1];
				String posY = msgTokens[2];
				String posZ = msgTokens[3];
				int id1 = Integer.parseInt(msgTokens[4]);
				Point3D pos1 = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				posX = msgTokens[5];
				posY = msgTokens[6];
				posZ = msgTokens[7];
				int id2 = Integer.parseInt(msgTokens[8]);
				Point3D pos2 = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				posX = msgTokens[9];
				posY = msgTokens[10];
				posZ = msgTokens[11];
				int id3 = Integer.parseInt(msgTokens[12]);
				Point3D pos3 = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				posX = msgTokens[13];
				posY = msgTokens[14];
				posZ = msgTokens[15];
				int id4 = Integer.parseInt(msgTokens[16]);
				Point3D pos4 = new Point3D(Float.parseFloat(posX), Float.parseFloat(posY), Float.parseFloat(posZ));
				game.createNPC(pos1, id1, pos2, id2, pos3, id3, pos4, id4);
			}
			if(msgTokens[0].compareTo("getBig")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.makeNPCBig(id);
			}
			if(msgTokens[0].compareTo("getSmall")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.makeNPCSmall(id);
			}
			if(msgTokens[0].compareTo("getNPCLoc")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.updateNPCLocation(id);
			}
			if(msgTokens[0].compareTo("lookAtAvatar")==0) {
				UUID ghostID = UUID.fromString(msgTokens[1]);
				int id = Integer.parseInt(msgTokens[2]);
				game.lookAtAvatar(ghostID, id);
			}
			if(msgTokens[0].compareTo("dead")==0) {
				UUID ghostID = UUID.fromString(msgTokens[1]);
				game.removeGhost(ghostID);
			}
			if(msgTokens[0].compareTo("NPCdead")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.removeNPC(id);
			}
			if(msgTokens[0].compareTo("NPChurt")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.hurtNPC(id);
			}
			if(msgTokens[0].compareTo("removeHealthPack")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.removeHealthPack(id);
			}
			if(msgTokens[0].compareTo("npcNotHurt")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.npcNotHurt(id);
			}
			if(msgTokens[0].compareTo("startTick")==0) {
				int count = Integer.parseInt(msgTokens[1]);
				game.updateGameStartTimer(count);
			}
			if(msgTokens[0].compareTo("npcAttack")==0) {
				int id = Integer.parseInt(msgTokens[1]);
				game.createNPCBullet(game.getNPC(id));
			}
			if(msgTokens[0].compareTo("createMine")==0) {
				float x = Float.parseFloat(msgTokens[1]);
				float y = Float.parseFloat(msgTokens[2]);
				float z = Float.parseFloat(msgTokens[3]);
				Point3D p = new Point3D(x, y ,z);
				String team = (String)msgTokens[4];
				//game.createEnemyMine(p, team);
			}
		}
	}
	
	private void sendTheDeets(UUID clientID, UUID remID, Point3D pos, String teamColor) {
		try {
			String message = "dsfr," + clientID.toString() + "," + remID.toString() + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + teamColor;
			sendPacket(message);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	private void sendWantsDetailsMessage(UUID clientID) {
		try {
			String message = "wsds," + clientID.toString();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updatePosition(UUID ghostID, Point3D ghostPosition) {
		Iterator<GhostAvatar> itr = ghostAvatars.iterator();
		while(itr.hasNext()) {
			GhostAvatar ga = itr.next();
			if(ga.getID().equals(ghostID)) {
				ga.setPosition(ghostPosition);
			}
		}
	}

	private void createGhostAvatar(UUID ghostID, Point3D ghostPosition, String teamColor) {
		GhostAvatar newGhost = new GhostAvatar(ghostID, ghostPosition, game, teamColor);
		ghostAvatars.add(newGhost);
		game.addGhost(ghostID, ghostPosition, teamColor);
		System.out.println("ghostID created: " + ghostID.toString());
	}
	
	public void removeGhostAvatar(UUID ghostID) {
		Iterator<GhostAvatar> itr = ghostAvatars.iterator();
		while(itr.hasNext()) {
			GhostAvatar ghost = itr.next();
			if(ghost.getID().equals(ghostID)) {
				System.out.println("Ghost removed!");
				itr.remove();
			}
		}
		/*
		for(GhostAvatar ghost : ghostAvatars) {
			if(ghost.getID().equals(ghostID)) {
				ghostAvatars.remove(ghost);
				break;
			}
		}
		*/
	}
	
	public void createBullet(Point3D pos, Vector3D vec, String team) {
		game.createEnemyBullet(pos, vec, team);
	}

	public void sendCreateMessage(Point3D pos) {
		try {
			String message = new String("create," + id.toString());
			message += "," + pos.getX() + "," + pos.getY() + "," + pos.getZ();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendJoinMessage() {
		try {
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendByeMessage() {
		try {
			sendPacket(new String("bye," + id.toString()));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendDetailsForMessage(UUID remID, Vector3D pos, String teamColor) {
		try {
			String message = new String("dsfr," + id.toString() + "," + remID.toString() + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + teamColor);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMoveMessage(Point3D pos) {
		try {
			String message = new String("move," + this.id.toString() + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ());
			updatePosition(this.id, pos);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendTankAttackMessage(Point3D pos, Vector3D v) {
		try {
			String message = new String("tattack," + this.id.toString() + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + v.getX() + "," + v.getY() + "," + v.getZ() + "," + game.getPlayer().getTeam());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendUpdateGhostPosition(Point3D pos, Vector3D dirT, Vector3D dirB, int hp, boolean boost, boolean hurt) {
		try {
			String message = new String("update," + this.id.toString() + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," 
		+ 			dirT.getX() + "," + dirT.getY() + "," + dirT.getZ() + "," + dirB.getX() + "," + dirB.getY() + "," + dirB.getZ() + ","
					+ hp + "," + boost + "," + hurt);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Vector<GhostAvatar> getGhosts() {
		return ghostAvatars;
	}

	public void sendNPCNear(boolean b, int id, float distance) {
		try {
			String message = new String("npcNearCheck," + this.id.toString() + "," + b + "," + id + "," + distance);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendNPCMediumNear(boolean b, int id, float distance) {
		try {
			String message = new String("npcMediumNearCheck," + this.id.toString() + "," + b + "," + id + "," + distance);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public UUID getID() {
		return this.id;
	}

	public void importNPCS() {
		try {
			String message = new String("importNPCS," + this.id.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateNPCLocation(Point3D pos, int id) {
		try {
			String message = new String("updateNPCLoc," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + id);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendDeadMessage(UUID ghostID) {
		try {
			String message = new String("dead," + ghostID.toString());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getTeam() {
		return team;
	}

	public void sendNPCDead(UFO npc) {
		try {
			String message = new String("NPCdead," + id + "," + npc.getID());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendNPCHurt(UFO npc) {
		try {
			String message = new String("NPChurt," + id + "," + npc.getID());
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendRemoveHealthPack(int id) {
		try {
			String message = new String("removeHealthPack," + this.id + "," + id);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendNPCAttackMessage(int id) {
		try {
			String message = new String("npcAttack," + this.id + "," + id);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendCreateMineMessage(Point3D p, String team) {
		try {
			String message = new String("createMine," + this.id + "," + p.getX() + "," + p.getY() + "," + p.getZ() + "," + team);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
