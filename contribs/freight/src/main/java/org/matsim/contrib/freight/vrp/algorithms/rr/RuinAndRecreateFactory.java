/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package org.matsim.contrib.freight.vrp.algorithms.rr;

import org.matsim.contrib.freight.vrp.basics.TourPlan;
import org.matsim.contrib.freight.vrp.basics.VehicleRoutingProblem;


public interface RuinAndRecreateFactory {

	
	public abstract RuinAndRecreate createAlgorithm(VehicleRoutingProblem vrp, RRSolution initialSolution);
	
	public abstract RuinAndRecreate createAlgorithm(VehicleRoutingProblem vrp, TourPlan initialSolution);

	public abstract void addRuinAndRecreateListener(RuinAndRecreateListener l);
	
	public abstract void setIterations(int iterations);
	
	public abstract void setWarmUp(int nOfWarmUpIterations);
	
	
	
}
