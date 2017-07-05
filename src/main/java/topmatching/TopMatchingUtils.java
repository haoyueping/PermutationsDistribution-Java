package topmatching;

import java.util.ArrayList;
import java.util.HashMap;

import general.GeneralUtils;
import pattern.Graph;
import pattern.Node;
import pattern.PatternUtils;

public class TopMatchingUtils {

	private static TopMatchingArgs topMatchingArgs;

	// This variable will serve all assignments, so we need to create it only
	// once per a given graph
	private static ArrayList<ArrayList<String>> allPossibleLabelsOrders;

	public static void init(TopMatchingArgs topMatchingArgsInput) {
		topMatchingArgs = topMatchingArgsInput;

		ArrayList<ArrayList<ArrayList<String>>> labelsPerLevel = getInitialLabelsOrders(topMatchingArgs.getG());
		allPossibleLabelsOrders = new ArrayList<>();

		createPossibleOrders(new ArrayList<String>(), allPossibleLabelsOrders, labelsPerLevel);
	}

	/**
	 * 
	 * Get all the possible permutations within level where the order between
	 * levels is dictated by the graph
	 * 
	 */
	private static ArrayList<ArrayList<ArrayList<String>>> getInitialLabelsOrders(Graph graph) {
		ArrayList<ArrayList<ArrayList<String>>> result = new ArrayList<>();
		ArrayList<ArrayList<String>> labelsPerLevel = PatternUtils.getLabelsPerLevel(graph);
		for (int i = 0; i < labelsPerLevel.size(); i++) {
			result.add(GeneralUtils.generatePermutations(labelsPerLevel.get(i)));
		}
		return result;
	}

	private static void createPossibleOrders(ArrayList<String> current, ArrayList<ArrayList<String>> result,
			ArrayList<ArrayList<ArrayList<String>>> labelsPerLevel) {
		if (labelsPerLevel.isEmpty()) {
			result.add(current);
			return;
		}
		ArrayList<ArrayList<String>> currentElementsToAdd = labelsPerLevel.get(0);
		for (int i = 0; i < currentElementsToAdd.size(); i++) {
			ArrayList<String> newCurrent = new ArrayList<>(current);
			newCurrent.addAll(currentElementsToAdd.get(i));
			ArrayList<ArrayList<ArrayList<String>>> subList = new ArrayList<>(
					labelsPerLevel.subList(1, labelsPerLevel.size()));
			createPossibleOrders(newCurrent, result, subList);
		}
	}

	public static DeltasContainer getInitialDeltas(TopProbArgs topProbArgs) {
		boolean optimize = Boolean.parseBoolean(GeneralUtils.properties.getProperty("enhanced_initial_deltas"));
		
		DeltasContainer result = new DeltasContainer();

		if (optimize) {
			// Build dependencies graph
			HashMap<String, Node> sigmaToNodeMap = new HashMap<>();
			
			
			
		} else {
			ArrayList<String> allLabels = new ArrayList<>(topProbArgs.getGamma().keySet());
			ArrayList<ArrayList<String>> possibleOrders = GeneralUtils.generatePermutations(allLabels);
			for (ArrayList<String> possibleOrder : possibleOrders) {
				Delta newDelta = new Delta(possibleOrder, topProbArgs.getGamma());
				if (result.getDelta(newDelta) == null) {
					result.addDelta(newDelta);
				}
			}
		}

		return result;
	}

}
