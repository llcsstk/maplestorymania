/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
					   Matthias Butz <matze@odinms.de>
					   Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/**
 *Dollhouse Event
**/
importPackage(Packages.client);
importPackage(Packages.server.life);
importPackage(Packages.tools);

var returnMap;
var map;
var eim;
var minPlayers = 1;
var instanceId;

function init() {
    em.setProperty("noEntry","false");
}

function monsterValue(eim, mobId) {
    return 1;
}

function setup(eim) {
    em.setProperty("noEntry","true");
	instanceId = em.getChannelServer().getInstanceId();
	var instanceName = "DollHouse_" + instanceId;
	var eim = em.newInstance(instanceName);
	return eim;
	
}

function playerEntry(eim, player) {
    returnMap = em.getChannelServer().getMapFactory().getMap(221024400);
	var map = eim.getMapFactory().getMap(922000010);
    player.changeMap(map, map.getPortal(0));
    map.shuffleReactors();
	var eventTime = 10 * 60000;
    eim.schedule("timeOut", eventTime); // invokes "timeOut" in how ever many seconds.
	eim.startEventTimer(eventTime);
}

function playerExit(eim, player) {
    em.setProperty("noEntry","false");
    player.changeMap(returnMap, returnMap.getPortal(4));
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function timeOut() {
    em.setProperty("noEntry","false");
    var player = eim.getPlayers().get(0);
    player.changeMap(returnMap, returnMap.getPortal(4));
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function playerDisconnected(eim, player) {
    em.setProperty("noEntry","false");
    player.getMap().removePlayer(player);
    player.setMap(returnMap);
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function clear(eim) {
    em.setProperty("noEntry","false");
    var player = eim.getPlayers().get(0);
    player.changeMap(returnMap, returnMap.getPortal(4));
    eim.unregisterPlayer(player);
    em.cancel();
    em.disposeInstance("DollHouse");
    eim.dispose();
}

function cancelSchedule() {
}

function dispose() {
}
