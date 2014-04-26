import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class FODParam {

	GraphicalModel model = null;
	GraphicalModel origModel = null;
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

				if (PrD_x_given_u == 0.0 || numerator == 0) {
					continue;
				}

				entrophy -= PrD_x_and_u
						* (Math.log(PrD_x_given_u) / Math.log(2));
			}

			logLikelihood -= entrophy;
		}

		// logLikelihood *= evidenceSet.size();

		return model;
	}

	public double logLikelihood(LinkedList<Evidence> dataSet) {

		return logLikelihood;
	}

	public double testLikelihoodOnFileAndCompare(String test_data) {
		double logLikelihoodDiff = 0.0;

		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(test_data));
			String preamble = reader.readLine();
			String[] tokens = preamble.split(" ");

			if (model.variables.size() != Integer.valueOf(tokens[0])) {
				System.out
						.println("uai and test data don't match on number of variables");
				System.exit(0);
			}

			int numData = Integer.valueOf(tokens[1]);
			logLikelihoodDiff = testLikelihoodOnDataAndCompare(reader);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return logLikelihoodDiff;
	}

	public double testLikelihoodOnDataAndCompare(BufferedReader reader) {
		String line = null;
		double logLikelihoodDiff = 0.0;

		try {
			while (null != (line = reader.readLine())) {
				String[] values = line.split(" ");

				Evidence evidence = new Evidence(model.variables);
				Evidence origEvidence = new Evidence(origModel.variables);
				evidence.setData(values);
				origEvidence.setData(values);
				evidence.makeEvidenceBeTrue();
				origEvidence.makeEvidenceBeTrue();

				double result = 1.0;
				for (Factor factor : model.factors) {
					result *= factor.underlyProbability();
				}

				if (result == 0.0) {
					continue;
				}

				double origResult = 1.0;

				for (Factor factor : origModel.factors) {
					origResult *= factor.underlyProbability();
				}

				if (origResult == 0.0) {
					continue;
				}

				double LLo = Math.log(origResult) / Math.log(2);
				double LLl = Math.log(result) / Math.log(2);
				logLikelihoodDiff += Math.abs(LLo - LLl);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return logLikelihoodDiff;
	}

	public double testLikelihoodOnFile(String test_data) {
		double logLikelihood = -1.0;

		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(test_data));
			String preamble = reader.readLine();
			String[] tokens = preamble.split(" ");

			if (model.variables.size() != Integer.valueOf(tokens[0])) {
				System.out
						.println("uai and test data don't match on number of variables");
				System.exit(0);
			}

			int numData = Integer.valueOf(tokens[1]);
			logLikelihood = testLikelihoodOnData(reader);

		} catch (IOException e) {
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

				if (result == 0.0) {
					continue;
				}

				logLikelihood += Math.log(result) / Math.log(2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return logLikelihood;
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
			e.printStackTrace();
		}
	}

	public void readTrainingDataOnFile(String training_data) {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(training_data));
			String preamble = reader.readLine();
			String[] tokens = preamble.split(" ");

			if (model.variables.size() != Integer.valueOf(tokens[0])) {
				System.out
						.println("uai and training data don't match on number of variables");
				System.exit(0);
			}

			numEvidence = Integer.valueOf(tokens[1]);
			readEvidenceSet(reader);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void dumpNetworkAsUAI(String output_uai) {
		PrintStream writer = null;
		try {
			writer = new PrintStream(output_uai);
			writer.println("BAYES");
			// number of variables
			writer.println(model.variables.size());
			// domain size of variables
			for (int i = 0; i < model.variables.size(); i++) {
				writer.print(model.getVariable(i).domainSize());
				if (i != model.variables.size() - 1) {
					writer.print(" ");
				}
			}
			writer.println();

			// number of factors
			writer.println(model.factors.size());
			// scope of factors
			for (int i = 0; i < model.factors.size(); i++) {
				Factor factor = model.getFactor(i);
				for (int j = 0; j < factor.variables.size(); j++) {
					writer.print(factor.getVariable(j).index);
					if (i != factor.variables.size() - 1) {
						writer.print(" ");
					}
				}
				writer.println();
			}
			writer.println();

			DecimalFormat roundFormat = new DecimalFormat("#.########");
			// CPTs
			for (int i = 0; i < model.factors.size(); i++) {
				Factor factor = model.getFactor(i);
				int domainSize = factor.getNodeVariable().domainSize();
				writer.println(roundFormat.format(factor.table.size()));
				for (int j = 0; j < factor.table.size(); j++) {
					writer.print(factor.getTabelValue(j));
					if ((j % domainSize) == (domainSize - 1)) {
						writer.println();
					} else {
						writer.print(" ");
					}
				}
				writer.println();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}

	public static void main(String[] args) {

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
		GraphicalModel origModel = new GraphicalModel(input_uai, true);
		model.initTabelWithoutSettingValue();
		FODParam fodParam = new FODParam(model);

		fodParam.readTrainingDataOnFile(training_data);
		fodParam.runFODParam();
		
		fodParam.origModel = origModel;
		double logLikelihoodDiff = fodParam.testLikelihoodOnFileAndCompare(test_data);

		// FileOutputStream output = new FileOutputStream(output_uai);
		System.out.println("______________________________________________________");
		System.out.println("log likelihood difference = " + logLikelihoodDiff);
		System.out.println("______________________________________________________");

		fodParam.dumpNetworkAsUAI(output_uai);
	}
}
