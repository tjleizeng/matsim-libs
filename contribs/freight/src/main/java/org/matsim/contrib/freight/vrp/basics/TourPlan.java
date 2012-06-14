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
package org.matsim.contrib.freight.vrp.basics;

import java.util.Collection;

public class TourPlan {
	
	private Collection<VehicleRoute> vehicleRoutes;
	
	private double score;

	public TourPlan(Collection<VehicleRoute> vehicleRoutes) {
		super();
		this.vehicleRoutes = vehicleRoutes;
	}

	public Collection<VehicleRoute> getVehicleRoutes() {
		return vehicleRoutes;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	

}
