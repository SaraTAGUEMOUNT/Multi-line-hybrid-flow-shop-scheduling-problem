package pack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import pack.MersenneTwisterFast;
import ilog.concert.*;
public class Data {
	

	String[] myArray; 
	public int size;
	public String pName;
	//Physical information^
	
	public int nbmaxjob;
	public int cost;
	public int nbmaxmachine;
	public int nbmaxligne;
	public int nbmaxetage;
	public int upperBound;
	public int powerMax;
	public int energyMax;
	public int nbLignesForJob[];
	public int nbJobWithManyLines;
	public int [] tabJobWithManyLines; 
	public int [] tabJobAssignedtoLine; 
	public int maxStage[];
	public int nbOperationsOnLigneForJob[][];
	public int lineNumberForJob[][];
	public int nbMachinesForOperationOnLigneForJob[][][];
	public int machineNumberForOperationOnLigneForJob[][][][];
	public int durationOnMachineForOperationOnLigneForJob[][][][];
	public int minDurationLigneJob[][];
	public int maxDurationLigneJob[][];
	public int minDurationMachine[];
	public int maxDurationMachine[];
	public int energyMinMac;
	public int energyMaxMac;
	public int stageForMachine[];
	public int lineForMachine[];
	//public int [][]energyMaxJobLigne;
	//energy information
	public int idlePowerForMachine[];
	public int powerProfileForMachineOperationOnLigneForJob[][][][][];
	public int energyOnMachineForOperationOnLigneForJob[][][][];
	public int nbCostProfile;
	public int durationCostProfileForStep[];
	public int valueCostProfileForStep[];
	public int minPowerThreshold;
	public int minEnergyThreshold; //à définir
	public int maxPowerThreshold;
	public int maxEnergyThreshold;
	public int PowerMaxOnMac[];
	public int PowerMinForjob[];
	public int CostPerStepTime[];
	public long weightCmax;
	public int numOpForJob[][]; //TOBEDEFINED
	public int cmax;
	public Data () {};
	public int HMAX = 99999;
	public int[] trackPowerThreshold;
	
	public Data (String instName) {
		try {
			String[] values = null;
			InputStream ips = new FileInputStream(new File("./Instances/"+instName+".mlsf"));
			pName = instName;
			InputStreamReader ipsr = new InputStreamReader(ips);
			upperBound = 0; minPowerThreshold=9999;maxPowerThreshold=0;
			BufferedReader br = new BufferedReader(ipsr);
			String line = br.readLine();
			values = line.trim().split("\\s+");
			nbmaxjob = Integer.parseInt(values[0]);
			nbmaxligne= Integer.parseInt(values[1]);
			nbmaxmachine= Integer.parseInt(values[2]);
			size = 0;
			nbmaxetage = nbmaxmachine;
			idlePowerForMachine = new int [nbmaxmachine];
			PowerMaxOnMac = new int[nbmaxmachine];
			stageForMachine = new int[nbmaxmachine];
			lineForMachine = new int[nbmaxmachine];
			PowerMinForjob = new int[nbmaxjob];
			numOpForJob = new int[nbmaxjob][];
			int matrixDurationsBound[][][] = new int[nbmaxjob][nbmaxligne][nbmaxmachine];
			for (int m=0; m < nbmaxmachine;++m) {
				idlePowerForMachine[m]=Integer.parseInt(values[3+m]);
				PowerMaxOnMac[m]=0;
				for (int j=0; j < nbmaxjob; ++j) {
					PowerMinForjob[j]=99999;
					for (int l=0; l < nbmaxligne; ++l) {
						matrixDurationsBound[j][l][m]=0; //initialisation pour calcul borne sup duree
					}
				}
			}
			nbJobWithManyLines=0; 
			tabJobWithManyLines = new int [nbmaxjob];
			tabJobAssignedtoLine = new int [nbmaxligne];
			nbLignesForJob = new int[nbmaxjob];
			maxStage = new int[nbmaxjob];
			lineNumberForJob = new int[nbmaxjob][];
			nbOperationsOnLigneForJob = new int[nbmaxjob][];
			nbMachinesForOperationOnLigneForJob = new int[nbmaxjob][][];
			machineNumberForOperationOnLigneForJob = new int[nbmaxjob][][][];
			durationOnMachineForOperationOnLigneForJob= new int[nbmaxjob][][][];
			powerProfileForMachineOperationOnLigneForJob= new int[nbmaxjob][][][][];
			energyOnMachineForOperationOnLigneForJob= new int[nbmaxjob][][][];
			int[][] energyMinJobLigne = new int[nbmaxjob][];
			int[][] energyMaxJobLigne = new int[nbmaxjob][];
			minDurationLigneJob = new int[nbmaxjob][nbmaxligne];
			maxDurationLigneJob = new int[nbmaxjob][nbmaxligne];
			minDurationMachine = new int[nbmaxmachine];
			maxDurationMachine = new int[nbmaxmachine];
			for (int m=0; m < nbmaxmachine;++m) {
				minDurationMachine[m] = 0;
				maxDurationMachine[m] = 0;
			}
			weightCmax=0;
			int cptOpNum=0;
			for (int j=0; j<nbmaxjob; j++) {
				maxStage[j]=0;
				
				int cpt = 0;
				line = br.readLine();
				values = line.trim().split("\\s+");
				nbLignesForJob[j] = Integer.parseInt(values[cpt++]);
				energyMinJobLigne[j] = new int[nbLignesForJob[j]];
				energyMaxJobLigne[j] = new int[nbLignesForJob[j]];
				lineNumberForJob[j] = new int[nbLignesForJob[j]];
				nbOperationsOnLigneForJob[j] = new int[nbLignesForJob[j]];
				
				nbMachinesForOperationOnLigneForJob[j] = new int[nbLignesForJob[j]][];
				machineNumberForOperationOnLigneForJob[j] = new int[nbLignesForJob[j]][][];
				durationOnMachineForOperationOnLigneForJob[j]= new int[nbLignesForJob[j]][][];
				powerProfileForMachineOperationOnLigneForJob[j]= new int[nbLignesForJob[j]][][][];
				energyOnMachineForOperationOnLigneForJob[j]=new int[nbLignesForJob[j]][][];
				int dureeLigne = 0;
				if(nbLignesForJob[j]>1) {
					tabJobWithManyLines[nbJobWithManyLines]=j;
					nbJobWithManyLines++;
					/*for (int l=0; l<nbLignesForJob[tabJobWithManyLines[nbJobWithManyLines]]; l++) {
						tabJobAssignedtoLine[l]=j;
					    j--;
					}*/
				}
        
				
				for (int l=0; l<nbLignesForJob[j]; l++) {
					lineNumberForJob[j][l] = Integer.parseInt(values[cpt++]);
					nbOperationsOnLigneForJob[j][l] = Integer.parseInt(values[cpt++]);
					energyMinJobLigne[j][l]=0;
					energyMaxJobLigne[j][l]=0;
					if (nbOperationsOnLigneForJob[j][l]>maxStage[j]) {
						maxStage[j]=nbOperationsOnLigneForJob[j][l];
					}
					nbMachinesForOperationOnLigneForJob[j][l] = new int[nbOperationsOnLigneForJob[j][l]];
					machineNumberForOperationOnLigneForJob[j][l] = new int[nbOperationsOnLigneForJob[j][l]][];
					durationOnMachineForOperationOnLigneForJob[j][l]= new int[nbOperationsOnLigneForJob[j][l]][];
					powerProfileForMachineOperationOnLigneForJob[j][l]= new int[nbOperationsOnLigneForJob[j][l]][][];
					energyOnMachineForOperationOnLigneForJob[j][l]= new int[nbOperationsOnLigneForJob[j][l]][];
					int powerLineL = 0;
					for (int op=0; op<nbOperationsOnLigneForJob[j][l]; op++) {
						nbMachinesForOperationOnLigneForJob[j][l][op] = Integer.parseInt(values[cpt++]);
				        
						machineNumberForOperationOnLigneForJob[j][l][op] = new int[nbMachinesForOperationOnLigneForJob[j][l][op]];
						durationOnMachineForOperationOnLigneForJob[j][l][op]= new int[nbMachinesForOperationOnLigneForJob[j][l][op]];
						powerProfileForMachineOperationOnLigneForJob[j][l][op]= new int[nbMachinesForOperationOnLigneForJob[j][l][op]][];
						energyOnMachineForOperationOnLigneForJob[j][l][op]= new int[nbMachinesForOperationOnLigneForJob[j][l][op]];
						int powerML=99999;int eMin = 999999;
						int dureeMax = 0;
						int dureeMin = 99999;
						for (int m=0; m<nbMachinesForOperationOnLigneForJob[j][l][op]; m++) {
							machineNumberForOperationOnLigneForJob[j][l][op][m] = Integer.parseInt(values[cpt++]);
							stageForMachine[machineNumberForOperationOnLigneForJob[j][l][op][m]]=op;
							lineForMachine[machineNumberForOperationOnLigneForJob[j][l][op][m]]=lineNumberForJob[j][l];
							durationOnMachineForOperationOnLigneForJob[j][l][op][m]= Integer.parseInt(values[cpt++]);
							powerProfileForMachineOperationOnLigneForJob[j][l][op][m]=new int[durationOnMachineForOperationOnLigneForJob[j][l][op][m]];
							energyOnMachineForOperationOnLigneForJob[j][l][op][m]=0;
							int wMin = 0;
							if (durationOnMachineForOperationOnLigneForJob[j][l][op][m]>dureeMax) dureeMax = durationOnMachineForOperationOnLigneForJob[j][l][op][m];
							if (durationOnMachineForOperationOnLigneForJob[j][l][op][m]<dureeMin) dureeMin = durationOnMachineForOperationOnLigneForJob[j][l][op][m];
							for (int w=0; w < durationOnMachineForOperationOnLigneForJob[j][l][op][m];++w) {
								powerProfileForMachineOperationOnLigneForJob[j][l][op][m][w]=Integer.parseInt(values[cpt++]);
								energyOnMachineForOperationOnLigneForJob[j][l][op][m]+=powerProfileForMachineOperationOnLigneForJob[j][l][op][m][w];
								weightCmax+=energyOnMachineForOperationOnLigneForJob[j][l][op][m];
								if (powerProfileForMachineOperationOnLigneForJob[j][l][op][m][w]>wMin) wMin = powerProfileForMachineOperationOnLigneForJob[j][l][op][m][w];
								if (powerProfileForMachineOperationOnLigneForJob[j][l][op][m][w]>PowerMaxOnMac[machineNumberForOperationOnLigneForJob[j][l][op][m]]) PowerMaxOnMac[machineNumberForOperationOnLigneForJob[j][l][op][m]] = powerProfileForMachineOperationOnLigneForJob[j][l][op][m][w];
							}
							if (wMin<powerML) powerML = wMin;
							if (eMin>energyOnMachineForOperationOnLigneForJob[j][l][op][m]) eMin=energyOnMachineForOperationOnLigneForJob[j][l][op][m];
							//get longest duration for an operation on a given stage
							if (matrixDurationsBound[j][l][op]<durationOnMachineForOperationOnLigneForJob[j][l][op][m]) matrixDurationsBound[j][l][op]=durationOnMachineForOperationOnLigneForJob[j][l][op][m];
							upperBound+=durationOnMachineForOperationOnLigneForJob[j][l][op][m];
							minDurationMachine[machineNumberForOperationOnLigneForJob[j][l][op][m]]+=durationOnMachineForOperationOnLigneForJob[j][l][op][m];
						}
						maxDurationLigneJob[j][l]+=dureeMax;
						minDurationLigneJob[j][l]+=dureeMin;
						energyMinJobLigne[j][l]+=energyMinMac;
						energyMaxJobLigne[j][l]+=energyMaxMac;
						energyMinJobLigne[j][l]+=eMin;
						if (powerML>powerLineL) powerLineL=powerML;
					}
					if (PowerMinForjob[j]>powerLineL) PowerMinForjob[j]=powerLineL;
				}
				numOpForJob[j]=new int[maxStage[j]];
				for (int s=0; s<maxStage[j]; ++s) {
					numOpForJob[j][s]=size++;
				}
				//size += maxStage[j];//surdimensionner 
			}
			upperBound=0;
			for (int j=0; j < nbmaxjob;++j) {
				int dureeJob=0;
				for (int l=0; l < nbLignesForJob[j];++l) {
					int dureeLigne = 0;
					for (int op=0; op<nbOperationsOnLigneForJob[j][l]; op++) {
						dureeLigne+=matrixDurationsBound[j][l][op]; //get sum of longest durations of operations for job on a given line
					}
					if (dureeLigne>dureeJob) dureeJob=dureeLigne; //store longest processing for job
				}
				upperBound+=dureeJob;
			}
			CostPerStepTime = new int[HMAX];
			maxPowerThreshold=0;
			for (int m=0; m<nbmaxmachine; m++) {
				maxPowerThreshold+=PowerMaxOnMac[m];
			}
			minPowerThreshold = 0;
			for (int j=0; j<nbmaxjob; j++) {
				if (minPowerThreshold<PowerMinForjob[j]) minPowerThreshold=PowerMinForjob[j];
			}
			minEnergyThreshold = 0;
			for (int j=0; j<nbmaxjob; j++) {
				int emax = 99999;
				for (int l=0; l < nbLignesForJob[j];++l) {
					if (emax>energyMinJobLigne[j][l]) emax=energyMinJobLigne[j][l];
				}
				minEnergyThreshold+=emax;
			}
			maxEnergyThreshold = 0;
			minEnergyThreshold = 0;
			for (int j=0; j<nbmaxjob; j++) {
				int emax = 0, emin = HMAX;
				for (int l=0; l < nbLignesForJob[j];++l) {
					if (emin>energyMinJobLigne[j][l]) emin=energyMinJobLigne[j][l];
					if (emax<energyMaxJobLigne[j][l]) emax=energyMaxJobLigne[j][l];
				}
				minEnergyThreshold+=emin;
				maxEnergyThreshold+=emax;
			}
			
			line = br.readLine();
			values = line.trim().split("\\s+");
			nbCostProfile = Integer.parseInt(values[0]);
			 durationCostProfileForStep = new int[nbCostProfile];
			 valueCostProfileForStep= new int[nbCostProfile];
			 int maxCost = 0;
			for (int cost=0; cost < nbCostProfile;++cost) {
				line = br.readLine();
				values = line.trim().split("\\s+");
				durationCostProfileForStep[cost]=Integer.parseInt(values[0]);
				valueCostProfileForStep[cost]=Integer.parseInt(values[1]);
				if (maxCost<valueCostProfileForStep[cost]) maxCost= valueCostProfileForStep[cost];
			}
			int t =0;
			while ( t < HMAX) {
				for (int cost=0; cost < nbCostProfile;++cost) {
					for (int p=0; p < durationCostProfileForStep[cost]; ++p) {
						CostPerStepTime[t]=valueCostProfileForStep[cost];
						++t;
						if (t == HMAX) {
							break;
						}
					}
					if (t == HMAX) {
						break;
					}
				}
			}
			weightCmax*=maxCost;
			int weight = 10;
			while ((weight*=10)<weightCmax);
			weightCmax=weight;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setTarification(int localUpperBound) {
		// DEFNITION COUTS PAR PERIODE
		CostPerStepTime = new int[localUpperBound];
		int t =0;
		while ( t < localUpperBound) {
			for (int cost=0; cost < nbCostProfile;++cost) {
				for (int p=0; p < durationCostProfileForStep[cost]; ++p) {
					CostPerStepTime[t]=valueCostProfileForStep[cost];
					++t;
					if (t == localUpperBound) {
						break;
					}
				}
				if (t == localUpperBound) {
					break;
				}
			}
			
		}
	}
	
	private String myArray(int j, int l, int m) {
		// TODO Auto-generated method stub
		return null;
	}
	public int getSize() {
        return size;
    }
	public int getnbmaxjob() {
        return nbmaxjob;
    }
	public int getnbmaxmachine() {
        return nbmaxmachine;
    }
	public int getnbmaxligne() {
     return nbmaxligne;
    }

	public void generateInstances(int instNum, int nbInstances, int nbJobMin, int nbJobMax, int nbLignesMin, int nbLigneMax,  int nbEtagesMin, int nbEtagesMax, int nbMacMax, int dureeOpMin, int dureeOpMax, int puissanceMin, int puissanceMax, int nbProfilsCoutsMin, int nbProfilsCoutsMax, int costMin, int costMax) {
		  
		 for (int inst = instNum; inst < instNum+nbInstances; ++inst) {
			 //random number generator
			 MersenneTwisterFast r = new MersenneTwisterFast(inst);
			 
			 try {
			     
				 // define job and line numbers
			     int nbJobs = nbJobMin + r.nextInt(nbJobMax-nbJobMin+1);
				 int nbLignes = nbLignesMin + r.nextInt(nbLigneMax-nbLignesMin+1);
				// define machine repartition per line
				 int repartitionOfMacOnStageOnLine[][] = new int[nbLignes][nbEtagesMax+1];
				 int nbEtagesLigne[] = new int[nbLignes];
				// int macInLigne[] = new int[nbLignes];
				 int nbMachines = 0;
				 for (int l= 0; l < nbLignes; ++l) {
					 nbEtagesLigne[l]=nbEtagesMin+r.nextInt(nbEtagesMax-nbEtagesMin+1);
					// macInLigne[l]=nbEtagesLigne[l];
					 nbMachines+=nbEtagesLigne[l];//updating number of machines
					 for (int e=0; e<nbEtagesLigne[l]; ++e) {
						 repartitionOfMacOnStageOnLine[l][e]=1;
					 }
				 }
				 for (int l= 0; l < nbLignes; ++l) {
					 for (int e=0; e<nbEtagesLigne[l]; ++e) {
						 int m = r.nextInt(nbMacMax);
						 /*if (nbEtagesLigne[l]==1) {
							 while (m <3) {
								 m = r.nextInt(nbMacMax);
							 }
						 }*/
						 repartitionOfMacOnStageOnLine[l][e]+=m; //machine number per stage
						 nbMachines+=m; //updating number of machines
					 }
				 }
				 
				 // numbering of each machine
				 int[][][] macNumberLigne = new int[nbLignes][nbMachines][nbMachines];
				 int cpt = 0;
				 for (int l= 0; l < nbLignes; ++l) {
					 for (int e=0; e<nbEtagesLigne[l]; ++e) {
						 for (int m=0; m<repartitionOfMacOnStageOnLine[l][e]; ++m) {
							 macNumberLigne[l][e][m]=cpt++;
						 }
					 }
				 }
				 // defining operations for jobs	 
				 String gammesG="";
				 int[] lignesCheckForJob= new int[nbLignes];
				 for (int job = 0; job < nbJobs; ++job) {
					 String gammes="";
					 int nbLignesForJob = 1+r.nextInt(nbLignes);
					 gammes += nbLignesForJob + " "; //le nombre de lignes
					 int[] lignesForJob= new int[nbLignes];
					 for (int l=0; l < nbLignesForJob; ++l) {lignesForJob[l]=0;}
					 //assigning possibles lines to the job
					 int decompte = nbLignesForJob;
					 while (decompte >0) {
						 for (int l=0; l < nbLignesForJob; ++l) {
							 lignesCheckForJob[l]++;
							 //random choice of possible line for job
							 if(decompte>0 && r.nextInt(2)==1 && lignesForJob[l]==0) {
								 decompte--;lignesForJob[l]=1;
							 }
						 } 
					 }
					 for (int l=0; l < nbLignesForJob; ++l) {
						 if (lignesCheckForJob[l]==0) {
							 int tutu =0;
						 }
					 }
					 for (int l=0; l < nbLignesForJob; ++l) {
						 if (lignesForJob[l]==1) { // if line is possible for job
							 gammes += l +" "+nbEtagesLigne[l]+ " "; //le numéro de la ligne + nombre d'étages = nombre d'opérations
							 for (int e = 0; e < nbEtagesLigne[l]; ++e) {
								 gammes+= repartitionOfMacOnStageOnLine[l][e] + " "; //number of possible machine at stage e
								 for (int m=0; m< repartitionOfMacOnStageOnLine[l][e]; ++m) {
									 int dureeOp = dureeOpMin+ r.nextInt(dureeOpMax-dureeOpMin);
									 gammes += macNumberLigne[l][e][m] + " " + (dureeOp) + " "; //machine number + duration
									 //power profile for operation
									 for (int etape=0; etape < dureeOp; ++etape) {
										// double puiss = ((double)1.0)/((double)dureeOp);
										 int p = r.nextInt(puissanceMax-2*dureeOp);
										 int puis = (puissanceMin+p);
										 gammes += puis+ " "; //for each unit -> assign a power 
									 }
								 }
							 }
						 }
					 }
					 gammes+="\n";
					 gammesG+=gammes;
				 }
				 // defining cost periods
				 int periodeCosts = 24;
				 int nbProfilsCouts = nbProfilsCoutsMin+r.nextInt(nbProfilsCoutsMax-nbProfilsCoutsMin+1);
				 int[] dureeProfilCost = new int[nbProfilsCouts];
				 int[] costProfilCout = new int[nbProfilsCouts];
				 for (int c=0; c < nbProfilsCouts; ++c) {
					 dureeProfilCost[c]=1;
					 periodeCosts--;
				 }
				 while (periodeCosts>0) {
					 for (int c=0; c < nbProfilsCouts; ++c) {
						 int addDuree = r.nextInt(2);
						 dureeProfilCost[c]+=addDuree;
						 periodeCosts-=addDuree;
						 if (periodeCosts==0) c=nbProfilsCouts;
					 }
				 }
				 for (int c=0; c < nbProfilsCouts; ++c) {
					 costProfilCout[c]=costMin+ r.nextInt(costMax-costMin+1);
				 }
				 // pre-writing of costs
				 String costs = nbProfilsCouts+"\n"; 
				 for (int c=0; c < nbProfilsCouts; ++c) {
					costs+=dureeProfilCost[c]+" "+ costProfilCout[c] + "\n";
				 }
				 // pre-writing of header
				 String entete = nbJobs + " " + nbLignes + " " + nbMachines + " ";
				 for (int m=0; m < nbMachines; ++m) {
					 entete += puissanceMin+ r.nextInt(puissanceMin) + " ";
				 }
				 entete += "\n";
				 
				 File outFile = new File("./Instances/instance_"+inst+".mlsf");
				 outFile.getParentFile().mkdirs();
				 FileWriter fileWriter = new FileWriter(outFile);
				 fileWriter.write(entete);
				 fileWriter.write(gammesG);
				 fileWriter.write(costs);
				 fileWriter.close();
			 } catch (IOException ioe) {
				 System.out.println(ioe.getMessage());
			 }
		 }
		 
	}
}

