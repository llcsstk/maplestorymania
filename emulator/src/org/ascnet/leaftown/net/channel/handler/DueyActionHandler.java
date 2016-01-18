/*
 * This file is part of AscNet Leaftown.
 * Copyright (C) 2014 Ascension Network
 *
 * AscNet Leaftown is a fork of the OdinMS MapleStory Server.
 * The following is the original copyright notice:
 *
 *     This file is part of the OdinMS Maple Story Server
 *     Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 *                        Matthias Butz <matze@odinms.de>
 *                        Jan Christian Meyer <vimes@odinms.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation. You may not use, modify
 * or distribute this program under any other version of the
 * GNU Affero General Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ascnet.leaftown.net.channel.handler;

import org.ascnet.leaftown.client.Equip;
import org.ascnet.leaftown.client.IItem;
import org.ascnet.leaftown.client.Item;
import org.ascnet.leaftown.client.MapleCharacter;
import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.client.MapleInventoryType;
import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.net.channel.ChannelServer;
import org.ascnet.leaftown.server.MapleDueyActions;
import org.ascnet.leaftown.server.MapleInventoryManipulator;
import org.ascnet.leaftown.server.MapleItemInformationProvider;
import org.ascnet.leaftown.tools.MaplePacketCreator;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class DueyActionHandler extends AbstractMaplePacketHandler {

    public enum Actions {

        TOSERVER_SEND_ITEM(0x02),
        TOSERVER_CLOSE_DUEY(0x07),
        TOSERVER_CLAIM_PACKAGE(0x04),
        TOSERVER_REMOVE_PACKAGE(0x05),
        TOCLIENT_OPEN_DUEY(0x08),
        TOCLIENT_NOT_ENOUGH_MESOS(0x0A),
        TOCLIENT_NAME_DOES_NOT_EXIST(0x0C),
        TOCLIENT_SAMEACC_ERROR(0x0D),
        TOCLIENT_PACKAGE_MSG(0x1B),
        TOCLIENT_SUCCESSFUL_MSG(0x17), // ending byte 4 if received. 3 if delete
        TOCLIENT_SUCCESSFULLY_SENT(0x12);

        final byte code;

        private Actions(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }

        public static Actions getByType(byte type) {
            for (Actions a : Actions.values()) {
                if (a.code == type) {
                    return a;
                }
            }
            return null;
        }
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // 3D 00 [02] [00] [00 00] [00 00] [ff ff ff 7f] [06 00 73 77 6F 72 64 44] [00]
        // [hdr] .op  .iid .itempos .amt   .mesos .....  .recipient name......... .spd.
        // 3D 00 [02] [01] [01 00] [ff ff] [ff 7f 00 00] [00 00..................] 00
        c.getPlayer().resetAfkTimer();
        byte operation = slea.readByte();
        if (operation == Actions.TOSERVER_SEND_ITEM.getCode()) {
            final int fee = 5000;
            int finalcost = 0;
            boolean quick;
            String message = "";
            byte inventId = slea.readByte();
            short itemPos = slea.readShort();
            short amount = slea.readShort();
            if (amount < 1) {
                c.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_NOT_ENOUGH_MESOS.getCode()));
                c.sendPacket(MaplePacketCreator.serverNotice(1, "Having fun packet editing?"));
                return;
            }
            int mesos = slea.readInt();
            String recipient = slea.readMapleAsciiString();
            byte speed = slea.readByte();
            if (c.getPlayer().getName().equalsIgnoreCase(recipient)) {
                c.disconnect();
                return;
            }
            if (speed == 1) { //Quicksend
                message = slea.readMapleAsciiString();
                quick = true;
            } else {
                finalcost = mesos + fee + getTax(mesos); //Only normal send has a cost
                quick = false;
            }
            if (finalcost <= 0 && !quick) {
                c.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_NOT_ENOUGH_MESOS.getCode()));
                c.sendPacket(MaplePacketCreator.serverNotice(1, "Having fun packet editing?"));
                return;
            }
            if (quick) {
                if (!c.getPlayer().haveItem(5330000, 1, false, false) || message.length() > 100) { //Client editing hax
                    c.disconnect();
                    return;
                }
            }
            boolean send = false;
            if (c.getPlayer().getMeso() >= finalcost) {
                int accid = MapleCharacter.getAccIdFromCharName(recipient);
                if (accid != -1) {
                    if (accid != c.getAccID()) {
                        c.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_SUCCESSFULLY_SENT.getCode()));
                        send = true;
                    } else {
                        c.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_SAMEACC_ERROR.getCode()));
                    }
                } else {
                    c.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_NAME_DOES_NOT_EXIST.getCode()));
                }
            } else {
                c.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_NOT_ENOUGH_MESOS.getCode()));
            }
            boolean recipientOn = false;
            MapleClient rClient = null;
            try {
                int channel = c.getChannelServer().getWorldInterface().find(recipient);
                if (channel > -1) {
                    recipientOn = true;
                    ChannelServer rcserv = ChannelServer.getInstance(channel);
                    rClient = rcserv.getPlayerStorage().getCharacterByName(recipient).getClient();
                }
            } catch (RemoteException re) {
                c.getChannelServer().reconnectWorld();
            }
            if (send) {
                if (inventId > 0) {
                    MapleInventoryType inv = MapleInventoryType.getByType(inventId);
                    IItem item = c.getPlayer().getInventory(inv).getItem(itemPos); // NOTE. The checks arent in gMS order.
                    if (item != null && item.getQuantity() >= amount && c.getPlayer().getItemQuantity(item.getItemId(), false) >= amount) {
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        if (ii.isThrowingStar(item.getItemId()) || ii.isShootingBullet(item.getItemId())) {
                            MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, item.getQuantity(), true);
                        } else {
                            MapleInventoryManipulator.removeFromSlot(c, inv, (byte) itemPos, amount, true, false);
                        }
                        if (quick)
                            addItemToDB(c, item, amount, mesos, c.getPlayer().getName(), MapleCharacter.getIdByName(recipient, 0), recipientOn, true, message);
                        else
                            addItemToDB(c, item, amount, mesos, c.getPlayer().getName(), MapleCharacter.getIdByName(recipient, 0), recipientOn, false, "");
                    } else {
                        c.disconnect();
                        return;
                    }
                } else {
                    if (quick)
                        addMesoToDB(c, mesos, c.getPlayer().getName(), MapleCharacter.getIdByName(recipient, 0), recipientOn, true, message);
                    else
                        addMesoToDB(c, mesos, c.getPlayer().getName(), MapleCharacter.getIdByName(recipient, 0), recipientOn, false, "");
                }
                if (recipientOn && rClient != null) {
                    rClient.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_PACKAGE_MSG.getCode()));
                }
                if (quick) {
                    MapleInventoryManipulator.removeById(c, MapleInventoryType.CASH, 5330000, 1, false, false);
                } else {
                    c.getPlayer().gainMeso(-finalcost, false);
                }
            }
        } else if (operation == Actions.TOSERVER_REMOVE_PACKAGE.getCode()) {
            int packageid = slea.readInt();
            removePackageFromDB(packageid);
            c.sendPacket(MaplePacketCreator.removeItemFromDuey(true, packageid));
        } else if (operation == Actions.TOSERVER_CLAIM_PACKAGE.getCode()) {
            int packageid = slea.readInt();
            MapleDueyActions dp = loadSingleItem(packageid);
            if (dp.getItem() != null) {
                if (!MapleInventoryManipulator.canHold(c, Collections.singletonList(dp.getItem()))) {
                    c.getPlayer().dropMessage("Your inventory is full.");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                } else {
                    c.sendPacket(MaplePacketCreator.modifyInventory(true, MapleInventoryManipulator.addByItem(c, dp.getItem(), "Received through Duey", false)));
                }
            }
            c.getPlayer().gainMeso(dp.getMesos(), false);
            removePackageFromDB(packageid);
            c.sendPacket(MaplePacketCreator.removeItemFromDuey(false, packageid));
        }
    }

    private void addMesoToDB(MapleClient c, int mesos, String sName, int recipientID, boolean isOn, boolean quicksend, String message) {
        addItemToDB(c, null, 1, mesos, sName, recipientID, isOn, quicksend, message);
    }

    private void addItemToDB(MapleClient c, IItem item, int quantity, int mesos, String sName, int recipientID, boolean isOn, boolean quicksend, String message) {
        Connection con = DatabaseConnection.getConnection();
        int tocheck;
        if (isOn)
            tocheck = 1; // 1 = msg sent because recipient was online
        else
            tocheck = 0; // 0 = msg will be sent and will be changed to 1 when recipient logs on
        try {
            /*if (quicksend == true && !message.matches("")) {
				c.getPlayer().sendNote(recipientID, "[Duey] " + message);
			}*/
            PreparedStatement ps = con.prepareStatement("INSERT INTO dueypackages (receiverid, sendername, mesos, senttime, alerted, type, quicksend) VALUES (?, ?, ?,CURRENT_TIMESTAMP(), ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, recipientID);
            ps.setString(2, sName);
            ps.setInt(3, mesos);
            ps.setInt(4, tocheck);
            if (quicksend)
                ps.setInt(6, 1);
            else
                ps.setInt(6, 0);
            if (item == null) {
                ps.setInt(5, 3);
                ps.executeUpdate();
            } else {
                ps.setInt(5, item.getType());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                rs.next();
                PreparedStatement ps2;
                if (item.getType() == 1) { // equips
                    ps2 = con.prepareStatement("INSERT INTO dueyitems (packageid, itemid, quantity, upgradeslots, level, str, dex, `int`, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, owner) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    Equip eq = (Equip) item;
                    ps2.setInt(2, eq.getItemId());
                    ps2.setInt(3, quantity); // 1 ?
                    ps2.setInt(4, eq.getUpgradeSlots());
                    ps2.setInt(5, eq.getLevel());
                    ps2.setInt(6, eq.getStr());
                    ps2.setInt(7, eq.getDex());
                    ps2.setInt(8, eq.getInt());
                    ps2.setInt(9, eq.getLuk());
                    ps2.setInt(10, eq.getHp());
                    ps2.setInt(11, eq.getMp());
                    ps2.setInt(12, eq.getWatk());
                    ps2.setInt(13, eq.getMatk());
                    ps2.setInt(14, eq.getWdef());
                    ps2.setInt(15, eq.getMdef());
                    ps2.setInt(16, eq.getAcc());
                    ps2.setInt(17, eq.getAvoid());
                    ps2.setInt(18, eq.getHands());
                    ps2.setInt(19, eq.getSpeed());
                    ps2.setInt(20, eq.getJump());
                    ps2.setString(21, eq.getOwner());
                } else {
                    ps2 = con.prepareStatement("INSERT INTO dueyitems (packageid, itemid, quantity, owner) VALUES (?, ?, ?, ?)");
                    ps2.setInt(2, item.getItemId());
                    ps2.setInt(3, quantity);
                    ps2.setString(4, item.getOwner());
                }
                ps2.setInt(1, rs.getInt(1));
                ps2.executeUpdate();
                ps2.close();
                rs.close();
            }
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static List<MapleDueyActions> loadItems(MapleCharacter chr) {
        List<MapleDueyActions> packages = new LinkedList<>();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages LEFT JOIN dueyitems USING (packageid) WHERE receiverid = ?");
            ps.setInt(1, chr.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MapleDueyActions dueypack = getItemByPackageId(rs);
                dueypack.setSender(rs.getString("sendername"));
                dueypack.setMesos(rs.getInt("mesos"));
                dueypack.setSentTime(rs.getString("senttime"));
                packages.add(dueypack);
            }
            rs.close();
            ps.close();
            return packages;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }

    public static MapleDueyActions loadSingleItem(int packageid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM dueypackages LEFT JOIN dueyitems USING (packageid) WHERE packageid = ?");
            ps.setInt(1, packageid);
            ResultSet rs = ps.executeQuery();
            MapleDueyActions dueypack = null;
            if (rs.next()) {
                dueypack = getItemByPackageId(rs);
                dueypack.setSender(rs.getString("sendername"));
                dueypack.setMesos(rs.getInt("mesos"));
                dueypack.setSentTime(rs.getString("senttime"));
            }
            rs.close();
            ps.close();
            return dueypack;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }

    public static void receiveDueyAlert(MapleClient c, int recipientId) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE dueypackages SET alerted = 1 where receiverid = ?"); //msg received
            ps.setInt(1, recipientId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
        c.sendPacket(MaplePacketCreator.sendDueyMessage(Actions.TOCLIENT_PACKAGE_MSG.getCode()));
    }

    private int getTax(int meso) {
        int fee = 0;
        if (meso >= 10000000) {
            fee = (int) Math.round(0.04 * meso);
        } else if (meso >= 5000000) {
            fee = (int) Math.round(0.03 * meso);
        } else if (meso >= 1000000) {
            fee = (int) Math.round(0.02 * meso);
        } else if (meso >= 100000) {
            fee = (int) Math.round(0.01 * meso);
        } else if (meso >= 50000) {
            fee = (int) Math.round(0.005 * meso);
        }
        return fee;
    }

    private void removePackageFromDB(int packageid) {
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM dueypackages WHERE packageid = ?");
            ps.setInt(1, packageid);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("DELETE FROM dueyitems WHERE packageid = ?");
            ps.setInt(1, packageid);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static MapleDueyActions getItemByPackageId(ResultSet rs) {
        try {
            MapleDueyActions dueypack;
            if (rs.getInt("type") == 1) {
                Equip eq = new Equip(rs.getInt("itemid"), (byte) 0, -1);
                eq.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
                eq.setLevel((byte) rs.getInt("level"));
                eq.setStr((short) rs.getInt("str"));
                eq.setDex((short) rs.getInt("dex"));
                eq.setInt((short) rs.getInt("int"));
                eq.setLuk((short) rs.getInt("luk"));
                eq.setHp((short) rs.getInt("hp"));
                eq.setMp((short) rs.getInt("mp"));
                eq.setWatk((short) rs.getInt("watk"));
                eq.setMatk((short) rs.getInt("matk"));
                eq.setWdef((short) rs.getInt("wdef"));
                eq.setMdef((short) rs.getInt("mdef"));
                eq.setAcc((short) rs.getInt("acc"));
                eq.setAvoid((short) rs.getInt("avoid"));
                eq.setHands((short) rs.getInt("hands"));
                eq.setSpeed((short) rs.getInt("speed"));
                eq.setJump((short) rs.getInt("jump"));
                eq.setOwner(rs.getString("owner"));
                dueypack = new MapleDueyActions(rs.getInt("packageid"), eq);
            } else if (rs.getInt("type") == 2) {
                Item newItem = new Item(rs.getInt("itemid"), (byte) 0, rs.getShort("quantity"));
                newItem.setOwner(rs.getString("owner"));
                dueypack = new MapleDueyActions(rs.getInt("packageid"), newItem);
            } else {
                dueypack = new MapleDueyActions(rs.getInt("packageid"));
            }
            return dueypack;
        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        }
    }
}