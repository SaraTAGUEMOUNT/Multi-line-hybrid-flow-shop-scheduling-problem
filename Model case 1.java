package test;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import ilog.concert.*; //modeling objects interface
import ilog.cplex.*;// develop and deploy optimization models
import ilog.concert.IloException;
import ilog.concert.IloIntVar;

public class Model {
	int mkpn;
	int peakW;
	int costE;
	int energE;
	double cplexGap;
	double cplexTime;
	IloCplex cplex;
	IloIntVar [][][][][] startDate;
	IloIntVar cmax;
	IloIntVar cost;
	IloIntVar energy;
	int peak;
	
	public Model (Data data, boolean isCmax, int cmaxBound, int puissanceMax, int energyMax, int timeMax) throws IloException {
	
		//a class to solve lp problems 
		cplex = new IloCplex();
		//declartion des variables int s c et  boolean x y 
		data.upperBound = cmaxBound;//a fixer pour tests
		data.setTarification(cmaxBound);
	
		//dÈclaration des variables x start date et end date (allocation mÈmoire et dÈclarer les valeurs numvar;;	
		startDate = new IloIntVar[data.nbmaxjob][data.nbmaxligne][data.nbmaxetage][data.nbmaxmachine][data.upperBound];
				
		IloIntVar cmax = cplex.intVar(0, data.upperBound+5); cmax.setName("cmax");
		IloIntVar cost = cplex.intVar(0,9999999);cost.setName("cost");
		IloIntVar energy = cplex.intVar(0,9999999);energy.setName("energy");
		for (int j = 0; j < data.nbmaxjob ; j++) {	 
			for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
				for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l] ; op++) {
					for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
						for (int t = 0; t < data.upperBound - data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; t++) {
							startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t] =  cplex.boolVar();
							startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t].setName("startDate_"+j+"_"+data.lineNumberForJob[j][l]+"_"+op+"_"+data.machineNumberForOperationOnLigneForJob[j][l][op][m]+"_"+t);
						}
					}
				}
			}
		}
			
		
		
		// #############################################################################################################################################################
		// #############################################################################################################################################################
		//													CONSTRAINTS
		// #############################################################################################################################################################
		// #############################################################################################################################################################
	
		// une seule date de dÈbut pour une opÈration si affectÈe ‡ la ligne, 0 sinon				
		for (int j = 0; j < data.nbmaxjob ; j++) {	 
			IloLinearNumExpr sum1 = cplex.linearNumExpr();
			for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
				for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][0] ; m++) {
					for (int t = 0; t < data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j][l][0][m] ; t++) {
						sum1.addTerm(1.0, startDate[j][data.lineNumberForJob[j][l]][0][data.machineNumberForOperationOnLigneForJob[j][l][0][m]][t]);
					}
				}
			}
			cplex.addEq(sum1, 1).setName("c2_"+j+"_"+0);
		}
		
		//assurer respect gamme opÈratoire		
		for (int j = 0; j < data.nbmaxjob ; j++) {	 
			for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
				for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l]-1 ; op++) {
					for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
						for (int t = 0; t < data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; t++) {	//-data.durationOnMachineForOperationOnLigneForJob[j][l][op1+1][m]
							IloLinearNumExpr sum1 = cplex.linearNumExpr();	
							for (int mm = 0; mm < data.nbMachinesForOperationOnLigneForJob[j][l][op+1] ; mm++) {
								if (t+data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]+data.durationOnMachineForOperationOnLigneForJob[j][l][op+1][mm]<data.upperBound) {
									for (int tt = t+data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; tt < data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j][l][op+1][mm]; tt++) {	//-data.durationOnMachineForOperationOnLigneForJob[j][l][op1+1][m]
										sum1.addTerm(1.0, startDate[j][data.lineNumberForJob[j][l]][op+1][data.machineNumberForOperationOnLigneForJob[j][l][op+1][mm]][tt]);	
									}
								}
							}
							cplex.addLe(startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t], sum1).setName("c3_"+j+"_"+data.lineNumberForJob[j][l]+"_"+op+"_"+(op+1)+"_"+data.machineNumberForOperationOnLigneForJob[j][l][op][m]+"_"+t);	
						}
					}
				}
			}
		}

		// une machine ne peut executer qu'un job ‡ la fois : avec StartDate
		for (int j1 = 0; j1 < data.nbmaxjob ; j1++) {
			for (int l1 = 0; l1 < data.nbLignesForJob[j1] ; l1++) {
				for (int op1 = 0; op1 < data.nbOperationsOnLigneForJob[j1][l1] ; op1++) {
					for (int m1 = 0; m1 < data.nbMachinesForOperationOnLigneForJob[j1][l1][op1] ; m1++) {
						int TmaxJ1 = data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j1][l1][op1][m1];
						for (int t = 0; t < TmaxJ1 ; t++) {
							for (int j2 = 0; j2 < data.nbmaxjob ; j2++) {
								IloLinearNumExpr sum1 = cplex.linearNumExpr();
								for (int l2 = 0; l2 < data.nbLignesForJob[j2] ; l2++) {
									for (int op2 = 0; op2 < data.nbOperationsOnLigneForJob[j2][l2] ; op2++) {
										for (int m2 = 0; m2 < data.nbMachinesForOperationOnLigneForJob[j2][l2][op2] ; m2++) {
											if(j1!=j2 && data.machineNumberForOperationOnLigneForJob[j1][l1][op1][m1]==data.machineNumberForOperationOnLigneForJob[j2][l2][op2][m2] ) {
												int maxTT = t+data.durationOnMachineForOperationOnLigneForJob[j1][l1][op1][m1];
												//if (maxTT<data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j2][l2][op2][m2]) {
												for (int tt = t; tt < maxTT; tt++) {
													if (tt<data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j2][l2][op2][m2])
														sum1.addTerm(1.0, startDate[j2][data.lineNumberForJob[j2][l2]][op2][data.machineNumberForOperationOnLigneForJob[j2][l2][op2][m2]][tt]);
												}
												//}
											}
										}
									}
								}
								cplex.addLe(sum1, cplex.sum(1,cplex.prod(-1,startDate[j1][data.lineNumberForJob[j1][l1]][op1][data.machineNumberForOperationOnLigneForJob[j1][l1][op1][m1]][t]))).
								setName("c4_"+j1+"_"+j2+"_"+data.lineNumberForJob[j1][l1]+"_"+op1+"_"+data.machineNumberForOperationOnLigneForJob[j1][l1][op1][m1]+"_"+t);
							
								}
							}
					}
				}
			}
		}
		
		//definition cmax
		for (int j = 0; j < data.nbmaxjob ; j++) {	 
			for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
				int op = data.nbOperationsOnLigneForJob[j][l]-1;
				for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
					IloLinearNumExpr sum1 = cplex.linearNumExpr();
					for (int t = 0; t < data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j][l][op][m] ; t++) {
						sum1.addTerm(t+data.durationOnMachineForOperationOnLigneForJob[j][l][op][m], startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]);
					}
					
					cplex.addLe(sum1, cmax).setName("c5_"+j+"_"+data.lineNumberForJob[j][l]+"_"+op+"_"+data.machineNumberForOperationOnLigneForJob[j][l][op][m]);	
				}
			}
		}
		cplex.addLe(cmax, cmaxBound).setName("c6");	//borne sur le makespan
		
		
		// power limitation constraint
		for (int t = 0; t < data.upperBound; t++) {
			IloLinearNumExpr sum1 = cplex.linearNumExpr();
			for (int j = 0; j < data.nbmaxjob ; j++) {	 
				for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
					for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l]; op++) {
						for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
							for (int tt = 0; tt < data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; tt++) {	//-data.durationOnMachineForOperationOnLigneForJob[j][l][op1+1][m]
								if(t-tt < data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]) {
									if(t-tt >= 0) {
									sum1.addTerm(data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][tt], 
										startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t-tt]);
									}
								}
							}
						}
					}
				}
			}
			cplex.addLe(sum1, puissanceMax).setName("c7_"+t);	
		}

		//A verifier/amÈliorer
		// constraint for ensuring no exceeding of energyMax in production (in future : add idleTimes)
		IloLinearNumExpr sum = cplex.linearNumExpr();
		for (int t = 0; t < data.upperBound; t++) {
			for (int j = 0; j < data.nbmaxjob ; j++) {	 
				for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
					for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l]; op++) {
						for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
							//energy consumption of producing operations
							if (t<data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]) {
								sum.addTerm(data.energyOnMachineForOperationOnLigneForJob[j][l][op][m],
									startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]);
							}
						}
					}
				}
			}
		}
		cplex.addLe(sum, energy).setName("c8");
		cplex.addLe(energy,energyMax).setName("c9"); //borne sur l'Ènergie
		
		// somme des couts
		IloLinearNumExpr sumCost = cplex.linearNumExpr();
		for (int j = 0; j < data.nbmaxjob ; j++) {	 
			for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
				for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l]; op++) {
					for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
						for (int t = 0; t < data.upperBound-data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; t++) {
							for (int tt = 0; tt < data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; tt++) {
								//energy consumption of producing operations
								sumCost.addTerm(data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][tt]*data.CostPerStepTime[t+tt],
									startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]);
							}
						}
					}
				}
			}
		}
		cplex.addLe(sumCost,cost).setName("c10");
		
		// des tests:
		//cplex.addLe(cost, 40000);
		/*cplex.addEq(startDate[1][0][0][0][0],1.0);
		cplex.addEq(startDate[0][0][0][0][5],1.0);
		cplex.addEq(startDate[2][0][0][1][0],1.0);*/
		if (isCmax) {
			cplex.addMinimize(cplex.sum(cplex.prod(data.weightCmax, cmax),cost));
			//cplex.addMinimize(cmax);
		} else {
			cplex.addMinimize(cost);
		}
		
		//cplex.addMinimize(energy);
		cplex.setParam(IloCplex.Param.TimeLimit, 600);
		cplex.exportModel("./Results/LP_FILES/"+data.pName+".lp"); //+"_"+(int)mining.getNbSigma()
		try {
			double start = cplex.getCplexTime();
			if(cplex.solve()) {
				int timeHorizon=(int) cplex.getValue(cmax);
				mkpn = 0;
				costE = (int) cplex.getValue(cost);
				energE = (int) cplex.getValue(energy);
				
		       
		        for (int j = 0; j < data.nbmaxjob ; j++) {	 
					for (int l = 0; l < data.nbLignesForJob[j] ; l++) {
						for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l] ; op++) {
							boolean isFound = false;
							for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
								for (int t = 0; t <= (timeHorizon-data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]) ; t++) {
									try {
										if (startDate[j][data.lineNumberForJob[j][l]]!=null) {
											if (startDate[j][data.lineNumberForJob[j][l]][op]!=null) {
												if (startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]]!=null) {
													if (startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]!=null) {
														// !! 0.99 < getValue() <1.01 pour combler les erreurs d'arrondis ...
														if(!isFound && (0.99<cplex.getValue(startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t])) && (cplex.getValue(startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t])<1.01)) {
															int sD = t;
															System.out.println("startDate_"+j+"_"+data.lineNumberForJob[j][l]+"_"+op+"_"+data.machineNumberForOperationOnLigneForJob[j][l][op][m]+"_" +t+" : "+cplex.getValue(startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]) );		
															isFound = true;
															if (mkpn < sD+data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]) {
																mkpn =sD+data.durationOnMachineForOperationOnLigneForJob[j][l][op][m];
															}
														}
													}
												}
											}
										}
									} catch (IloException iloe) {
										System.out.println(iloe.getMessage());
									}
								}
							}
						}						
					}
		        }
		        cplexTime = cplex.getCplexTime()-start;
		        cplexGap = cplex.getMIPRelativeGap()*100;
		        System.out.println("Solution status: " + cplex.getStatus());
				System.out.println("CPU: " + cplexTime+"s");
		        System.out.println("Gap: " + cplexGap+"%");
		        System.out.println("cmax: " + mkpn);
		        System.out.println("cost: " + costE);	
		        System.out.println("energy: " + energE);	
		        	
		        System.out.println();
				
			}
		} catch(IloException e) {
			e.printStackTrace();
		}
	}	
	
	public void writeGANTT_SVG(Data data) {
		int i, job, mac;
		int nbmac = data.nbmaxmachine, nbjob = data.nbmaxjob;
		int margin = 100;
		int marginBetweenRect = 2;
		int fleche = 20;
		int rectWidth = 50;
		int xStart = 0, xLength = 0, yStart = 0, yLength = 0;
		Random rand = new Random();

		try {
			File ff = new File("./Results/GANTT/"+data.pName + "_gantt.svg"); // d√©finir l'arborescence

			ff.createNewFile();
			FileWriter ffw = new FileWriter(ff);
			// ffw.write(data.pName + "\n"); // √©crire une ligne dans le fichier
			// resultat.txt

			/*----------------------------------------------------------------------
			Ratio pour taille des rectangles du diagramme
			----------------------------------------------------------------------*/
			int ratio = 2;
			
			  if (mkpn <= 30) { ratio = 20; } else if (mkpn < 100) { ratio = 10; } else if
			  (mkpn < 250) { ratio = 5; } else if (mkpn < 500) { ratio = 3; }
			 //mkpn++;
			int maxDiagLength = (int) ((ratio) * mkpn + 2 * margin + fleche + data.nbmaxmachine);
			int maxDiagHeight = (rectWidth + marginBetweenRect) * data.nbmaxmachine + margin + fleche + 10;

			ffw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
			ffw.write(" <svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"" + maxDiagLength + "\" height=\"" + maxDiagHeight + "\">\n");
			ffw.write("<title> Diagramme de Gantt du Probleme: " + data.pName + "</title>\n");
			ffw.write("<desc> Cette figure repr√©sente le planning des op√©rations du probl√®me trait√©.</desc>\n");

			/*----------------------------------------------------------------------
			    Cr√©ation de la palette de couleurs
			----------------------------------------------------------------------*/
			int[][] colors = new int[nbjob][3];
			rand.setSeed(1);
			for (i = 0; i < nbjob; ++i) {
				colors[i][0] = rand.nextInt(256);
				colors[i][1] = rand.nextInt(256);
				colors[i][2] = rand.nextInt(256);
			}
			int powerTab[] = new int[mkpn];int peakG=0;
			int costTab[] = new int[mkpn]; int costG=0; int energyG=0;
			for (int t = 0; t < mkpn; ++t) {
				powerTab[t]=0;costTab[t]=0;
			}

			for (int j = 0; j < data.nbmaxjob; j++) {
				for (int l=0; l < data.nbLignesForJob[j];++l) {
					for (int op = 0; op < data.nbOperationsOnLigneForJob[j][l] ; op++) {
						for (int m = 0; m < data.nbMachinesForOperationOnLigneForJob[j][l][op] ; m++) {
						
							boolean isFound = false;
				
							if (startDate[j]!= null) {
								if (startDate[j][data.lineNumberForJob[j][l]]!=null) {
									if (startDate[j][data.lineNumberForJob[j][l]][op]!=null) {
										if (startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]]!=null) {
											for (int t = 0; t <= mkpn - data.durationOnMachineForOperationOnLigneForJob[j][l][op][m] + 1 ; t++) {
												try {
												if (startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]!=null) {
													if(!isFound &&  (0.99 < cplex.getValue(startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]) && cplex.getValue(startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t])<1.01)) {
														//System.out.println("startDate_"+j+"_"+data.lineNumberForJob[j][l]+"_"+op+"_"+data.machineNumberForOperationOnLigneForJob[j][l][op][m]+"_" +t+" :" +cplex.getValue(startDate[j][data.lineNumberForJob[j][l]][op][data.machineNumberForOperationOnLigneForJob[j][l][op][m]][t]));		
														
														job = j;
														mac = data.machineNumberForOperationOnLigneForJob[j][l][op][m];
														int endDate = t + data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]-1;
														int startDate = t;
														xStart = margin + (ratio) * (startDate) + marginBetweenRect; // +dd1*macVisited[mac]
														xLength = (ratio) * (data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]) - marginBetweenRect; // +dd1
														yStart = margin + mac * rectWidth + marginBetweenRect;
														yLength = rectWidth - marginBetweenRect;
				
														ffw.write("<rect style=\"fill:rgb(" + colors[job][0] + "," + colors[job][1] + "," + colors[job][2] + ");fill-opacity:0.5;\" width=\"" + xLength + "\" height=\"" + yLength + "\" x=\"" + xStart + "\" y=\"" + yStart + "\"/>\n");
				
														ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 15) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">J" + job + "</text>");
														ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 25) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">" + (startDate) + "</text>");
														ffw.write("<text x=\"" + ((xLength + 2 * xStart) / 2) + "\" y=\"" + (yStart + 35) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">" + (endDate+1) + "</text>");
				
														// construction power curve
														try {
															for (int p=0; p < data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; ++p) {
																powerTab[startDate+p]+=data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p];
																if (powerTab[startDate+p]>peakG)peakG=powerTab[startDate+p];
															}
															for (int p=0; p < data.durationOnMachineForOperationOnLigneForJob[j][l][op][m]; ++p) {
																costTab[startDate+p]+=data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p]*data.CostPerStepTime[startDate+p];
																costG+=data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p]*data.CostPerStepTime[startDate+p];
																energyG+=data.powerProfileForMachineOperationOnLigneForJob[j][l][op][m][p];
															}
														} catch (Exception e) {
															System.out.println(e.getMessage());
														}
														
														isFound = true;
													}
												}
												} catch (Exception e) {
													System.out.println(e.getMessage());
												}
												
											}
										}
									}
								}
							
							} 
							
						}
						
					}
						//}
					//}catch (IloException iloe) {System.out.println(iloe.getMessage());}
				}
			}

			
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
			ffw.write("<line x1=\"" + margin + "\" y1=\"" + (margin - fleche) + "\" x2=\"" + margin + "\" y2=\"" + (margin + nbmac * rectWidth + marginBetweenRect) + "\" stroke=\"dimgrey\" />");
			ffw.write("<line x1=\"" + margin + "\" y1=\"" + (margin - fleche) + "\" x2=\"" + (margin + 10) + "\" y2=\"" + (margin - fleche + 10) + "\" stroke=\"dimgrey\" />");

			for (i = 1; i <= nbmac; ++i) {
				ffw.write("<text x=\"" + (margin - 45) + "\" y=\"" + (margin - fleche + i * rectWidth) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\">L."+(data.lineForMachine[i-1]+1)+"-S."+(data.stageForMachine[i-1]+1)+"-M." + i + "</text>");

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
			ffw.write("<line x1=\"" + margin + "\" y1=\"" + (margin + nbmac * rectWidth + marginBetweenRect) + "\" x2=\"" + (margin + ratio * mkpn + fleche) + "\" y2=\"" + (margin + nbmac * rectWidth + marginBetweenRect) + "\" stroke=\"dimgrey\" />");
			ffw.write("<line x1=\"" + (margin + ratio * mkpn + fleche - 10) + "\" y1=\"" + (margin + nbmac * rectWidth + marginBetweenRect - 10) + "\" x2=\"" + (margin + ratio * mkpn + fleche) + "\" y2=\"" + (margin + nbmac * rectWidth + marginBetweenRect) + "\" stroke=\"dimgrey\" />");
			ffw.write("<line x1=\"" + (margin + ratio * mkpn) + "\" y1=\"" + (margin + nbmac * rectWidth + marginBetweenRect + 2) + "\" x2=\"" + (margin + ratio * mkpn) + "\" y2=\"" + (margin + nbmac * rectWidth + marginBetweenRect + 10) + "\" stroke=\"dimgrey\" />");

			ffw.write("<text x=\"" + (margin + 10) + "\" y=\"" + (margin - fleche - 5) + "\" font-family=\"sans-serif\" font-size=\"20px\" text-anchor=\"middle\" fill=\"dimgrey\">Machines</text>");
			ffw.write("<text x=\"" + (margin + ratio * mkpn + 2 * fleche) + "\" y=\"" + (margin + (nbmac) * rectWidth + marginBetweenRect) + "\" font-family=\"sans-serif\" font-size=\"20px\" text-anchor=\"middle\" fill=\"dimgrey\">Time</text>");

			ffw.write("<text x=\"" + (maxDiagLength / 2) + "\" y=\"" + (margin + ( nbmac-7) * rectWidth + marginBetweenRect + 40) + "\" font-family=\"sans-serif\" font-size=\"10px\" text-anchor=\"middle\" fill=\"dimgrey\">" + data.pName + " - makespan: " + (mkpn) +" - cost: " + costG +" - peak: " + peakG + " - energy: "+ energyG+" - GAP: " + cplexGap +" - CPU: " + cplexTime +"</text>");

			// Fin du fichier
			ffw.write("</svg>\n");
			ffw.close(); // fermer le fichier √† la fin des traitements
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
				
		
		
	
	

