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

package org.ascnet.leaftown.client.anticheat;

import org.ascnet.leaftown.database.DatabaseConnection;
import org.ascnet.leaftown.server.TimerManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.LinkedHashSet;
import java.util.Set;

public class CheatingOffensePersister {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheatingOffensePersister.class);
    private final static CheatingOffensePersister instance = new CheatingOffensePersister();
    private final Set<CheatingOffenseEntry> toPersist = new LinkedHashSet<>();

    private CheatingOffensePersister() {
        TimerManager.getInstance().register(new PersistingTask(), 61000);
    }

    public static CheatingOffensePersister getInstance() {
        return instance;
    }

    public void persistEntry(CheatingOffenseEntry coe) {
        synchronized (toPersist) {
            toPersist.remove(coe); //equal/hashCode h4x
            toPersist.add(coe);
        }
    }

    public class PersistingTask implements Runnable {

        @Override
        public void run() {
            CheatingOffenseEntry[] offenses;
            synchronized (toPersist) {
                offenses = toPersist.toArray(new CheatingOffenseEntry[toPersist.size()]);
                toPersist.clear();
            }

            Connection con = DatabaseConnection.getConnection();
            try {
                PreparedStatement insertps = con.prepareStatement("INSERT INTO cheatlog (cid, offense, count, lastoffensetime, param) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                PreparedStatement updateps = con.prepareStatement("UPDATE cheatlog SET count = ?, lastoffensetime = ?, param = ? WHERE id = ?");
                for (CheatingOffenseEntry offense : offenses) {
                    String parm = offense.getParam() == null ? "" : offense.getParam();
                    if (offense.getDbId() == -1) {
                        insertps.setInt(1, offense.getChrfor().getId());
                        insertps.setString(2, offense.getOffense().name());
                        insertps.setInt(3, offense.getCount());
                        insertps.setTimestamp(4, new Timestamp(offense.getLastOffenseTime()));
                        insertps.setString(5, parm);
                        insertps.executeUpdate();
                        ResultSet rs = insertps.getGeneratedKeys();
                        if (rs.next()) {
                            offense.setDbId(rs.getInt(1));
                        }
                        rs.close();
                    } else {
                        updateps.setInt(1, offense.getCount());
                        updateps.setTimestamp(2, new Timestamp(offense.getLastOffenseTime()));
                        updateps.setString(3, parm);
                        updateps.setInt(4, offense.getDbId());
                        updateps.executeUpdate();
                    }
                }
                insertps.close();
                updateps.close();
            } catch (SQLException e) {
                log.error("error persisting cheatlog", e);
            }
        }
    }
}