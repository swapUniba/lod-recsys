package di.uniba.it.lodrecsys.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.eval.EvaluateRecommendation;
import di.uniba.it.lodrecsys.eval.SparsityLevel;
import di.uniba.it.lodrecsys.utility.MassPrior;
import di.uniba.it.lodrecsys.utils.ExceptionErrorAlgorithm;
import di.uniba.it.lodrecsys.utils.Utils;

public class GraphRunnerLOD {

	private static Logger currLogger = Logger.getLogger(GraphRunner.class.getName());	
	
	private static void recoveryMethod(String args[], String[] method)throws ExceptionErrorAlgorithm{
		for (int i=0; i<args.length;i++){
			
			if(!(args[i].equals("UserItemPriorGraph")||args[i].equals("UserItemProperty")||
					args[i].equals("UserItemPropLodPrior")||args[i].equals("UserItemPropLodPriorNegative"))){
				
			throw new ExceptionErrorAlgorithm("Errore algoritmo inesistente: "+args[i]+".\n"+
					"I modelli supportati sono: UserItemPriorGraph, UserItemProperty, UserItemPropLodPrior, UserItemPropLodPriorNegative");			
			}else {
				method[i]=args[i];
			}
		}
	}

	public static void main(String[] args) throws IOException {
	String trainPath = "C:/Users/Martina/Desktop/ML/train.tsv", 
	       testPath = "C:/Users/Martina/Desktop/ML/test.tsv",
	       resultPath = "C:/Users/Martina/Desktop/ML/results"; 
	
	      
	//List<Map<String, String>> metricsForSplit = new ArrayList<>();
	//int[] listRecSizes = new int[] { 5, 10, 15, 20 };
	int numberOfSplit = 1;
	double massProb = 0.8;
	
	if (args.length<1){
		throw new ExceptionErrorAlgorithm("Errore: specificare le configurazioni da eseguire. \n"
				+ "Scegliere tra:UserItemPriorGraph, UserItemProperty,"
				+ "UserItemPropLodPrior, UserItemPropLodPriorNegative");
	
	}else {
		String[] method=new String[args.length];
		recoveryMethod(args, method);
	
	long meanTimeElapsed = 0, startTime;

	for (int numSplit = 1; numSplit <= numberOfSplit; numSplit++) {
		int m = 0;
		for (int j = 0; j < method.length; j++) {
			startTime = System.nanoTime();
			String trainFile = trainPath,
			       testFile = testPath;
			
			Pair<RecGraph, RequestStruct> pair = GraphFactory.create(method[m], trainFile, testFile, massProb);
			RecGraph userItemGraph = pair.key;
			RequestStruct requestStruct = pair.value;

			Map<String, Set<Rating>> ratings = userItemGraph.runPageRank(new RequestStruct(0.80));
			Set<String> keySet = ratings.keySet();
			Iterator<String> it = keySet.iterator();

			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new File(resultPath + File.separator + method[m] + ".csv"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			BufferedWriter buffer;
			buffer = new BufferedWriter(pw);

			// *******///
			int prog = 0;
			while (it.hasNext()) {
				String key = it.next();
				// System.out.println("user: " + key +
				// " raccomandazioni: " + ratings.get(key));

				Set<Rating> rcm = ratings.get(key);
				Iterator<Rating> iteratorrcm = rcm.iterator();
				while (iteratorrcm.hasNext()) {
					Rating i = iteratorrcm.next();
					// System.out.println(i);
					buffer.write(key + " Q0 " + i.getItemID() + " " + prog + " " + i.getRating() + " EXP " + "\n");
					prog++;

				}

			}
			meanTimeElapsed += (System.nanoTime() - startTime);
			currLogger.info("Time for " + method[m] + " :" + (double) meanTimeElapsed / 1000000000.0);
			buffer.flush();
			pw.close();
			System.out.println("done!");

			m++;
			
		}
			meanTimeElapsed /= 6;
			currLogger.info("Total running time: " + meanTimeElapsed);
	}	

}
}
}
