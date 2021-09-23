package fuzzexperiment.runner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import fuzzexperiment.runner.metrics.Metric;
import fuzzexperiment.runner.metrics.OfflineMetric;
import fuzzingengine.exptgenerator.ListHasNoElement;

public class FuzzingPopulation {
	public List<FuzzingExptResult> pop = new ArrayList<FuzzingExptResult>();

	private int sizeLimit;
	Random rng = new Random();

	public FuzzingPopulation(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

	private void printPop(List<FuzzingExptResult> res) {
		for (FuzzingExptResult r : res) {
			System.out.println(r.toString());
		}
	}

	public void print() {
		printPop(pop);
	}

//	public Optional<FuzzingExptResult> pickPopulationElementToExplore() {
//		// Select one in the top x% of the population...
//		List<FuzzingExptResult> sortedPop = pop.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
//		
//		
//		
//		// Print the population
//		int elementSelection = rng.nextInt(Math.min(sortedPop.size(), topNumToSelect));
//		FuzzingExptResult picked = sortedPop.get(elementSelection);
//		System.out.println("pickPopulationElementToExplore - looking for highest");
//		System.out.print("sortedPop=");
//		printPop(sortedPop);
//		System.out.println("elementSelection (from highest)=" + elementSelection);
//		System.out.println("picked=" + picked);
//		return Optional.of(picked);
//	}

	protected <E> E selectRandomElementFrom(List<E> l, String what) throws ListHasNoElement {
		int length = l.size();
		if (length == 0) {
			throw new ListHasNoElement(what);
		} else {
			int i = rng.nextInt(length);
			return l.get(i);
		}
	}

	public Optional<FuzzingExptResult> pickPopulationElementToExplore() {
		List<ArrayList<FuzzingExptResult>> rankings = computeRankings();
		
		if (rankings.size() < 1) {
			return Optional.empty();
		} else {
			// Find one of the non-dominated solutions to explore
			List<FuzzingExptResult> nondominatedSolutions = rankings.get(0);
			try {
				FuzzingExptResult e = selectRandomElementFrom(nondominatedSolutions, "nondominatedSolutions in pickPopulationElementToExplore");
				return Optional.of(e);
			} catch (ListHasNoElement e) {
				return Optional.empty();
			}
		}
	}
	
	public void findElementToRemove() {
		// Find the non-dominated solutions
		List<ArrayList<FuzzingExptResult>> ranks = computeRankings();
		int rankCount = ranks.size();
		boolean found = false;
		
		// Start from the lowest ranked element to remove from the population
		for (int i = rankCount - 1; i >= 0; i--) {
			if (!found) {
				try {
					List<FuzzingExptResult> ranked = ranks.get(i);
					FuzzingExptResult forRemoval = selectRandomElementFrom(ranked, "ranked " + i);;
					System.out.println("Looking for element to remove in rank " + i);
					System.out.println("Found element to remove in the population = " + forRemoval);
					pop.remove(forRemoval);
					found = true;
				} catch (ListHasNoElement e) {
					System.out.println("No element found in rank " + i);
				}
			}
		}
	}

	public void pushToPopulation(FuzzingExptResult newResult) {
		// Add the new element to the population
		pop.add(newResult);
		// If the population size is greater than the size limit...
		if (pop.size() > sizeLimit) {
			findElementToRemove();
		}
	}

	public int currentSize() {
		return pop.size();
	}
	
	public void logPopulation(FileWriter file) throws IOException {
		for (FuzzingExptResult r : pop) {
				file.write(r.toStringWithSpec() + "\n");
		}
		file.write("==============================================================================================================\n");
	}

	// https://github.com/jMetal/jMetal/blob/9a68b6878a1d7429fb1a0be87f84ec6faa05fa8c/jmetal-core/src/main/java/org/uma/jmetal/util/ranking/impl/FastNonDominatedSortRanking.java
	public List<ArrayList<FuzzingExptResult>> computeRankings() {
		List<ArrayList<FuzzingExptResult>> rankedSubPopulations;
		List<FuzzingExptResult> population = new ArrayList<FuzzingExptResult>(pop);

		// dominateMe[i] contains the number of population dominating i
		int[] dominateMe = new int[population.size()];

		// iDominate[k] contains the list of population dominated by k
		List<List<Integer>> iDominate = new ArrayList<>(population.size());

		// front[i] contains the list of individuals belonging to the front i
		ArrayList<List<Integer>> front = new ArrayList<>(population.size() + 1);

		// Initialize the fronts
		for (int i = 0; i < population.size() + 1; i++) {
			front.add(new LinkedList<Integer>());
		}

		// Fast non dominated sorting algorithm
		// Contribution of Guillaume Jacquenot
		for (int p = 0; p < population.size(); p++) {
			// Initialize the list of individuals that i dominate and the number
			// of individuals that dominate me
			iDominate.add(new LinkedList<Integer>());
			dominateMe[p] = 0;
		}

		int flagDominate = 0;
		for (int p = 0; p < (population.size() - 1); p++) {
			// For all q individuals , calculate if p dominates q or vice versa
			for (int q = p + 1; q < population.size(); q++) {
				// flagDominate =
				// CONSTRAINT_VIOLATION_COMPARATOR.compare(population.get(p),
				// population.get(q));
				// if (flagDominate == 0) {
				// flagDominate = dominanceComparator.compare(population.get(p),
				// population.get(q));
				// flagDominate = population.get(p).compareTo(population.get(q));
				flagDominate = population.get(q).compareTo(population.get(p));
				//System.out.println(flagDominate);
				// }
				if (flagDominate == -1) {
					iDominate.get(p).add(q);
					dominateMe[q]++;
				} else if (flagDominate == 1) {
					iDominate.get(q).add(p);
					dominateMe[p]++;
				}
			}
		}

		for (int i = 0; i < population.size(); i++) {
			if (dominateMe[i] == 0) {
				front.get(0).add(i);
			}
		}

		// Obtain the rest of fronts
		int i = 0;
		Iterator<Integer> it1, it2; // Iterators
		while (front.get(i).size() != 0) {
			i++;
			it1 = front.get(i - 1).iterator();
			while (it1.hasNext()) {
				it2 = iDominate.get(it1.next()).iterator();
				while (it2.hasNext()) {
					int index = it2.next();
					dominateMe[index]--;
					if (dominateMe[index] == 0) {
						front.get(i).add(index);
					}
				}
			}
		}

		rankedSubPopulations = new ArrayList<>();
		// 0,1,2,....,i-1 are fronts, then i fronts
		for (int j = 0; j < i; j++) {
			rankedSubPopulations.add(j, new ArrayList<FuzzingExptResult>(front.get(j).size()));
			it1 = front.get(j).iterator();
			while (it1.hasNext()) {
				rankedSubPopulations.get(j).add(population.get(it1.next()));
			}
		}
		return rankedSubPopulations;
	}

	public void logFinalPopulation(FileWriter outLog, List<OfflineMetric> ms, boolean onlyNonDom) throws IOException {
		String firstCol = "ID";
		int id = 1;
		
		outLog.write(firstCol + ",");
		for (Metric m : ms) {
			outLog.write(m.getClass().getSimpleName() + ",");
		}
		
		outLog.write("\n");
		
		List<FuzzingExptResult> chosen;
		if (onlyNonDom) {
			List<ArrayList<FuzzingExptResult>> rankings = computeRankings();
			if (rankings.size() > 0) {
				chosen = rankings.get(0);
			} else {
				// If there are no non-dom solutions, chosen is empty
				chosen = new ArrayList<>();
			}
		} else {
			chosen = pop;
		}
		
		for (FuzzingExptResult r : chosen) {
			outLog.write(String.valueOf(id) + ",");
			id++;
			Map<Metric,Double> metrics = r.getMetrics();
			for (Metric m : ms) {
				outLog.write(metrics.get(m) + ",");
			}
			outLog.write("\n");
		}
	}
}
