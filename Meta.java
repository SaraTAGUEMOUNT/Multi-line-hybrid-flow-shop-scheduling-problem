package pack;

import pack.Data;
import pack.MersenneTwisterFast;
import pack.Sol;
import ilog.concert.IloException;

public class Meta {

	public Meta() {}

	public Sol iteratedGreedy(Data data, MersenneTwisterFast mstf) {
		Sol sol_temp = new Sol (data);
		Sol Sol_init = new Sol(data);
		Sol bestSol = new Sol(data);
		bestSol.objfunction=999999999;
		for (int i=0; i<data.size; i++) {
			
			/*Sol_init.generateRandom(data, mstf);
			Sol_init.decodage(data);
			try  {
				Sol_init.Exact(data, mstf);
			} catch (IloException iloe) {
				
			}
			Sol_init.LocalSearch(data,sol_temp, 1000, mstf);*/
			
			//comparaison entre les solutions et garder la meilleure solution
			
			//if (Sol_init.energyCost<bestSol.energyCost) {
			if (Sol_init.objfunction<bestSol.objfunction) {
				bestSol.copieSol(data, Sol_init);
			
		}
		
		
		
	}
		return bestSol;	
  }
}

