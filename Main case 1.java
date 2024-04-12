package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import test.Data*/;

public class Main {
	static int cptInstances=0;
	static int instNum, nbInstances,  nbJobMin,  nbJobMax,  nbLignesMin,  nbLigneMax,  nbEtagesMin,  nbEtagesMax;
	static int nbMacMax,  dureeOpMin,  dureeOpMax,  puissanceMin,  puissanceMax,  nbProfilsCoutsMin;
	static int nbProfilsCoutsMax,  costMin,  costMax;
	static int makespan,  powerMax,  energyMax, timeSolveMax;
	static boolean isCmaxEstimate;
	
	public static void main(String[] args) {
		// instances names : to update if more than 100 instances to handle
		String[] pNames = new String[100];
		try {
			
			String workingDir = System.getProperty("user.dir");
			System.out.println("Current working directory : " + workingDir);
			// #############################################################################################
			//									Get Instances Names
			// #############################################################################################
			InputStream ips = new FileInputStream(new File(workingDir+"/Instances/instances_list.txt"));
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			while ((pNames[cptInstances++] = br.readLine()) != null);--cptInstances;
			
			
			
			// #############################################################################################
			//									Generating INSTANCES
			// #############################################################################################
			boolean generateInstances = false;
			if (generateInstances) {
				Data madata = new Data();
				//generate nbInstances starting at number instNum
				//WARNING : can override already existing instances if instNum is not well chosen
				madata.generateInstances(instNum=17, nbInstances=3, nbJobMin=10, nbJobMax=20, nbLignesMin=3, nbLigneMax=3, nbEtagesMin = 3, nbEtagesMax=4, 
						nbMacMax=3, dureeOpMin=5, dureeOpMax=30, puissanceMin=10, puissanceMax=70, nbProfilsCoutsMin=5, nbProfilsCoutsMax=10, costMin=5, costMax=50);
			}
			
			// #############################################################################################
			//									Reading AND Solving Problems
			// #############################################################################################
			for (int ins = 0; ins <1; ++ins) {
				String pName = pNames[ins];
				pName = "instance_elmperiod1";    				System.out.println("########################## READING INSTANCE #####################################");
				Data data = new Data(pName);
				
				System.out.println(" Problem Name:"+pName+"\n Upper Bound On Cmax:"+data.upperBound);
				// ############################# CONTRAINTES ###########################################
				int eMax = 5000; System.out.println("   Energy Max: "+eMax);
				//int pMax = data.minPowerThreshold+15; 
				int pMax = 120;
				data.powerMax = pMax;
				System.out.println("   Power Max: "+pMax);
				int cMax = 60; System.out.println("   Makespan Max: "+cMax);
				cMax += 1; // !!!!!!!! penser à faire +1 sur la valeur du cmax pour des raisons de taille de structures de données
				
				// ############################# LANCEMENT CALCULS ###########################################
				System.out.println("########################## START INSTANCE SOLVE #####################################");
				Model cplex = new Model(data, isCmaxEstimate = false, makespan=cMax, powerMax=(pMax<data.maxPowerThreshold?pMax:data.maxPowerThreshold), energyMax=(eMax<data.maxEnergyThreshold?eMax:data.maxEnergyThreshold), timeSolveMax=3000);
				System.out.println(" Problem Name:"+pName+"\n Upper Bound On Cmax:"+data.upperBound+"\n nop:"+data.Size);
				
				System.out.println("########################## WRITING GANTT FILE #####################################");
				cplex.writeGANTT_SVG(data);
				int test = 2;
				System.out.println("########################## END INSTANCE SOLVE #######################################");
			}
			System.out.println("########################## END #######################################");
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

} 


