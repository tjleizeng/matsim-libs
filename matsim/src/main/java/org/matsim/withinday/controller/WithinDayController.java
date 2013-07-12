/* *********************************************************************** *
 * project: org.matsim.*
 * WithinDayController.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.withinday.controller;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;
import org.matsim.core.mobsim.framework.events.MobsimInitializedEvent;
import org.matsim.core.mobsim.framework.listeners.FixedOrderSimulationListener;
import org.matsim.core.mobsim.framework.listeners.MobsimInitializedListener;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.router.TripRouterFactory;
import org.matsim.core.router.TripRouterFactoryImpl;
import org.matsim.core.router.util.TravelTime;
import org.matsim.withinday.mobsim.WithinDayEngine;
import org.matsim.withinday.mobsim.WithinDayQSimFactory;
import org.matsim.withinday.replanning.identifiers.tools.ActivityReplanningMap;
import org.matsim.withinday.replanning.identifiers.tools.LinkReplanningMap;
import org.matsim.withinday.trafficmonitoring.TravelTimeCollector;
import org.matsim.withinday.trafficmonitoring.TravelTimeCollectorFactory;

/**
 * This controller contains the basic structure for
 * simulation runs using within-day replanning.
 *
 * @author Christoph Dobler
 */
public abstract class WithinDayController extends Controler implements StartupListener, MobsimInitializedListener {

	private static final Logger log = Logger.getLogger(WithinDayController.class);

	/*
	 * How many parallel Threads shall do the Replanning.
	 */
	private int numReplanningThreads = 2;
	
	private TravelTimeCollectorFactory travelTimeCollectorFactory = new TravelTimeCollectorFactory();
	private TravelTimeCollector travelTimeCollector;
	private Set<String> travelTimeCollectorModes = null;
	private ActivityReplanningMap activityReplanningMap;
	private LinkReplanningMap linkReplanningMap;

	private boolean withinDayEngineInitialized = false;
	private WithinDayEngine withinDayEngine;
	private TripRouterFactory withinDayTripRouterFactory;
	private FixedOrderSimulationListener fosl = new FixedOrderSimulationListener();

	public WithinDayController(String[] args) {
		super(args);

		init();
	}

	public WithinDayController(Config config) {
		super(config);

		init();
	}

	public WithinDayController(Scenario scenario) {
		super(scenario);

		init();
	}

	/*
	 * ===================================================================
	 * Those methods initialize objects that might be typically be used
	 * by within-day replanning code.
	 * ===================================================================
	 */
	
	public void setNumberOfReplanningThreads(int threads) {
		this.numReplanningThreads = threads;
	}
	
	public int getNumberOfReplanningThreads() {
		return this.numReplanningThreads;
	}
	
	public void setModesAnalyzedByTravelTimeCollector(Set<String> modes) {
		this.travelTimeCollectorModes = modes;
	}
	
	public Set<String> getModesAnalyzedByTravelTimeCollector() {
		return Collections.unmodifiableSet(this.travelTimeCollectorModes);
	}
	
	public void createAndInitTravelTimeCollector() {
		this.createAndInitTravelTimeCollector(this.travelTimeCollectorModes);
	}

	public void createAndInitTravelTimeCollector(Set<String> analyzedModes) {
		if (this.events == null) {
			log.warn("Cannot create and init the TravelTimeCollector. EventsManager has not be initialized yet!");
			return;
		}
		if (travelTimeCollector == null) {
			travelTimeCollector = travelTimeCollectorFactory.createTravelTimeCollector(this.scenarioData, analyzedModes);
			fosl.addSimulationListener(travelTimeCollector);
			this.events.addHandler(travelTimeCollector);
		}
	}

	public TravelTimeCollector getTravelTimeCollector() {
		return this.travelTimeCollector;
	}

	public void createAndInitActivityReplanningMap() {
		if (this.events == null) {
			log.warn("Cannot create and init the ActivityReplanningMap. EventsManager has not be initialized yet!");
			return;
		}
		if (activityReplanningMap == null) {
			activityReplanningMap = new ActivityReplanningMap();
			this.getEvents().addHandler(activityReplanningMap);
			fosl.addSimulationListener(activityReplanningMap);
		}
	}

	public ActivityReplanningMap getActivityReplanningMap() {
		return this.activityReplanningMap;
	}

	public void createAndInitLinkReplanningMap() {
		this.createAndInitLinkReplanningMap(null);
	}
	
	public void createAndInitLinkReplanningMap(Map<String, TravelTime> travelTime) {
		if (this.events == null) {
			log.warn("Cannot create and init the LinkReplanningMap. EventsManager has not be initialized yet!");
			return;
		}
		if (linkReplanningMap == null) {
			linkReplanningMap = new LinkReplanningMap(this.network, travelTime);
			this.getEvents().addHandler(linkReplanningMap);
			fosl.addSimulationListener(linkReplanningMap);
		}
	}


	public LinkReplanningMap getLinkReplanningMap() {
		return this.linkReplanningMap;
	}

	/*
	 * TODO: Add a Within-Day Group to the Config. Then this method
	 * can be called on startup.
	 */
	public void initWithinDayEngine(int numOfThreads) {
		if (!withinDayEngineInitialized) {
			log.info("Initialize ReplanningManager");
			withinDayEngine.initializeReplanningModules(numOfThreads);
			withinDayEngineInitialized = true;
		}
	}

	/*
	 * Use travel times from the travel time collector for car trips.
	 */
	public void initWithinDayTripRouterFactory() {
		this.withinDayTripRouterFactory = new TripRouterFactoryImpl(
				getScenario(),
				getTravelDisutilityFactory(),
				this.getTravelTimeCollector(),
				getLeastCostPathCalculatorFactory(),
				getScenario().getConfig().scenario().isUseTransit() ? getTransitRouterFactory() : null);
	}
	
	public WithinDayEngine getWithinDayEngine() {
		return this.withinDayEngine;
	}
	
	public void setWithinDayTripRouterFactory(TripRouterFactory tripRouterFactory) {
		this.withinDayTripRouterFactory = tripRouterFactory;
	}

	public TripRouterFactory getWithinDayTripRouterFactory() {
		return this.withinDayTripRouterFactory;
	}
	
	public FixedOrderSimulationListener getFixedOrderSimulationListener() {
		return this.fosl;
	}
	/*
	 * ===================================================================
	 */

	private void init() {
		
		super.getMobsimListeners().add(fosl);
		
		// initialize a withinDayEngine and set WithinDayQSimFactory as MobsimFactory
		this.withinDayEngine = new WithinDayEngine(this.getEvents());
		this.setMobsimFactory(new WithinDayQSimFactory(withinDayEngine));
		
		// register this as a Controller and Simulation Listener
		this.getFixedOrderSimulationListener().addSimulationListener(this);
		this.addControlerListener(this);
	}

	/*
	 * When the Controller Startup Event is created, the EventsManager
	 * has already been initialized. Therefore we can initialize now
	 * all Objects, that have to be registered at the EventsManager.
	 */
	@Override
	public void notifyStartup(StartupEvent event) {
		this.initWithinDayEngine(this.numReplanningThreads);
		this.createAndInitTravelTimeCollector();
		this.createAndInitActivityReplanningMap();
		this.createAndInitLinkReplanningMap();
	}
	
	@Override
	public void notifyMobsimInitialized(MobsimInitializedEvent e) {
		initReplanners((QSim)e.getQueueSimulation());
	}
	
	/**
	 * This is where one should initialize the Replanners and Identifiers.
	 * It is called by a MobsimInitializedListener.
	 */
	protected abstract void initReplanners(QSim sim);

}