package pack;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.Scanner;

import pack.MersenneTwisterFast;

import pack.Data;

public class Sol {
	int[] pi;
	double[] sigma;
	public int[] gamma;
	double cplexGap;
	double cplexTime;
	boolean isDefined;
	long objfunction;
	int i;
	int makespan;
	int energyConsumption;
	int peak;
	int M = 100;
	int[] startDateOp;
	public long energyCost;
	long objectiveFunction;
	int violatedMakespan;
	int violatedEnergy;
	int violatedPeak;
	int mkpn;
	int machine;
	int op;
	int line;
	boolean isSetObjective;
	// vecteur associÈs ‡ la solution

	int[] peakPower; // time indexed
	int[] startDates;
	int[] endDates;

	// tables de calculs
	int[] jobOccurence;
	int[] lastOpOnMachine;
	int[] s;

	int[][] positionOccJobs;

	int[][] listJobOnMachine;
	int[] nbOpOnMachine;
	int[] machineForOp;
	int[] indiceMachineForOp;
	int[] durationForOp;
	int[] numMachineForOp;
	int[][] positionJobOnMac;

	int powerTab[] = new int[999999];
	int peakG = 0;
	int costTab[] = new int[999999];
	int costG = 0;
	int energyG = 0;

	public void decodage(Data data) {
		// lecture et Èvaluation de la solution
		violatedMakespan = 0;
		violatedEnergy = 0;
		violatedPeak = 0;
		energyConsumption = 0;
		makespan = 0;
		energyCost = 0;
		peak = 0;
		objfunction = 0;
		for (int j = 0; j < data.nbmaxjob; ++j) {
			jobOccurence[j] = 0;
		}
		for (int m = 0; m < data.nbmaxmachine; ++m) {
			lastOpOnMachine[m] = -1;
			nbOpOnMachine[m] = 0;

		}

		isDefined = true;
		// initialiser le tableau de puissance disponible
		for (int i = 0; i < data.HMAX; ++i) {
			peakPower[i] = 0;
		}
		// parcours du vecteur pi
		for (int i = 0; i < data.size; ++i) {
			int job = pi[i];
			int line = gamma[job];

			// verifier qu'on n'a pas vu le job plus de fois que la ligne n'a d'Ètages
			if (jobOccurence[job] < data.nbOperationsOnLigneForJob[job][line]) {
				int nbMachineForJobOccurence = data.nbMachinesForOperationOnLigneForJob[job][line][jobOccurence[job]];
				double choiceMachine = 1.0 / nbMachineForJobOccurence;
				int op = data.numOpForJob[job][jobOccurence[job]];
				int indiceMac = 0;
				double start = choiceMachine;
				while (start < sigma[op]) {
					start += choiceMachine;
					indiceMac++;
				}

				// dÈterminer les starts dates des opÈrations
				int machine = data.machineNumberForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac];
				int startDateConj = 0;
				if (jobOccurence[job] > 0) {
					startDateConj = endDates[op - 1];
				}

				int startDateDisj = 0;
				if (lastOpOnMachine[machine] > -1) {
					startDateDisj = endDates[lastOpOnMachine[machine]];
				}

				// vÈrifier les seuils de makespan
				int dateOp = startDateConj;
				if (dateOp < startDateDisj)
					dateOp = startDateDisj;

				// vÈrifier les seuils de puissances et mÈthode check puissance pour ne pas
				// violer la contrainte de puissance

				boolean isOverTime = false; // pour detecter depassement du timeHorizon
				boolean isScheduleOp = true; // verifier si l'opÈration est ordonnancable dans le temps imparti <=
												// timeHorizon
				int startPoint = dateOp;
				int t = 0;
				// while(!isScheduleOp && !isOverTime) {
				while (t < data.durationOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac]) {

					if (data.powerProfileForMachineOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac][t]
							+ peakPower[startPoint + t] > data.powerMax) {
						startPoint++;
						t = 0;
					} else {
						++t;
					}
					if (startPoint
							+ data.durationOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac] > data.cmax) {
						isOverTime = true;
						startPoint = dateOp;
						// violatedPeak++;
						break;
					}
				}
				isScheduleOp = true;
				for (t = 0; t < data.durationOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac]; t++) {
					peakPower[startPoint+ t] += data.powerProfileForMachineOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac][t];
					if (peakPower[startPoint + t] > peak)
						peak = peakPower[startPoint + t];
					if (peakPower[startPoint + t] > data.powerMax) {
						violatedPeak++;
					}
				}

				// }
				dateOp = startPoint;
				startDates[op] = dateOp;
				endDates[op] = dateOp
						+ data.durationOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac];

				if (endDates[op] > makespan)
					makespan = endDates[op];
				if (endDates[op] > data.upperBound)
					violatedMakespan++;

				energyConsumption += data.energyOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac];
				if (energyConsumption > data.energyMax)
					violatedEnergy++;
				positionOccJobs[job][jobOccurence[job]] = i;
				// energy COST (data.costPerStepTime) et procÈdure update energy cost

				for (t = 0; t < data.durationOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac]; t++) {

					energyCost += data.CostPerStepTime[startDates[op] + t]
							* data.powerProfileForMachineOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac][t];

				}

				listJobOnMachine[machine][nbOpOnMachine[machine]] = job;
				positionJobOnMac[machine][job] = nbOpOnMachine[machine];
				nbOpOnMachine[machine]++;
				jobOccurence[job]++;
				lastOpOnMachine[machine] = op;
				machineForOp[op] = indiceMac;
				numMachineForOp[op] = machine;
			}
		}

		objfunction = energyCost + (violatedMakespan + violatedEnergy + violatedPeak) * data.weightCmax;

	}

	
	// use the three solution vectors and the developed heuristic
	public void heuristic(Data data, Sol sol_temp, Sol sol_temp2, Sol sol_sav, int itermax, MersenneTwisterFast mstf) {
		Sol sol2 = new Sol(data);
		int itermaxLS = (int) ((data.size*(data.size-1))*0.7);
		// isSetObjective = false;
		for (int z = 0; z < data.size; z++) {
			sol2.generateRandom(data, mstf);
			sol2.decodage(data);
			/*if (!isSetObjective) {
				this.copieSol(data, sol2);
				isSetObjective = true;
			}*/
			int i = 0;
			// local search phase
			while (i++ < data.size) {

				for (int j = 0; j < itermaxLS; j++) {
					sol_sav.copieSol(data, sol2);
					
					// on teste sur Pi
					sol_temp.copieSol(data, sol2);
					sol_temp.Neighborpi(data, 1, mstf);
					if (sol_temp.objfunction < sol_sav.objfunction)
						sol_sav.copieSol(data, sol_temp);
					
					// on teste sur sigma
					sol_temp.copieSol(data, sol2);
					sol_temp.Neighborsigma(data, 1, mstf);
					if (sol_temp.objfunction < sol_sav.objfunction)
						sol_sav.copieSol(data, sol_temp);

					
					// si sol_sav est meilleure que sol2, alors on dÈplace sol_temp dessus
					if (sol_sav.objfunction < sol2.objfunction) {
						sol2.copieSol(data, sol_sav);
						j = 0;
					}
					if ( sol2.objfunction < this.objfunction) {
						this.copieSol(data, sol2);
						//this.writeGANTT_SVG(data);
					}
				}
					
				//this.writeGANTT_SVG(data);

				sol2.Neighborgamma(data, 1, mstf);
				sol2.decodage(data);
				// si sol_sav est meilleur alors on la garde comme solution courante
				if (sol2.objfunction < objfunction) {
					this.copieSol(data, sol2);
					//this.writeGANTT_SVG(data);
				}
			}

		}
	}
	
	

	
	public void Neighborpi(Data data, int nbrswap, MersenneTwisterFast mstf) {

		for (int nbs = 0; nbs < nbrswap; ++nbs) {
			int j1 = mstf.nextInt(data.getnbmaxjob());
			
			int j2 = mstf.nextInt(data.getnbmaxjob());
			
			while (j1 == j2)
				j2 = mstf.nextInt(data.getnbmaxjob());

			int line1 = gamma[j1];
			int line2 = gamma[j2];

			int occ1 = mstf.nextInt(data.nbOperationsOnLigneForJob[j1][line1]);
			int occ2 = mstf.nextInt(data.nbOperationsOnLigneForJob[j2][line2]);

			int positionJ1Occ1 = positionOccJobs[j1][occ1];
			int positionJ2Occ2 = positionOccJobs[j2][occ2];

			int temp = pi[positionJ1Occ1];
			pi[positionJ1Occ1] = pi[positionJ2Occ2];
			pi[positionJ2Occ2] = temp;

			decodage(data);
		}

	}

	public void Neighborsigma(Data data, int nbrChangeMachine, MersenneTwisterFast mstf) {

		for (int nbs = 0; nbs < nbrChangeMachine; ++nbs) {
			int j1 = mstf.nextInt(data.getnbmaxjob());

			int line1 = gamma[j1];

			int occ1 = mstf.nextInt(data.nbOperationsOnLigneForJob[j1][line1]);

			double choiceMachine = mstf.nextDouble(true, true);

			sigma[data.numOpForJob[j1][occ1]] = choiceMachine;

			decodage(data);
		}

	}

	public void Neighborgamma(Data data, int nbrChangeLine, MersenneTwisterFast mstf) {

		for (int nbs = 0; nbs < nbrChangeLine; ++nbs) {
			int j1 = mstf.nextInt(data.nbJobWithManyLines);
			int line1 = mstf.nextInt(data.nbLignesForJob[data.tabJobWithManyLines[j1]]);
			while (gamma[data.tabJobWithManyLines[j1]] == line1) {
				line1 = mstf.nextInt(data.nbLignesForJob[data.tabJobWithManyLines[j1]]);
			}

			gamma[data.tabJobWithManyLines[j1]] = line1;
			decodage(data);
		}
	}

	public void Neighborlarge(Data data, Sol sol_temp, int nbrswap, MersenneTwisterFast mstf) {
		sol_temp.Neighborpi(data, 3, mstf);
		sol_temp.Neighborsigma(data, 1, mstf);
		sol_temp.Neighborgamma(data, 2, mstf);

	}

	public void copieSol(Data data, Sol sol) {
		int t = 0;
		makespan = sol.makespan;
		energyCost = sol.energyCost;
		energyConsumption = sol.energyConsumption;

		peak = sol.peak;

		while (t < makespan) {
			peakPower[t] = sol.peakPower[t];
			t++;
		}
		objfunction = sol.objfunction;
		violatedMakespan = sol.violatedMakespan;
		violatedEnergy = sol.violatedEnergy;
		violatedPeak = sol.violatedPeak;
		for (int i = 0; i < data.size; ++i) {
			this.pi[i] = sol.pi[i];
			this.sigma[i] = sol.sigma[i];
			this.startDates[i] = sol.startDates[i];
			this.endDates[i] = sol.endDates[i];
		}
		for (int j = 0; j < data.getnbmaxjob(); j++) {
			this.gamma[j] = sol.gamma[j];
		}

		for (int m = 0; m < data.nbmaxmachine; m++) {
			nbOpOnMachine[m] = sol.nbOpOnMachine[m];
			for (int o = 0; o < sol.nbOpOnMachine[m]; o++) {
				listJobOnMachine[m][o] = sol.listJobOnMachine[m][o];
			}
		}

	}

	
	
	
	/*  public void copieSol(Data data, Sol sol) {
		int t = 0;
		makespan = sol.makespan;
		energyCost = sol.energyCost;
		energyConsumption = sol.energyConsumption;

		peak = sol.peak;

		while (t < makespan) {
			peakPower[t] = sol.peakPower[t];
			t++;
		}
		objfunction = sol.objfunction;
		violatedMakespan = sol.violatedMakespan;
		violatedEnergy = sol.violatedEnergy;
		violatedPeak = sol.violatedPeak;
		for (int i = 0; i < data.size; ++i) {
			this.pi[i] = sol.pi[i];
			this.sigma[i] = sol.sigma[i];
			this.startDates[i] = sol.startDates[i];
			this.endDates[i] = sol.endDates[i];
		}
		for (int j = 0; j < data.getnbmaxjob(); j++) {
			this.gamma[j] = sol.gamma[j];
		}

		for (int m = 0; m < data.nbmaxmachine; m++) {
			nbOpOnMachine[m] = sol.nbOpOnMachine[m];
			for (int o = 0; o < sol.nbOpOnMachine[m]; o++) {
				listJobOnMachine[m][o] = sol.listJobOnMachine[m][o];
			}
		}

	}*/
	 
	
	public void generateRandom(Data data, MersenneTwisterFast mstf) {
		int[] jobs = new int[data.getnbmaxjob()];

		int jobvalue;
		// TODO

		// dÈfinition de gamma et sigma
		
		for (int j = 0; j < data.getnbmaxjob(); j++) {
			if (data.nbLignesForJob[j]>0) {
			gamma[j] = mstf.nextInt(data.nbLignesForJob[j]);
			jobs[j] = j;
			jobOccurence[j] = data.maxStage[j];
			// System.out.println(data.lineNumberForJob[j][gamma[j]]);
		}
	}
		int nbJob = data.getnbmaxjob();

		for (int i = 0; i < data.size; i++) {
			sigma[i] = mstf.nextDouble();
			int selectedJob = mstf.nextInt(nbJob);
			jobvalue = jobs[selectedJob];
			pi[i] = jobvalue;
			jobOccurence[selectedJob]--;
			if (jobOccurence[selectedJob] == 0) {
				jobOccurence[selectedJob] = jobOccurence[nbJob - 1];
				jobs[selectedJob] = jobs[nbJob - 1];
				nbJob--;
			}
		}
		//decodage(data);
	}

	public Sol(Data data) {
		isDefined = false;

		positionOccJobs = new int[data.nbmaxjob][];
		pi = new int[data.size];
		sigma = new double[data.size];
		gamma = new int[data.nbmaxjob];
		jobOccurence = new int[data.nbmaxjob];
		lastOpOnMachine = new int[data.nbmaxmachine];
		positionJobOnMac = new int[data.nbmaxmachine][data.nbmaxjob];
		machineForOp = new int[data.size];
		indiceMachineForOp = new int[data.size];
		startDateOp = new int[data.size];
		durationForOp = new int[data.size];
		numMachineForOp = new int[data.size];
		peakPower = new int[data.HMAX];
		startDates = new int[data.size];
		endDates = new int[data.size];
		for (int j = 0; j < data.nbmaxjob; ++j) {
			positionOccJobs[j] = new int[data.maxStage[j]];
		}
		// initialization : matheuristique
		listJobOnMachine = new int[data.nbmaxmachine][];
		nbOpOnMachine = new int[data.nbmaxmachine];
		for (int m = 0; m < data.nbmaxmachine; ++m) {
			listJobOnMachine[m] = new int[data.nbmaxjob];
		}
	}

	public void writeGANTT_SVG(Data data)  {

		//IloCplex cplex = new IloCplex();
		int i, job, mac;
		int nbmac = data.nbmaxmachine, nbjob = data.nbmaxjob;
		int margin = 100;
		int marginBetweenRect = 2;
		int fleche = 20;
		int rectWidth = 50;
		int xStart = 0, xLength = 0, yStart = 0, yLength = 0;
		Random rand = new Random();
		//decodage(data);
		

		try {
			File ff = new File("./Results/GANTT/" + data.pName + "_gantt.svg"); // d√©finir l'arborescence

			ff.createNewFile();
			FileWriter ffw = new FileWriter(ff);
			// ffw.write(data.pName + "\n"); // √©crire une ligne dans le fichier
			// resultat.txt

			/*----------------------------------------------------------------------
			Ratio pour taille des rectangles du diagramme
			----------------------------------------------------------------------*/
			int ratio = 2;

			if (mkpn <= 30) {
				ratio = 20;
			} else if (mkpn < 100) {
				ratio = 10;
			} else if (mkpn < 250) {
				ratio = 5;
			} else if (mkpn < 500) {
				ratio = 3;
			}
			mkpn = makespan; 
			//mkpn++;
			int maxDiagLength = (int) ((ratio) * mkpn + 2 * margin + fleche + data.nbmaxmachine);
			int maxDiagHeight = (rectWidth + marginBetweenRect) * data.nbmaxmachine + margin + fleche + 10;

			ffw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			ffw.write(" <svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + maxDiagLength
					+ "\" height=\"" + maxDiagHeight + "\">\n");
			ffw.write("<title> Diagramme de Gantt du Probleme: " + data.pName + "</title>\n");
			ffw.write("<desc> Cette figure repr√©sente le planning des op√©rations du probl√®me trait√©.</desc>\n");

			/*----------------------------------------------------------------------
			    Cr√©ation de la palette de couleurs
			----------------------------------------------------------------------*/
			int[][] colors = new int[nbjob][3];
			rand.setSeed(1);//job to have the same color if the code ran multiple times
			for (i = 0; i < nbjob; ++i) {
				
				colors[i][0] = rand.nextInt(256);
				colors[i][1] = rand.nextInt(256);
				colors[i][2] = rand.nextInt(256);
				jobOccurence[i]=0;
			}
			int powerTab[] = new int[data.HMAX];
			int peakG = 0;
			int costTab[] = new int[data.HMAX];
			int costG = 0;
			int energyG = 0;
			for (int t = 0; t < data.HMAX; ++t) {
				powerTab[t] = 0;
				costTab[t] = 0;
				
			}
			
			
			//rÈcupÈration des donnÈes heuristque startdate
	
			
			for ( i=0; i < data.size; ++i) { 
				job = pi[i];
				line = gamma[job];
				//jobOccurence[job]=op;
				
				// verifier qu'on n'a pas vu le job plus de fois que la ligne n'a d'Ètages
				if (jobOccurence[job]<data.nbOperationsOnLigneForJob[job][line]) {
	
					int indiceMac = 0;
					int nbMachineForJobOccurence = data.nbMachinesForOperationOnLigneForJob[job][line][jobOccurence[job]];
					double choiceMachine = 1.0/nbMachineForJobOccurence;
					int op = data.numOpForJob[job][jobOccurence[job]];
					double start = choiceMachine;
					while (start < sigma[op]) {
						start += choiceMachine; indiceMac++;
						
					}
					
					mac = data.machineNumberForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac];
					int endDate = endDates[op];
					int startDate = startDates[op];
					
					xStart = margin + (ratio) * (startDate) ; // +dd1*macVisited[mac]
					xLength = (ratio) * (data.durationOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac]); // +dd1
					yStart = margin + mac * rectWidth ;
					yLength = rectWidth ;

					ffw.write("<rect style=\"fill:rgb(" + colors[job][0] + "," + colors[job][1] + "," + colors[job][2] + ");fill-opacity:0.5;\" width=\"" + xLength + "\" height=\"" + yLength + "\" x=\"" + xStart + "\" y=\"" + yStart + "\"/>\n");

					ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 15) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">J" + job + "</text>");
					ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 25) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">" + (startDate) + "</text>");
					ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 35) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">" + (endDate) + "</text>");
					 
						for (int p=0; p < data.durationOnMachineForOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac]; ++p) {
								//costTab[startDate+p]+=data.powerProfileForMachineOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac][p]*data.CostPerStepTime[startDate+p];
								costG+=data.powerProfileForMachineOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac][p]*data.CostPerStepTime[startDate+p];
								energyG+=data.powerProfileForMachineOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac][p];
								powerTab[startDate+p]+=data.powerProfileForMachineOperationOnLigneForJob[job][line][jobOccurence[job]][indiceMac][p];
								if (powerTab[startDate+p]>peakG)peakG=powerTab[startDate+p];
							
						}
						jobOccurence[job]++;
				}
			}
			
		
			

	/*	
	for (int j = 0; j < data.nbmaxjob; j++) {
				// data.nbLignesForJob[j]
				for (int l = 0; l < data.nbLignesForJob[j]; ++l) {
					if (gamma[j] == l) {
						for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l]; op++) {
							// for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op]; m++)
							// {
							// if (machineForOp[data.numOpForJob[j][op]]==m) {
							job = j;
							mac = machineForOp[data.numOpForJob[j][op]];
							int endDate = startDateOp[data.numOpForJob[j][op]] + durationForOp[data.numOpForJob[j][op]]
									- 1;
							int startDate = startDateOp[data.numOpForJob[j][op]];
							xStart = margin + (ratio) * (startDate) + marginBetweenRect; // +dd1*macVisited[mac]
							xLength = (ratio) * (durationForOp[data.numOpForJob[j][op]]) - marginBetweenRect; // +dd1
							yStart = margin + mac * rectWidth + marginBetweenRect;
							yLength = rectWidth - marginBetweenRect;

							ffw.write("<rect style=\"fill:rgb(" + colors[job][0] + "," + colors[job][1] + ","
									+ colors[job][2] + ");fill-opacity:0.5;\" width=\"" + xLength + "\" height=\""
									+ yLength + "\" x=\"" + xStart + "\" y=\"" + yStart + "\"/>\n");

							ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 15)
									+ "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">J" + job
									+ "</text>");
							ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 25)
									+ "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">"
									+ (startDate) + "</text>");
							ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 35)
									+ "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">"
									+ (endDate + 1) + "</text>");

							
							
							// construction power curve
							try { 
								  for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op]; ++m) { 
									  if(data.machineNumberForOperationOnLigneForJob[j][l][op][m]==machineForOp[data.numOpForJob[j][op]]) {
										  for (int p = 0; p < durationForOp[data.numOpForJob[j][op]]; ++p) {
											  powerTab[startDate + p] += data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p]; 
											  if(powerTab[startDate + p] > peakG) { 
												  peakG = powerTab[startDate + p]; 
												  //System.out.println("peakG2:"+peakG);
												  
												  
											  }
										  }
									
									     for (int p = 0; p < data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; ++p) {
									    	 costTab[startDate + p] += data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p]* data.CostPerStepTime[startDate + p]; 
									    	 costG += data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p] * data.CostPerStepTime[startDate + p]; 
									    	 energyG += data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p];
									    	
									    	 //System.out.println("cost:"+costTab[startDate + p]);
									     }
									    
									  }
								  }
							     } catch(Exception e) 
							  { 
							    	 System.out.println(e.getMessage());
							 }
						}
					}
				}

			
		} 	
*/
			
			int t = 0;int nb =0;
			while( t<mkpn) {
				 
				 int duree = data.durationCostProfileForStep[nb];
				 int costPeriod = data.CostPerStepTime[t];
	
				xStart = margin + (ratio) * (t) + marginBetweenRect; 
				xLength = (ratio) * (duree) - marginBetweenRect; 
				yStart = margin + (nbmac) * rectWidth + marginBetweenRect+10;
				yLength = 5 - marginBetweenRect;

				ffw.write("<rect style=\"fill:rgb(" + colors[0][0] + "," + colors[0][1] + "," + colors[0][2] + ");fill-opacity:0.5;\" width=\"" + xLength + "\" height=\"" + yLength + "\" x=\"" + xStart + "\" y=\"" + yStart + "\"/>\n");

				ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 15) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">P" + nb + "</text>");
				ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 25) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">" + (costPeriod) + "</text>");
					
				if (nb<data.nbCostProfile-1) {
					nb++;
				 } else {
					 nb = 0;
				 }
			 t+=duree;

		   }
			  t=0;
				int PicSurGantt = peak < data.powerMax? data.powerMax:peak;
				ffw.write("<line x1=\"" + (margin) +  "\" y1=\"" + (margin+ nbmac * rectWidth-data.powerMax * ((nbmac)* rectWidth)/PicSurGantt) + "\" x2=\"" + (margin+mkpn* ratio) + "\" y2=\"" + (margin+ nbmac * rectWidth - data.powerMax * ((nbmac)* rectWidth)/PicSurGantt ) + "\" stroke=\"red\" />");
				
				while (t<mkpn) { 
					
					ffw.write("<line x1=\"" + (margin+t* ratio) +  "\" y1=\"" + (margin + nbmac * rectWidth-powerTab[t] * ((nbmac)* rectWidth)/PicSurGantt) + "\" x2=\"" + (margin+(t+1)* ratio) + "\" y2=\"" + (margin + nbmac * rectWidth - powerTab[t] * ((nbmac)* rectWidth)/PicSurGantt ) + "\" stroke=\"blue\" />");
					
					t++;
				}
			
				
			// buffer.setColor(Color.DARK_GRAY);
						// vertical
						ffw.write("<line x1=\"" + margin + "\" y1=\"" + (margin - fleche) + "\" x2=\"" + margin + "\" y2=\""
								+ (margin + nbmac * rectWidth + marginBetweenRect) + "\" stroke=\"dimgrey\" />");
						ffw.write("<line x1=\"" + margin + "\" y1=\"" + (margin - fleche) + "\" x2=\"" + (margin + 10) + "\" y2=\""
								+ (margin - fleche + 10) + "\" stroke=\"dimgrey\" />");

						for (i = 1; i <= nbmac; ++i) {
							ffw.write("<text x=\"" + (margin - 45) + "\" y=\"" + (margin - fleche + i * rectWidth)
									+ "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">L."
									+ (data.lineForMachine[i - 1] + 1) + "-S." + (data.stageForMachine[i - 1] + 1) + "-M." + i
									+ "</text>");

						}
					
						//creation d'une ligne 
						
						
						//ecriture de la ligne
						 
						ffw.write("<line x1=\"" + (margin + ratio * mkpn  ) + "\" y1=\"" + (margin) + "\" x2=\"" + (margin + ratio * mkpn) + "\" y2=\"" + (margin + nbmac * rectWidth + marginBetweenRect ) + "\" stroke=\"purple\" />\n");
						 ffw.write("<text x=\"" + (margin + ratio * mkpn + 10) + "\" y=\"" + (margin + (nbmac) * rectWidth + marginBetweenRect + 10) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\" fill=\"purple\">W</text>");
						
						 
						 while (t < mkpn) {
								 //remplissage de la ligne: ajout des nombres
								  //int powerStelpOnLine = data.powerMax/10;
							    //int powerStelpOnLine = 1;
							    int power = 0; 
							     while ( power <= data.powerMax) {
							      
							       int x1 = margin + ratio *mkpn + 15; //tracer verticalement au vecteur time
							    	//vecteur de positionnement des nombres 
							     
							    	int y3 = margin + nbmac * rectWidth -  power*(nbmac*rectWidth)/data.powerMax;
							    	int y4 = margin + nbmac * rectWidth;
							    	
							    	//mettre les valeurs sur la ligne
							    	//int p = powerTab[t];: use power instead of p
							    	ffw.write("<text_x=\"" + x1 + "\" y=\"" + y3 + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"start\">"
											+ power + "</text>\n");
							    	//power+= powerStelpOnLine;
							    	power++;
								 }
							     
							     t++;
							}

						
							
							

					   
						
							
						
						// horizontal
						ffw.write("<line x1=\"" + margin + "\" y1=\"" + (margin + (nbmac) * rectWidth + marginBetweenRect)
								+ "\" x2=\"" + (margin + ratio * mkpn + fleche) + "\" y2=\""
								+ (margin + (nbmac) * rectWidth + marginBetweenRect) + "\" stroke=\"dimgrey\" />");
						ffw.write("<line x1=\"" + (margin + ratio * mkpn + fleche - 10) + "\" y1=\""
								+ (margin + nbmac * rectWidth + marginBetweenRect - 10) + "\" x2=\""
								+ (margin + ratio * mkpn + fleche) + "\" y2=\"" + (margin + (nbmac) * rectWidth + marginBetweenRect)
								+ "\" stroke=\"dimgrey\" />");
						ffw.write("<line x1=\"" + (margin + ratio * mkpn) + "\" y1=\""
								+ (margin + (nbmac) * rectWidth + marginBetweenRect + 2) + "\" x2=\"" + (margin + ratio * mkpn)
								+ "\" y2=\"" + (margin + (nbmac) * rectWidth + marginBetweenRect + 10) + "\" stroke=\"dimgrey\" />");

						ffw.write("<text x=\"" + (margin + 10) + "\" y=\"" + (margin - fleche - 5)
								+ "\" font-family=\"sans-serif\" font-size=\"20px\" text-anchor=\"middle\" fill=\"dimgrey\">Machines</text>");
						ffw.write("<text x=\"" + (margin + ratio * mkpn + 2 * fleche) + "\" y=\""
								+ (margin + (nbmac) * rectWidth + marginBetweenRect)
								+ "\" font-family=\"sans-serif\" font-size=\"20px\" text-anchor=\"middle\" fill=\"dimgrey\">Time</text>");

						ffw.write("<text x=\"" + (maxDiagLength / 2) + "\" y=\""
								+ (margin + ( nbmac-7) * rectWidth + marginBetweenRect + 30)
								+ "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\" fill=\"dimgrey\">"
								+ data.pName + " - makespan: " + (mkpn) + " - cost: " + costG + " - peak: " + peakG + " - energy: "
								+ energyG + " - GAP: " + (cplexGap) +" - CPU: " + (cplexTime) +"</text>");
						
					
						// Fin du fichier
						ffw.write("</svg>\n");
						ffw.close(); // fermer le fichier √  la fin des traitements
	}catch (Exception e) {
		System.out.println(e.getMessage());
		}
		}
	
	}
	
	
	
