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

package org.ascnet.leaftown.client;

import org.ascnet.leaftown.tools.ValueHolder;

public enum MapleDisease implements ValueHolder<Long> {

    NULL(0x0L),
    SLOW(0x1L),
    SEDUCE(0x80L),
    //CURSE(0x200L),
    STUN(0x2000000000000L),
    POISON(0x4000000000000L),
    SEAL(0x8000000000000L),
    GM_DISABLE_SKILL(0x8000000000000L),
    GM_DISABLE_MOVEMENT(0x2000000000000L),
    DARKNESS(0x10000000000000L),
    WEAKEN(0x4000000000000000L),
    ZOMBIFY(0x4000L),
    CRAZY_SKULL(0x80000L),
    CURSE(0x8000000000000000L),
    BLIND(0x2000L),
    FREEZE(0x80000L);

    private final long i;

    private MapleDisease(long i) {
        this.i = i;
    }

    @Override
    public Long getValue() {
        return i;
    }
}