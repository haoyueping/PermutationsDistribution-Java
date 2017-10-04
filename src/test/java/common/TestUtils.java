package common;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import general.Distribution;
import general.GeneralUtils;
import general.Mallows;
import general.main.AlgorithmType;
import general.main.GeneralArgs;
import general.main.GeneralArgs.ScenarioSettings;
import liftedtopmatching.LiftedTopMatchingAlgorithm;
import pattern.Graph;
import pattern.GraphGenerator;
import stats.Stats;
import topmatching.SimpleDistribution;
import topmatching.TopMatchingAlgorithm;

public class TestUtils {

	private static final Logger logger = Logger.getLogger(TestUtils.class.getName());

	public static ArrayList<String> summaries = new ArrayList<>();

	private static double Epsilon = 10e-6;

	private static final String TOP_MATCHNING = "Top Matching";

	private static final String LIFTED_TOP_MATCHNING = "Lifted Top Matching";

	private static double[] exactProbs;

	static {
		exactProbs = new double[] { 0.990541370714296, 0.989000393257776, 0.732986262312703, 0.719424460431655,
				0.761006046617952, 0.934812879701345, 0.9498882307361549, 1.0, 0.6937277499487445, 0.503103,
				0.9741246606894051, 0.567747, 0.862931, 0.028696, 0.731592, 0.900444, 0.994654, 0.765118 };
	}

	public static boolean runTest(int graphId, int numItems, boolean runMultiThread, int numOfThreads,
			AlgorithmType algorithmType) {
		GeneralArgs.runMultiThread = runMultiThread;
		GeneralArgs.numOfThreads = numOfThreads;

		Graph graph = GraphGenerator.GetGraph(graphId);

		Mallows model = new Mallows(GeneralUtils.getItems(numItems), 0.3);

		double exactProb = exactProbs[graphId - 1];

		Distribution distribution = new SimpleDistribution(model);

		double prob = 0.0;
		switch (algorithmType) {
		case TOP_MATCHNING:
			prob = new TopMatchingAlgorithm().calculateProbability(graph, distribution);
			break;
		case LIFTED_TOP_MATCHING:
			prob = new LiftedTopMatchingAlgorithm().calculateProbability(graph, distribution);
			break;
		default:
		}
		return Math.abs(exactProb - prob) < Epsilon;
	}

	public static boolean runLiftedTopMatchingRandomTest(Graph graph, int numItems, int numOfLabels, int scenario,
			boolean runMultiThread, int numOfThreads) {
		GeneralArgs.runMultiThread = runMultiThread;
		GeneralArgs.numOfThreads = numOfThreads;

		StringBuilder summary = new StringBuilder(
				String.format("(Labels,Items): (%d,%d), Top Matching: ", numOfLabels, numItems));

		Mallows model = new Mallows(GeneralUtils.getItems(numItems), 0.3);

		ScenarioSettings scenarioSettings = new ScenarioSettings(scenario, numItems, numOfLabels, runMultiThread,
				numOfThreads);

		Stats stats = new Stats(scenarioSettings, graph.toString());

		// TopMatching
		stats.setAlgorithm(TOP_MATCHNING);

		Distribution simpleDistribution = new SimpleDistribution(model);

		stats.setStartTimeDate(new Date());

		logger.info(String.format("Running top-matchnig algorithm for a random scenario %d with %d labels and %d items",
				scenario, numOfLabels, numItems));

		double topMatchingProb = new TopMatchingAlgorithm().calculateProbability(graph, simpleDistribution);

		stats.setEndTimeDate(new Date());

		summary.append(String.format("%f (%s MS), ", topMatchingProb,
				(stats.getEndTimeDate().getTime() - stats.getStartTimeDate().getTime())));

		stats.setProbability(topMatchingProb);

		System.out.println(stats);

		// LiftedTopMatching
		stats = new Stats(scenarioSettings, graph.toString());

		stats.setAlgorithm(LIFTED_TOP_MATCHNING);

		stats.setStartTimeDate(new Date());

		logger.info(String.format(
				"Running lifted-top-matchnig algorithm for a random scenario %d with %d labels and %d items", scenario,
				numOfLabels, numItems));

		double liftedTopMatchingProb = new LiftedTopMatchingAlgorithm().calculateProbability(graph, simpleDistribution);

		stats.setEndTimeDate(new Date());

		summary.append(String.format("Lifted Top Matching: %f (%s MS)", liftedTopMatchingProb,
				(stats.getEndTimeDate().getTime() - stats.getStartTimeDate().getTime())));

		if (Math.abs(liftedTopMatchingProb - 0.000000) > 10e-6 && Math.abs(liftedTopMatchingProb - 1.000000) > 10e-6) {
			summaries.add(summary.toString());
		} else {
			summaries.add("NI");
		}

		stats.setProbability(liftedTopMatchingProb);

		System.out.println(stats);

		return Math.abs(topMatchingProb - liftedTopMatchingProb) < Epsilon;

	}

}
