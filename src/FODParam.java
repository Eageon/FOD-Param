import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;


public class FODParam {
	
	GraphicalModel model = null;
	LinkedList<Evidence> evidenceSet = null;

	public void initializeParam(GraphicalModel model) {
		this.model = model;
		//this.evidenceSet = evidenceSet;
	}
	
	public void readEvidenceSet(BufferedReader reader) {
		evidenceSet = new LinkedList<>();
		
		String line = null;
		try {
			while(null != (line = reader.readLine())) {
				String[] observed = line.split(" ");
				Evidence evidence = new Evidence(model.variables);
				evidence.setData(observed);
				evidenceSet.add(evidence);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public GraphicalModel runFODParam() {
		// for each parameter, that is factor
		for (int i = 0; i < model.factors.size(); i++) {
			Factor factor = model.getFactor(i);
			for (int j = 0; j < factor.table.size(); j++) {
				int[] values = factor.tableIndexToVaraibleValue(j);
				
				int numerator = 0;
				int denominator = 0;
				
				for (Evidence e : evidenceSet) {
					if(e.isConsistentWith(factor.variables, values, 0, factor.variables.size())) {
						numerator++;
					}
					if(e.isConsistentWith(factor.variables, values, 0, factor.variables.size() - 1)) {
						denominator++;
					}
				}
				
				if (denominator == 0) {
					factor.setTableValue(j, 0.0);
					continue;
				}
				
				factor.setTableValue(j, ((double)numerator / (double)denominator));
			}
		}
		
		return model;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
