package pack;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;


import pack.Data;
import pack.MersenneTwisterFast;
import pack.Meta ;
import pack.Sol ;


public class Main {
	static int instNum, nbInstances,  nbJobMin,  nbJobMax,  nbLignesMin,  nbLigneMax,  nbEtagesMin,  nbEtagesMax;
	static int nbMacMax,  dureeOpMin,  dureeOpMax,  puissanceMin,  puissanceMax,  nbProfilsCoutsMin;
	static int nbProfilsCoutsMax,  costMin,  costMax;
	static int makespan,  powerMax,  energyMax, timeSolveMax;
	static boolean isCmaxEstimate;
	static int test=0;
	static int cptInstances=0;
	
	public static void main(String[] args) {
		// instances names : to update if more than 100 instances to handle
		String[] pNames = new String[100];
	     try {
			
			String workingDir = System.getProperty("user.dir");
			System.out.println("Current working directory : " + workingDir);
			// get instances names
			InputStream ips = new FileInputStream(new File(workingDir+"/Instances/instances_list.txt"));
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
		
			while ((pNames[test++] = br.readLine()) != null);--test;

			boolean generateInstances = false;
			if (generateInstances) {
				Data madata = new Data();
				//generate nbInstances starting at number instNum
				//WARNING : can override already existing instances if instNum is not well chosen
				madata.generateInstances(instNum=1, nbInstances=3, nbJobMin=20, nbJobMax=20, nbLignesMin=3, nbLigneMax=3, nbEtagesMin = 4, nbEtagesMax=5, 
						nbMacMax=14, dureeOpMin=5, dureeOpMax=6, puissanceMin=10, puissanceMax=70, nbProfilsCoutsMin=5, nbProfilsCoutsMax=10, costMin=5, costMax=50);
			}
			
			//parcourir l'instances lignes par lignes
			//int rowpNames = pNames.length;
			for (int ins = 0; ins <1; ins++) {
				long obj = 99999999;
			
			for (int rp=1; rp<11; rp++) {
		
				MersenneTwisterFast mstf = new MersenneTwisterFast(rp);
				String pName = pNames[ins];
				
				pName = "instance_3";
				//pName.indexOf("str");
				
				//récupérer les valeurs des instances
			   // pName = "instance"+String.valueOf(ins);
				//pName = "instance_66";
				
				System.out.println("########################## READING INSTANCE #####################################");
				Data data = new Data(pName);
				
				System.out.println(" Problem Name:"+pName+"\n Upper Bound On Cmax:"+data.upperBound);
				// ############################# CONTRAINTES ###########################################
				int eMax = 30000; System.out.println("   Energy Max: "+eMax);
				int pMax=800;
				int cMax = 300; System.out.println("   Makespan Max: "+cMax);
				//cMax += 1; // !!!!!!!! penser à faire +1 sur la valeur du cmax pour des raisons de taille de structures de données
				data.upperBound = cMax;
				data.energyMax= eMax;
				data.powerMax = pMax;
				Sol bestSol = new Sol(data); Sol solTemp = new Sol(data);
				Sol solTemp2 = new Sol(data);
				Sol solSav=new Sol(data);
				//SolModelExactCas2 bestsol2=new SolModelExactCas2();
				//SolModeleExactCas3 bestsol3=new SolModeleExactCas3();
				//Meta meta = new Meta();
			
				bestSol.generateRandom(data, mstf);
				bestSol.decodage(data);
				
			  //bestSol.ModelExact(data, mstf);
				
			   //bestSol.ModelExactcas2(data, mstf);
				
			   //bestsol2.Model2(data, mstf);
			   //bestsol3.Model3(data, mstf);
			  
			 bestSol.heuristic(data, solTemp,solTemp2,solSav, 1000, mstf);
			 //bestSol.Heuristic2(data, solTemp,solSav, 50, mstf);
				//bestSol.writeGANTT_SVG(data);
	           if(bestSol.objfunction<obj) {
				bestSol.writeGANTT_SVG(data);
				obj= bestSol.objfunction;
			
			}
				
				System.out.println("########################## START INSTANCE SOLVE #####################################");
				System.out.println(" Problem Name:"+pName+"\n Upper Bound On Cmax:"+data.upperBound+"\n nop:"+data.size);
				System.out.println(" Writing Gantt File");
			   // bestSol.writeGANTT_SVG(data);
				
				
				//if(bestSol.costG<obj) {
				//bestSol.writeGANTT_SVG(data);
				//obj= bestSol.costG;
			
				//}
				System.out.println("nombre de rep :"+rp);
				System.out.println("########################## END INSTANCE SOLVE #######################################");
			}
			}	
			}catch (Exception e) {
				System.out.println("error1");
				e.printStackTrace();
           }
	}
}