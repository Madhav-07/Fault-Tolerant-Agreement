package demo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import common.Game;
import common.Machine;

public class Game_0022 extends Game {
    private ArrayList<Machine> machine_list;
    private int numFaulty;
    private int phaseNum;

    public Game_0022() {
        phaseNum = 0;
    }

    @Override
    public void addMachines(ArrayList<Machine> machines, int numFaulty) { 
        machine_list = machines;
        this.numFaulty = numFaulty;
        sendMachineList();          // sends machine_list to every machine
    }

    @Override
    public void startPhase() {
        phaseNum++;
        System.out.println("Phase " + phaseNum);

        // randomly tag numFaulty machines as faulty
        selectFaultyMachines sel = new selectFaultyMachines(machine_list.size());
        Set<Integer> faulty = sel.select(numFaulty);
        Iterator<Integer> j = faulty.iterator();
        Integer temp = j.next();

        for(int i = 0;i < machine_list.size();i++) {
            if(temp == i) {
                machine_list.get(i).setState(false);
                if(j.hasNext())
                    temp = j.next();
            }
            else {
                machine_list.get(i).setState(true);
            }
        }
        
        // choose one machine as leader
        faulty = sel.select(1);

        // round 0: leader sends message to all
        for(int num : faulty) {
            machine_list.get(num).setLeader();
        }
    }

    @Override
    public void startPhase(int leaderId, ArrayList<Boolean> areCorrect) {
        // setting states of each machine
        for(int i = 0;i < areCorrect.size();i++) {
            machine_list.get(i).setState(areCorrect.get(i));
        }

        // setting leader
        machine_list.get(leaderId).setLeader();
    }

    // sends machine_list to every machine
    void sendMachineList() {
        for(Machine temp : machine_list) {
            temp.setMachines(machine_list);
        }
    }
}

// class to randomly select t machines
class selectFaultyMachines {
    private Set<Integer> faultList;
    private Random rand;
    private int noOfMachines;

    selectFaultyMachines(int noOfMachines) {
        faultList = new TreeSet<>();
        rand = new Random();
        this.noOfMachines = noOfMachines;
    }

    // selecting "num" random numbers
    Set<Integer> select(int num) {
        if(!faultList.isEmpty()) {
            faultList.clear();
        }
        while(faultList.size() < num) {
            faultList.add(rand.nextInt(noOfMachines));
        }
        return faultList;
    }
}