/*******************************************************************************
 * Copyright (C) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.matsim.contrib.freight.vrp.algorithms.rr.recreation;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.matsim.contrib.freight.vrp.algorithms.rr.RRSolution;
import org.matsim.contrib.freight.vrp.algorithms.rr.VRPTestCase;
import org.matsim.contrib.freight.vrp.algorithms.rr.tourAgents.DistribJIFFactory;
import org.matsim.contrib.freight.vrp.algorithms.rr.tourAgents.JobOfferMaker;
import org.matsim.contrib.freight.vrp.algorithms.rr.tourAgents.LocalMCCalculator;
import org.matsim.contrib.freight.vrp.algorithms.rr.tourAgents.LocalMCCalculatorFactory;
import org.matsim.contrib.freight.vrp.algorithms.rr.tourAgents.RRDriverAgent;
import org.matsim.contrib.freight.vrp.algorithms.rr.tourAgents.RRTourAgentFactory;
import org.matsim.contrib.freight.vrp.basics.DriverCostFunction;
import org.matsim.contrib.freight.vrp.basics.Job;
import org.matsim.contrib.freight.vrp.basics.Shipment;
import org.matsim.contrib.freight.vrp.basics.VehicleRoutingProblem;
import org.matsim.contrib.freight.vrp.basics.VrpTourBuilder;

public class BestInsertionTest extends VRPTestCase{
	BestInsertion bestInsertion;
	
	VehicleRoutingProblem vrp;
	
	RRDriverAgent tourAgent1;
	
	RRDriverAgent tourAgent2;
	
	RRSolution solution;
	
	List<Job> unassignedJobs;
	
	
	@Override
	public void setUp(){
		
		initJobsInPlainCoordinateSystem();

		vrp = getVRP(2,2);
		
		VrpTourBuilder tourBuilder = new VrpTourBuilder();
		Shipment s1 = createShipment("1", makeId(0,10), makeId(0,0));
		
		tourBuilder.scheduleStart(makeId(0,0), 0.0, 0.0);
		tourBuilder.schedulePickup(s1);
		tourBuilder.scheduleDelivery(s1);
		tourBuilder.scheduleEnd(makeId(0,0), 0.0, Double.MAX_VALUE);
		RRTourAgentFactory rrTourAgentFactory = new RRTourAgentFactory(tourStatusProcessor, 
				vrp.getCosts().getCostParams(), new JobOfferMaker(vrp.getCosts(), constraints, new DistribJIFFactory(new LocalMCCalculatorFactory())));

		tourAgent1 = rrTourAgentFactory.createTourAgent(tourBuilder.build(), vrp.getVehicles().iterator().next());
		
		VrpTourBuilder anotherTourBuilder = new VrpTourBuilder();
		Shipment s2 = createShipment("2", makeId(10,0), makeId(0,0));
		anotherTourBuilder.scheduleStart(makeId(0,0), 0.0, 0.0);
		anotherTourBuilder.schedulePickup(s2);
		anotherTourBuilder.scheduleDelivery(s2);
		anotherTourBuilder.scheduleEnd(makeId(0,0), 0.0, Double.MAX_VALUE);
		tourAgent2 = rrTourAgentFactory.createTourAgent(anotherTourBuilder.build(), vrp.getVehicles().iterator().next());
		
		Collection<RRDriverAgent> agents = new ArrayList<RRDriverAgent>();
		agents.add(tourAgent1);
		agents.add(tourAgent2);
		solution = new RRSolution(agents);
		
		bestInsertion = new BestInsertion();
		unassignedJobs = new ArrayList<Job>();
		
		Shipment s3 = createShipment("3", makeId(0,5), makeId(2,10));
		unassignedJobs.add(s3);
	}
	

	public void testSizeOfNewSolution(){
		bestInsertion.run(solution, unassignedJobs, Double.MAX_VALUE);
		assertEquals(2, solution.getTourAgents().size());
	}
	
	public void testNuOfActivitiesOfAgent1(){
		bestInsertion.run(solution, unassignedJobs, Double.MAX_VALUE);
		assertEquals(6, tourAgent1.getTour().getActivities().size());
	}
	
	public void testNuOfActivitiesOfAgent2(){
		bestInsertion.run(solution, unassignedJobs, Double.MAX_VALUE);
		assertEquals(4, tourAgent2.getTour().getActivities().size());
	}
	
	public void testMarginalCostOfInsertion(){
		double oldCost = solution.getResult();
		bestInsertion.run(solution, unassignedJobs, Double.MAX_VALUE);
		double newCost = solution.getResult();
		assertEquals(4.0,newCost-oldCost);
	}
	
	public void testTotalCost(){
		double oldCost = solution.getResult();
		assertEquals(240.0,oldCost);
		bestInsertion.run(solution, unassignedJobs, Double.MAX_VALUE);
		double newCost = solution.getResult();
		assertEquals(244.0,newCost);
	}
	
	
	
}
