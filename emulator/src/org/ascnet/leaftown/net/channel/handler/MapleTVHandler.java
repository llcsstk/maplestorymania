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

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.AbstractMaplePacketHandler;
import org.ascnet.leaftown.tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author TheRamon
 */
public class MapleTVHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*D1 00 - header
		07 00 4F 72 69 67 69 6E 32 - "Origin2"
		00 00 00 00 - ?
		80 7F 3D 36 - (910000000 -> ?) Any suggestions?

		The MapleTV packet for Origin2

		You could check who's actually watched the MapleTV here or atleast passed by...
		This is why I don't have a fuckin clue why nexon implemented this packet.
		If there's any suggestions, feel free to PM TheRamon*/
		/*String chString = slea.readMapleAsciiString();
		slea.readInt();
		slea.readInt();

		c.sendPacket(MaplePacketCreator.serverNotice(5, chString));*/
    }
}