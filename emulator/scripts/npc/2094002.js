var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
	status++;
    } else {
	status--;
    }
    if (cm.getPlayer().getMapId() == 925100700) {
	cm.removeAll(4001117);
	cm.removeAll(4001120);
	cm.removeAll(4001121);
	cm.removeAll(4001122);
	cm.warp(251010404,0);
	cm.dispose();
	return;
    }
    var em = cm.getEventManager("Pirate");
    if (em == null) {
	cm.sendNext("The event isn't started.");
	cm.dispose();
	return;
    }
    if (!cm.isLeader()) {
	cm.sendNext("Tell your leader to talk to me.");
	cm.dispose();
	return;
    }
    switch(cm.getPlayer().getMapId()) {
	case 925100000:
	   cm.sendNext("We are heading into the Pirate Ship now! To get in, we must destroy all the monsters guarding it.");
	   cm.dispose();
	   break;
	case 925100100:
	   var emp = em.getProperty("stage2");
	   if (emp == null) {
		em.setProperty("stage2", "0");
		emp = "0";
	   }
	   if (emp.equals("0")) {
		if (cm.haveItem(4001120, 3)) {
		    cm.sendNext("Excellent! Now give me 3 #i4001121#.");
		    cm.gainItem(4001120, -3);
		    em.setProperty("stage2", "1");
		} else {
	   	    cm.sendNext("We are heading into the Pirate Ship now! To get in, we must qualify ourselves as noble pirates. Give me 3 #i4001120#.");
		}
	   } else if (emp.equals("1")) {
		if (cm.haveItem(4001121, 3)) {
		    cm.sendNext("Excellent! Now give me 3 #i4001122#.");
		    cm.gainItem(4001121, -3);
		    em.setProperty("stage2", "2");
		} else {
	   	    cm.sendNext("We are heading into the Pirate Ship now! To get in, we must qualify ourselves as noble pirates. Give me 3 #i4001121#.");
		}
	   } else if (emp.equals("2")) {
		if (cm.haveItem(4001122, 3)) {
		    cm.sendNext("The portal has now opened.");
		    cm.gainItem(4001122, -3);
		    em.setProperty("stage2", "3");
		} else {
	   	    cm.sendNext("We are heading into the Pirate Ship now! To get in, we must qualify ourselves as noble pirates. Give me 3 #i4001122#.");
		}
	   } else {
		cm.sendNext("The next stage has opened!");
	   }
	   cm.dispose();
	   break;
	case 925100200:
	   cm.sendNext("To assault the pirate ship, we must destroy the guards first.");
	   cm.dispose();
	   break;
	case 925100201:
	   if (cm.getMap().getAllMonstersThreadsafe().size() == 0) {
		cm.sendNext("Excellent.");
		if (em.getProperty("stage2a").equals("0")) {
		    cm.getMap().setReactorState();
		    em.setProperty("stage2a", "1");
		}
	   } else {
	   	cm.sendNext("These bellflowers are in hiding. We must liberate them.");
	   }
	   cm.dispose();
	   break;
	case 925100301:
	   if (cm.getMap().getAllMonstersThreadsafe().size() == 0) {
		cm.sendNext("Excellent.");
		if (em.getProperty("stage3a").equals("0")) {
		    cm.getMap().setReactorState();
		    em.setProperty("stage3a", "1");
		}
	   } else {
	   	cm.sendNext("These bellflowers are in hiding. We must liberate them.");
	   }
	   cm.dispose();
	   break;
	case 925100202:
	case 925100302:
	   cm.sendNext("These are the Captains and Krus which devote their whole life to Lord Pirate. Kill them as you see fit.");
	   cm.dispose();
	   break;
	case 925100400:
	   cm.sendNext("These are the sources of the ship's power. We must seal it by using the Old Metal Keys on the doors!");
	   cm.dispose();
	   break;
	case 925100500:
	   if (cm.getMap().getAllMonstersThreadsafe().size() == 0) {
		cm.warpParty(925100600);
	   } else {
	   	cm.sendNext("Defeat all monsters! Even Lord Pirate's minions!");
	   }
	   cm.dispose();
	   break;
    }
}