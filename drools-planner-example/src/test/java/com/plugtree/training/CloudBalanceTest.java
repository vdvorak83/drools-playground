package com.plugtree.training;

import org.drools.planner.config.SolverFactory;
import org.drools.planner.config.XmlSolverFactory;
import org.drools.planner.core.Solver;
import org.drools.planner.core.event.BestSolutionChangedEvent;
import org.drools.planner.core.event.SolverEventListener;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.buildin.hardandsoft.HardAndSoftScore;
import org.drools.planner.examples.cloudbalancing.domain.CloudBalance;
import org.drools.planner.examples.cloudbalancing.domain.CloudComputer;
import org.drools.planner.examples.cloudbalancing.domain.CloudProcess;
import org.drools.planner.examples.cloudbalancing.persistence.CloudBalancingGenerator;
import org.junit.Test;

public class CloudBalanceTest {

    @Test
	public void testExecution() {
        	
        	
        	
		// Build the Solver
		SolverFactory solverFactory = new XmlSolverFactory("/config/solverConfig.xml");
		Solver solver = solverFactory.buildSolver();
		// Load a problem with 400 computers and 1200 processes
		CloudBalance unsolvedCloudBalance = new CloudBalancingGenerator().createCloudBalance(400, 1200);
		// Set the problem
		solver.setPlanningProblem(unsolvedCloudBalance);
		// Register to monitor hard and soft score changes
		solver.addEventListener(new SolverEventListener() {
			@Override
			public void bestSolutionChanged(BestSolutionChangedEvent event) {
				Score<?> score = event.getNewBestSolution().getScore();
				HardAndSoftScore hsScore = (HardAndSoftScore) score;
				System.out.println("Score change: hard = " + hsScore.getHardScore() 
						+ ", soft = " + hsScore.getSoftScore());
			}
		});
		// Solve the problem 
		solver.solve();
		CloudBalance solvedCloudBalance = (CloudBalance) solver.getBestSolution();
		// Display the result
		System.out.println("\nSolved cloudBalance with 400 computers and 1200 processes:\n"
			+ toDisplayString(solvedCloudBalance));
	}
        
    private String toDisplayString(CloudBalance balance) {
    	StringBuilder displayString = new StringBuilder();
        for (CloudProcess process : balance.getProcessList()) {
            CloudComputer computer = process.getComputer();
            displayString.append("  ").append(process.getLabel()).append(" -> ")
                    .append(computer == null ? null : computer.getLabel()).append("\n");
        }
        return displayString.toString();
    }

}
