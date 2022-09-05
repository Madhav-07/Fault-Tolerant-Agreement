package demo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import common.Location;
import common.Machine;

public class Machine_0022 extends Machine {

	private ArrayList<Machine> machine_list;
	private int id;										// id of machine
	private selectyMachines sel;						// class used for random generator
	private int t;										// max no of faulty machines
	private int stepSize;
	private String name;								// name of machine
	private Location position;
	private int step_x;									// change in x-coordinate
	private int step_y;									// change in y-coordinate
	private int dir;									// the direction machine is facing
	private int phaseNum;
	private Random rand;
	private boolean round1_complete,round2_complete;
	private int left1,left2,right1,right2;				// counters
	private boolean error_state;						// indicates error_state
	private boolean isCorrect;

	public Machine_0022() {
		name = "0022";
		position = new Location(0, 0);
		dir = 1;
		phaseNum = 0;
		rand = new Random();
		round1_complete = round2_complete = true;
		left1 = left2 = right1 = right2 = 0;
		error_state = false;
	}
	
	@Override
	public void setMachines(ArrayList<Machine> machines) {
		this.machine_list = machines;
        id = machine_list.indexOf(this);	// finding id of itself from machine_list
        sel = new selectyMachines(machines.size());

		// calculation of max no of faulty machines
        t = machine_list.size()/3;
        if(t * 3 == machine_list.size()) {
            t--;
        }
	}	

	@Override
	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
		step_x = 0;
		step_y = stepSize;
	}

	@Override
	public void setState(boolean isCorrect) {
		this.isCorrect = isCorrect;
	}

	@Override
	public void setLeader() {
		left1 = left2 = right1 = right2 = 0;
		round1_complete = round2_complete = false;

		// 
		int direction = rand.nextInt(2);
		phaseNum++;
		if(isCorrect) {
            // send same message to all
            for(Machine temp : machine_list) {
                temp.sendMessage(id, phaseNum, 0, direction);
            }
        }
		else {
            // select t machines
            int limit = rand.nextInt(t + 1);

            Set<Integer> temp = sel.select(limit);
            Iterator<Integer> j = temp.iterator();
			Integer comp = -1;
			if(j.hasNext())
            	comp = j.next();
            for(int i = 0;i < machine_list.size();i++) {
                // sending 2t+1 same message
                if(comp != i) {
                    machine_list.get(i).sendMessage(id, phaseNum, 0, direction);
                }
                // either sending random message or stay silent to t machines
                else {
                    int temp_dec = rand.nextInt(3);
                    if(temp_dec != 2) {
                        machine_list.get(i).sendMessage(id, phaseNum, 0, temp_dec);
                    }
					if(j.hasNext())
                        comp = j.next();
                }
            }
        }
	}

	@Override
	public void sendMessage(int sourceId, int phaseNum, int roundNum, int decision) {
		// if machine in error state
		if(error_state == true) {
			return;
		}

		if(this.phaseNum != phaseNum) {
			this.phaseNum = phaseNum;
			if(round2_complete == false) {
				System.out.println(name + ": Error State");
				error_state = true;
				return;
			}
			left1 = left2 = right1 = right2 = 0;
			round1_complete = round2_complete = false;
		}

		// round 0
		if(roundNum == 0) {
			if(isCorrect) {
				// sending correct message to all machines
				for(Machine temp : machine_list) {
					temp.sendMessage(id, phaseNum, 1, decision);
				}
			}
			else {
				// sending random message to all machines
				int temp_dec = rand.nextInt(3);
				if(temp_dec != 2) {
					for(Machine temp : machine_list) {
						temp.sendMessage(id, phaseNum, 1, temp_dec);
					}
				}
			}
		}
		// round 1
		else if(roundNum == 1 && round1_complete == false) {
			if(decision == 0) {
				left1++;
			}
			else if(decision == 1) {
				right1++;
			}

			// if we received t + 1 same messages
			if(left1 >= t + 1 || right1 >= t + 1) {
				round1_complete = true;
				if(isCorrect) {
					int dec;
					if(left1 > right1) {
						dec = 0;
					}
					else {
						dec = 1;
					}
					// sending correct message to all machines
					for(Machine temp : machine_list) {
						temp.sendMessage(id, phaseNum, 2, dec);
					}
				}
				else {
					// sending random message to all machines
					int temp_dec = rand.nextInt(3);
					if(temp_dec != 2) {
						for(Machine temp : machine_list) {
							temp.sendMessage(id, phaseNum, 2, temp_dec);
						}
					}
				}
				left1 = right1 = 0;
			}
		}
		// round 2
		else if(roundNum == 2 && round2_complete == false) {
			if(decision == 0) {
				left2++;
			}
			else if(decision == 1) {
				right2++;
			}

			// if majority is reached
			if(left2 >= 2*t + 1 || right2 >= 2*t + 1) {
				round2_complete = true;
				if(isCorrect) {
					int dec;
					if(left2 > right2) {
						dec = 0;
					}
					else {
						dec = 1;
					}
					// changing to correct direction
					change_direction(dec);
				}
				else {
					// changing to random direction
					int temp_dec = rand.nextInt(2);
					change_direction(temp_dec);
				}
				left2 = right2 = 0;
			}
		}
	}

	void change_direction(int decision) {
		// North or +Y axis
        if(dir == 1) {
			// right
            if(decision == 1) {
                step_x = stepSize;
                step_y = 0;
                dir = 2;
            }
			// left
            else if(decision == 0) {
                step_x = -stepSize;
                step_y = 0;
                dir = 4;
            }
        }
        // East or +X axis
        else if(dir == 2) {
			// right
            if(decision == 1) {
                step_x = 0;
                step_y = -stepSize;
                dir = 3;
            }
			// left
            else if(decision == 0) {
                step_x = 0;
                step_y = stepSize;
                dir = 1;
            }
        }
        // South or -Y axis
        else if(dir == 3) {
			// right
            if(decision == 1) {
                step_x = -stepSize;
                step_y = 0;
                dir = 4;
            }
			// left
            else if(decision == 0) {
                step_x = stepSize;
                step_y = 0;
                dir = 2;
            }
        }
        // West or -X axis
        else if(dir == 4) {
			// right
            if(decision == 1) {
                step_x = 0;
                step_y = stepSize;
                dir = 1;
            }
			// left
            else if(decision == 0) {
                step_x = 0;
                step_y = -stepSize;
                dir = 3;
            }
        }
    }

    @Override
    protected void move() {
       position.setLoc(position.getX() + step_x, position.getY() + step_y);
    }

	@Override
	public String name() {
		return name;
	}

	@Override
	public Location getPosition() {
		return position;
	}
}

// randomly select t machines
class selectyMachines {
    private Set<Integer> faultList;
    private Random rand;
    private int noOfMachines;

    selectyMachines(int noOfMachines) {
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