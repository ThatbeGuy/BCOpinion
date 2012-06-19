public class ThreadDelegate {
	private int runNum = 0;
	Constants constants = new Constants();
	double epsilon = constants._epsilon;

	public synchronized void threadInc(SimThread t, DataHolder d) {
		if (Constants.muCheck) {
			if ((Constants.muIncS += Constants.muIncUp) < .75) {
				t.Constants._epsilon = this.epsilon;
				t.Constants._SIM_epsilon_start = this.epsilon;
				t.Constants._SIM_epsilon_final = this.epsilon;
				t.setRunNum(runNum);
				runNum++;
				d.init(t);
			} else {
				threadCheck(t);
			}
		} else if (Constants.ConstantEp) {
			if (runNum < constants._trials) {
				t.Constants._epsilon = this.epsilon;
				t.Constants._SIM_epsilon_start = t.Constants._epsilon;
				t.Constants._SIM_epsilon_final = t.Constants._epsilon;
				t.setRunNum(runNum);
				runNum++;
				if (this.epsilon > 1) {
					this.epsilon = 1;
				}
				d.init(t);
			}
		} else if ((this.epsilon < constants._SIM_epsilon_final)) {
			t.Constants._epsilon = this.epsilon;
			t.Constants._SIM_epsilon_start = t.Constants._epsilon;
			t.Constants._SIM_epsilon_final = t.Constants._epsilon;
			this.epsilon += this.constants._SIM_epsilon_step;
			t.setRunNum(runNum);
			runNum++;
			if (this.epsilon > 1) {
				this.epsilon = 1;
			}
			d.init(t);
		} else {
			threadCheck(t);
		}
	}

	public synchronized void threadStart(SimThread t) {
		if (Constants.muCheck) {
			if ((Constants.muIncS += Constants.muIncUp) <= .95) {
				t.Constants._epsilon = this.epsilon;
				t.Constants._SIM_epsilon_start = this.epsilon;
				t.Constants._SIM_epsilon_final = this.epsilon;
				t.setRunNum(runNum);
				runNum++;
				DataHolder holder = new DataHolder();
				holder.init(t);
				t.start();
			}
		} else if (Constants.ConstantEp) {
			if (Main.threads.size() < 2) {
				t.Constants._epsilon = this.epsilon;
				t.Constants._SIM_epsilon_start = t.Constants._epsilon;
				t.Constants._SIM_epsilon_final = t.Constants._epsilon;
				t.setRunNum(runNum);
				runNum++;
				DataHolder holder = new DataHolder();
				holder.init(t);
				t.start();
			} else {
				threadCheck(t);
				t.start();
			}
		} else if (t.Constants._epsilon != constants._SIM_epsilon_final) {
			t.Constants._epsilon = this.epsilon;
			t.Constants._SIM_epsilon_start = t.Constants._epsilon;
			t.Constants._SIM_epsilon_final = t.Constants._epsilon;
			this.epsilon += this.constants._SIM_epsilon_step;
			t.setRunNum(runNum);
			runNum++;
			DataHolder holder = new DataHolder();
			holder.init(t);
			t.start();
		}
	}

	public int getRuns() {
		return runNum;
	}

	public void threadCheck(SimThread t) {
		t.deactivate();
		for (SimThread a : Main.threads) {
			if (a.active) {
				if (a.hold.Counter < a.hold.Runs && a.hold != t.hold) {
					threadHop(t, a);
					break;
				}
			}
		}
	}

	public void threadHop(SimThread hopper, SimThread to) {
		// to.hold.FreezeAll();
		hopper.Constants._epsilon = to.Constants._epsilon;
		hopper.Constants._SIM_epsilon_start = to.Constants._SIM_epsilon_start;
		hopper.Constants._SIM_epsilon_final = to.Constants._SIM_epsilon_start;
		hopper.sim = to.sim;
		hopper.hold = to.hold;
		to.hold.addThread(hopper);
		// hopper.hold.UnfreezeAll();
	}
}
