/*
t * This file is part of AscNet Leaftown.
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

package org.ascnet.leaftown.scripting.npc;

import org.ascnet.leaftown.client.MapleClient;
import org.ascnet.leaftown.net.world.MaplePartyCharacter;
import org.ascnet.leaftown.scripting.AbstractScriptManager;
import org.ascnet.leaftown.tools.MaplePacketCreator;

import javax.script.Invocable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Matze
 */
public class NPCScriptManager extends AbstractScriptManager 
{
    private final Map<MapleClient, NPCConversationManager> cms = new HashMap<>();
    private final Map<MapleClient, Invocable> scripts = new HashMap<>();
    private static final NPCScriptManager instance = new NPCScriptManager();

    public synchronized static NPCScriptManager getInstance() 
    {
        return instance;
    }

    public void start(MapleClient c, int npc)
    {
        try 
        {
            final NPCConversationManager cm = new NPCConversationManager(c, npc);
            
            if (cms.containsKey(c)) 
                return;
            
            cms.put(c, cm);
            
            final Invocable iv = getInvocable("npc/" + npc + ".js", c);
            
            if (iv == null || NPCScriptManager.getInstance() == null) 
            {
            	MaplePacketCreator.boxMessage("O NPC ainda não está disponível. Por favor, contate os Administradores.");
            	log.info("Não foi encontrado o arquivo de script para o NPC " + npc);
                cm.dispose();
                return;
            }
            engine.put("cm", cm);
            scripts.put(c, iv);
            iv.invokeFunction("start");
        }
	    catch (UndeclaredThrowableException e) 
	    {
	    	c.sendPacket(MaplePacketCreator.boxMessage("Ocorreu um erro na execução desse NPC. Por favor, contate os Administradores."));
	    	log.error("Error executing NPC script in start: " + npc, e);
	        dispose(c);
	        cms.remove(c);
	    } 
	    catch (Exception e) 
	    {
	    	c.sendPacket(MaplePacketCreator.boxMessage("Ocorreu um erro na execução desse NPC. Por favor, contate os Administradores."));
	        log.error("Error executing NPC script in start: " + npc, e);
	        dispose(c);
	        cms.remove(c);
	    }
    }

    public void start(MapleClient c, int npc, String fname, String... params)
    {
        try 
        {
            final NPCConversationManager cm = new NPCConversationManager(c, npc);
            
            if (cms.containsKey(c)) 
                return;

            cms.put(c, cm);
            
            final Invocable iv = getInvocable("npc/" + fname + ".js", c);
            if (iv == null || NPCScriptManager.getInstance() == null) 
            {
            	MaplePacketCreator.boxMessage("O NPC ainda não está disponível. Por favor, contate os Administradores.");
            	log.info("Não foi encontrado o arquivo de script para o NPC " + npc);
                cm.dispose();
                return;
            }
            
            engine.put("cm", cm);
            
            for (int paramid = 0x00; paramid < params.length; paramid++)
                engine.put("param" + paramid, params[paramid]);
            
            scripts.put(c, iv);
            iv.invokeFunction("start");
        } 
	    catch (UndeclaredThrowableException e) 
	    {
	    	c.sendPacket(MaplePacketCreator.boxMessage("Ocorreu um erro na execução desse NPC. Por favor, contate os Administradores."));
	    	log.error("Error executing NPC script in start: " + npc, e);
	        dispose(c);
	        cms.remove(c);
	    } 
	    catch (Exception e) 
	    {
	    	c.sendPacket(MaplePacketCreator.boxMessage("Ocorreu um erro na execução desse NPC. Por favor, contate os Administradores."));
	        log.error("Error executing NPC script in start: " + npc, e);
	        dispose(c);
	        cms.remove(c);
	    }
    }

    public void start(String filename, MapleClient c, int npc, List<MaplePartyCharacter> chars) { // CPQ start
        try
        {
            final NPCConversationManager cm = new NPCConversationManager(c, npc, chars, 0);
            cm.dispose();
            if (cms.containsKey(c)) 
                return;

            cms.put(c, cm);
            final Invocable iv = getInvocable("npc/" + filename + ".js", c);
            final NPCScriptManager npcsm = NPCScriptManager.getInstance();
            
            if (iv == null || NPCScriptManager.getInstance() == null || npcsm == null) 
            {
            	MaplePacketCreator.boxMessage("O NPC ainda não está disponível. Por favor, contate os Administradores.");
            	log.info("Não foi encontrado o arquivo de script para o NPC " + npc + " com o FileName " + filename);
                cm.dispose();
                return;
            }
            
            engine.put("cm", cm);
            scripts.put(c, iv);
            iv.invokeFunction("start", chars);
	    }
	    catch (UndeclaredThrowableException e) 
	    {
	    	c.sendPacket(MaplePacketCreator.boxMessage("Ocorreu um erro na execução desse NPC. Por favor, contate os Administradores."));
	    	log.error("Error executing NPC script in start: " + npc, e);
	        dispose(c);
	        cms.remove(c);
	    } 
	    catch (Exception e) 
	    {
	    	c.sendPacket(MaplePacketCreator.boxMessage("Ocorreu um erro na execução desse NPC. Por favor, contate os Administradores."));
	        log.error("Error executing NPC script in start: " + npc, e);
	        dispose(c);
	        cms.remove(c);
	    }
    }

    public void action(MapleClient c, byte mode, byte type, int selection) 
    {
        final Invocable ns = scripts.get(c);
        if (ns != null) 
        {
            try 
            {
                ns.invokeFunction("action", mode, type, selection);
            } 
            catch (Exception e) 
            {
                if (c.getCM() != null)
                    log.error("Error executing NPC script in action " + c.getCM().getNpc() + ". " + e.getMessage(), e);
                
    	    	c.sendPacket(MaplePacketCreator.boxMessage("Ocorreu um erro na execução desse NPC. Por favor, contate os Administradores."));
                dispose(c);
            }
        }
    }

    public void dispose(NPCConversationManager cm) 
    {
        final MapleClient c = cm.getC();
        
        cms.remove(c);
        scripts.remove(c);
        resetContext("npc/" + cm.getNpc() + ".js", c);
    }

    public void dispose(MapleClient c)
    {
        final NPCConversationManager npccm = cms.get(c);
        
        if (npccm != null) 
            dispose(npccm);
    }

    public NPCConversationManager getCM(MapleClient c)
    {
        return cms.get(c);
    }
}