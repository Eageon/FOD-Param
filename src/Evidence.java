import java.util.ArrayList;

public class Evidence {
	ArrayList<Variable> varRef;
	ArrayList<Integer> observedData; // -1 means missing data
	ArrayList<Variable> missingVariables;
	
	boolean isFullyObserved = false;

	public Evidence(ArrayList<Variable> vars) {
		varRef = vars;
		observedData = new ArrayList<>(vars.size());

		for (int i = 0; i < vars.size(); i++) {
			observedData.add(-1);
		}
	}

	public int setData(String[] tokens) {
		int observedCount = 0;
		isFullyObserved = true;

		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			if (token.equals("?")) {
				observedData.set(i, -1);
				missingVariables.add(varRef.get(i));
				isFullyObserved = false;
			} else {
				observedData.set(i, Integer.valueOf(token));
				observedCount++;
			}
		}

		return observedCount;
	}

	/**
	 * 
	 * @param vars
	 *            should not be set be x and u instantiation while execute this
	 *            fucntion
	 * @param vals
	 *            instantiation values of vars
	 * @return
	 */
	public boolean isCollision(ArrayList<Variable> vars, int[] vals) {
		if (vars.size() != vals.length) {
			return true;
		}

		for (int i = 0; i < vals.length; i++) {
			if (varRef.contains(vars.get(i)) && vars.get(i).value != vals[i]) {
				return true;
			}
		}

		return false;
	}

	public boolean isConsistentWith(ArrayList<Variable> vars, int[] vals) {
		if (vals.length != vars.size()) {
			return false;
		}

		for (int i = 0; i < vals.length; i++) {
			Variable var = vars.get(i);
			if (vals[i] != varRef.get(var.index).value) {
				return false;
			}
		}

		return true;
	}

	public boolean isConsistentWith(ArrayList<Variable> vars, int[] vals,
			int offset, int length) {
		if (vals.length != vars.size()) {
			return false;
		}
		
		for (int i = offset; i < length; i++) {
			Variable var = vars.get(i);
			if (vals[i] != varRef.get(var.index).value) {
				return false;
			}
		}
		
		return false;
	}
	
	// set ref variables to the corresponding value of observedData
	public boolean makeEvidenceBeTrue() {
		if(!isFullyObserved) {
			return false;
		}
		
		for (int i = 0; i < varRef.size(); i++) {
			varRef.get(i).setSoftEvidence(observedData.get(i));
		}
		
		return true;
	}
}
