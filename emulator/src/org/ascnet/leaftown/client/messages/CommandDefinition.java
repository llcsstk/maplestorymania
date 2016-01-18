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

package org.ascnet.leaftown.client.messages;

public class CommandDefinition {

    private final String command;
    private final String parameterDescription;
    private final String help;
    private final int requiredLevel; // gm level

    public CommandDefinition(String command, String parameterDescription, String help, int requiredLevel) {
        this.command = command;
        this.help = help;
        this.parameterDescription = parameterDescription;
        this.requiredLevel = requiredLevel;
    }

    public String getCommand() {
        return command;
    }

    public String getHelp() {
        return help;
    }

    public String getParameterDescription() {
        return parameterDescription;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}