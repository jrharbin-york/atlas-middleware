package test.jmetal;

import org.uma.jmetal.util.comparator.DominanceComparator;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;

import exptrunner.jmetal.FaultInstanceSetSolution;

public class TestDominance {
	public static void main(String [] args) {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
		
			FaultInstanceSetSolution s1 = new FaultInstanceSetSolution(mission, "expt", false, 1200);
			FaultInstanceSetSolution s2 = new FaultInstanceSetSolution(mission, "expt", false, 1200);;
		
			s1.setObjective(0, 3);
			s1.setObjective(1, 4);
		
			s2.setObjective(0, 0);
			s2.setObjective(1, 1);
			
			//DominanceComparator<FaultInstanceSetSolution> dc = new DominanceComparator<FaultInstanceSetSolution>();
			DominanceComparator dc = new DominanceComparator();
			System.out.println(dc.compare(s1, s2));
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		}
	}
}
