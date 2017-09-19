package topmatching.delta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import general.main.AlgorithmType;
import general.main.GeneralArgs;

public class Delta {

	private HashMap<String, Integer> labelToIndex;

	// 0: not assigned
	// 1: assigned
	private HashMap<String, Integer> labelsState;

	private HashMap<String, HashSet<String>> lambda;

	private double probability;

	private String strForHash;

	private boolean hashIsValid;

	public Delta(ArrayList<String> order, HashMap<String, String> gamma) {

		// We build the labelToIndex to keep track of previously added sigmas
		this.labelToIndex = new HashMap<>();

		int j = 0;
		HashMap<String, Integer> sigmaToIndexMap = new HashMap<>();
		for (int i = 0; i < order.size(); i++) {
			String label = order.get(i);
			String sigma = gamma.get(label);
			if (!sigmaToIndexMap.containsKey(sigma)) {
				sigmaToIndexMap.put(sigma, ++j);
			}
			this.labelToIndex.put(label, sigmaToIndexMap.get(sigma));
		}
		this.probability = 1.0;
		this.hashIsValid = false;
	}

	public Delta(Delta other) {
		this.labelToIndex = new HashMap<>(other.labelToIndex);
		if (GeneralArgs.currentAlgorithm == AlgorithmType.BINARY_MATCHING
				|| GeneralArgs.currentAlgorithm == AlgorithmType.LIFTED_TOP_MATCHING) {
			this.labelsState = new HashMap<>(other.labelsState);
			this.lambda = new HashMap<>(other.lambda);
		}
		this.probability = other.probability;
		this.hashIsValid = false;
	}

	public Delta() {
		this.labelToIndex = new HashMap<>();
		if (GeneralArgs.currentAlgorithm == AlgorithmType.BINARY_MATCHING
				|| GeneralArgs.currentAlgorithm == AlgorithmType.LIFTED_TOP_MATCHING) {
			this.labelsState = new HashMap<>();
			this.lambda = new HashMap<>();
		}
		this.probability = 1.0;
		this.hashIsValid = false;
	}

	@Override
	public String toString() {
		if (GeneralArgs.currentAlgorithm == AlgorithmType.TOP_MATCHNING
				|| GeneralArgs.currentAlgorithm == AlgorithmType.LIFTED_TOP_MATCHING) {
			return String.format("Mapping: %s, Prob: %f", this.labelToIndex, this.probability);
		}
		return String.format("Mapping: %s, States: %s, Prob: %f", this.labelToIndex, this.labelsState,
				this.probability);
	}

	@Override
	public boolean equals(Object obj) {
		Delta delta1 = (Delta) obj;
		return getStrForHash().equals(delta1.getStrForHash());
	}

	@Override
	public int hashCode() {
		return getStrForHash().hashCode();
	}

	public void insertNewItem(int j) {
		for (String key : this.labelToIndex.keySet()) {
			if (this.labelToIndex.get(key) >= j) {
				int currentIndex = this.labelToIndex.get(key);
				this.labelToIndex.put(key, currentIndex + 1);
			}
		}
		this.hashIsValid = false;
	}

	private void createStrForHash() {
		createStrForHashAux();
		this.hashIsValid = true;
	}

	private void createStrForHashAux() {
		if (this.labelToIndex.isEmpty()) {
			this.strForHash = "";
			return;
		}
		StringBuilder sb = new StringBuilder();
		ArrayList<String> sortedKeys = new ArrayList<>(this.labelToIndex.keySet());
		Collections.sort(sortedKeys);
		for (String key : sortedKeys) {
			sb.append(key);
			sb.append(this.labelToIndex.get(key));
		}
		if (GeneralArgs.currentAlgorithm == AlgorithmType.BINARY_MATCHING
				|| GeneralArgs.currentAlgorithm == AlgorithmType.LIFTED_TOP_MATCHING) {
			sb.append("|||");
			// Add to the strForHash the state vector
			for (String key : sortedKeys) {
				sb.append(key);
				sb.append(this.labelsState.get(key));
			}
		}
		this.strForHash = sb.toString();
	}

	public int getLabelPosition(String label) {
		return this.labelToIndex.get(label) != null ? this.labelToIndex.get(label) : -1;
	}

	public int getNumOfLabels() {
		return this.labelToIndex.size();
	}

	public Set<String> getKeySet() {
		return this.labelToIndex.keySet();
	}

	public Collection<Integer> getValues() {
		return this.labelToIndex.values();
	}

	public String getLabelsToIndex() {
		return this.labelToIndex.toString();
	}

	public void putKeyValue(String key, int value) {
		this.labelToIndex.put(key, value);
		if (GeneralArgs.currentAlgorithm == AlgorithmType.BINARY_MATCHING
				|| GeneralArgs.currentAlgorithm == AlgorithmType.LIFTED_TOP_MATCHING) {
			this.labelsState.put(key, 0);
			this.hashIsValid = false;
		}
	}

	public int getLabelState(String label) {
		return this.labelsState.get(label);
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public void setStrForHash(String strForHash) {
		this.strForHash = strForHash;
	}

	public String getStrForHash() {
		if (!this.hashIsValid) {
			createStrForHash();
		}
		return this.strForHash;
	}

	// BinaryMatching section

	public void fillLambda(HashMap<String, String> gamma) {
		for (String label : gamma.keySet()) {
			String sigma = gamma.get(label);
			HashSet<String> labels = this.lambda.get(sigma);
			if (labels == null) {
				labels = new HashSet<>();
			}
			labels.add(label);
			this.lambda.put(sigma, labels);
		}
	}

	public int getNumOfDistinctNonAssignedLabels() {
		HashSet<Integer> seen = new HashSet<>();
		for (String label : this.labelsState.keySet()) {
			if (this.labelsState.get(label) == 0) {
				seen.add(this.labelToIndex.get(label));
			}
		}
		return seen.size();
	}

	public HashSet<String> getNonAssignedLabelsForSigma(String sigma) {
		HashSet<String> result = new HashSet<>();
		if (this.lambda.containsKey(sigma)) {
			for (String label : this.lambda.get(sigma)) {
				if (this.labelsState.get(label) == 0) {
					result.add(label);
				}
			}
		}
		return result;
	}

	private HashMap<Integer, HashSet<String>> getNonAssignedJsToLabelsBinaryMatching(String sigma) {
		if (!this.lambda.containsKey(sigma)) {
			return new HashMap<>();
		}

		HashMap<Integer, HashSet<String>> result = new HashMap<>();
		for (String label : this.labelsState.keySet()) {
			if (this.labelsState.get(label) == 0) {
				int j = this.labelToIndex.get(label);
				HashSet<String> labels = result.get(j);
				if (labels == null) {
					labels = new HashSet<>();
					result.put(j, labels);
				}
				labels.add(label);
			}
		}

		// Go over the sets of labels and omit the ones that are not contained in
		// sigma's lambda's set
		ArrayList<Integer> jsToRemove = new ArrayList<>();
		for (int j : result.keySet()) {
			HashSet<String> currentSet = result.get(j);
			if (!this.lambda.get(sigma).containsAll(currentSet)) {
				jsToRemove.add(j);
			}
		}

		for (int j : jsToRemove) {
			result.remove(j);
		}

		return result;
	}

	public HashMap<Integer, HashSet<String>> getNonAssignedJsToLabels(HashMap<String, HashSet<String>> globalLambda,
			String sigma) {
		return GeneralArgs.currentAlgorithm == AlgorithmType.BINARY_MATCHING
				? getNonAssignedJsToLabelsBinaryMatching(sigma)
				: getNonAssignedJsToLabelsLiftedTopMatching(globalLambda, sigma);
	}

	public int addAssignmentToLabel(String label) {
		this.labelsState.put(label, 1);
		this.hashIsValid = false;
		return this.labelToIndex.get(label);
	}

	// BinaryMatching section

	// LiftedTopMatching section

	private HashMap<Integer, HashSet<String>> getNonAssignedJsToLabelsLiftedTopMatching(
			HashMap<String, HashSet<String>> globalLambda, String sigma) {
		if (!globalLambda.containsKey(sigma)) {
			return new HashMap<>();
		}

		HashMap<Integer, HashSet<String>> result = new HashMap<>();
		for (String label : this.labelsState.keySet()) {
			if (this.labelsState.get(label) == 0) {
				int j = this.labelToIndex.get(label);
				HashSet<String> labels = result.get(j);
				if (labels == null) {
					labels = new HashSet<>();
					result.put(j, labels);
				}
				labels.add(label);
			}
		}

		// Go over the sets of labels and omit the ones that are not contained in
		// sigma's lambda's set
		ArrayList<Integer> jsToRemove = new ArrayList<>();
		for (int j : result.keySet()) {
			HashSet<String> currentSet = result.get(j);
			if (!globalLambda.get(sigma).containsAll(currentSet)) {
				jsToRemove.add(j);
			}
		}

		for (int j : jsToRemove) {
			result.remove(j);
		}

		return result;
	}

	// LiftedTopMatching section
}
