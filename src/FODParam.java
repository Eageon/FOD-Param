import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class FODParam {

	GraphicalModel model = null;
	LinkedList<Evidence> evidenceSet = null;
	int numEvidence = 0;

	double logLikelihood = 0.0;

	public FODParam(GraphicalModel model) {
		this.model = model;
	}
	
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
				
				if(PrD_x_given_u == 0.0 || numerator == 0) {
					continue;
				}
				
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

	public double testLikelihoodOnFile(String test_data) {
		double logLikelihood = -1.0;
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(test_data));
			String preamble = reader.readLine();
			String[] tokens = preamble.split(" ");
			
			if(model.variables.size() != Integer.valueOf(tokens[0])) {
				System.out.println("uai and test data don't match on number of variables");
				System.exit(0);
			}
			
			@SuppressWarnings("unused")
			int numData = Integer.valueOf(tokens[1]);
			logLikelihood = testLikelihoodOnData(reader);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return logLikelihood;
	}
	
	public double testLikelihoodOnData(BufferedReader reader) {
		String line = null;
		double logLikelihood = 0.0;

		try {
			while (null != (line = reader.readLine())) {
				String[] values = line.split(" ");

				Evidence evidence = new Evidence(model.variables);
				evidence.setData(values);
				evidence.makeEvidenceBeTrue();

				double result = 1.0;
				for (Factor factor : model.factors) {
					result *= factor.underlyProbability();
				}

				if(result == 0.0) {
					continue;
				}
				
				logLikelihood += Math.log(result) / Math.log(2);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return logLikelihood;
	}
	
	public void readTrainingDataOnFile(String training_data) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(training_data));
			String preamble = reader.readLine();
			String[] tokens = preamble.split(" ");
			
			if(model.variables.size() != Integer.valueOf(tokens[0])) {
				System.out.println("uai and training data don't match on number of variables");
				System.exit(0);
			}
			
			numEvidence = Integer.valueOf(tokens[1]);
			readEvidenceSet(reader);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (4 != args.length) {
			System.out
					.println("java -jar FODParam <input-uai-file> <training-data> <test-data> <output-uai-file>");
			System.exit(-1);
		}
		
		String input_uai = args[0];
		String training_data = args[1];
		String test_data = args[2];
		String output_uai = args[3];

		GraphicalModel model = new GraphicalModel(input_uai, false);
		model.initTabelWithoutSettingValue();
		FODParam fodParam = new FODParam(model);
		
		fodParam.readTrainingDataOnFile(training_data);
		fodParam.runFODParam();
		
		double logLikelihood = fodParam.testLikelihoodOnFile(test_data);
		
		//FileOutputStream output = new FileOutputStream(output_uai);
		System.out.println("____________________________");
		System.out.println("log likelihood difference = " + logLikelihood);
		System.out.println("____________________________");
	}

}
