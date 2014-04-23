import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;

public class FODParam {

	GraphicalModel model = null;
	LinkedList<Evidence> evidenceSet = null;

	double logLikelihood = 0.0;

	public void initializeParam(GraphicalModel model) {
		this.model = model;
		// this.evidenceSet = evidenceSet;
	}

	public void readEvidenceSet(BufferedReader reader) {
		evidenceSet = new LinkedList<>();

		String line = null;
		try {
			while (null != (line = reader.readLine())) {
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
			double entrophy = 0.0;

			for (int j = 0; j < factor.table.size(); j++) {
				int[] values = factor.tableIndexToVaraibleValue(j);

				int numerator = 0;
				int denominator = 0;

				for (Evidence e : evidenceSet) {
					if (e.isConsistentWith(factor.variables, values, 0,
							factor.variables.size())) {
						numerator++;
					}
					if (e.isConsistentWith(factor.variables, values, 0,
							factor.variables.size() - 1)) {
						denominator++;
					}
				}

				if (denominator == 0) {
					factor.setTableValue(j, 0.0);
					continue;
				}

				double PrD_x_and_u = (double) numerator / evidenceSet.size();
				double PrD_x_given_u = ((double) numerator / (double) denominator);
				factor.setTableValue(j, PrD_x_given_u);
				entrophy -= PrD_x_and_u
						* (Math.log(PrD_x_given_u) / Math.log(2));
			}

			logLikelihood -= entrophy;
		}

		logLikelihood *= evidenceSet.size();

		return model;
	}

	public double logLikelihood(LinkedList<Evidence> dataSet) {

		return logLikelihood;
	}
	
	public double testLikelihoodOnData(BufferedReader reader) {
		String line = null;
		double logLikelihood = 0.0;
		
		try {
			while(null != (line = reader.readLine())) {
				String[] values = line.split(" ");
				
				Evidence evidence = new Evidence(model.variables);
				evidence.setData(values);
				evidence.makeEvidenceBeTrue();
				
				double result = 1.0;
				for (Factor factor : model.factors) {
					result *= factor.underlyProbability();
				}
				
				logLikelihood += Math.log(result) / Math.log(2);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return logLikelihood;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
