package di.uniba.it.lodrecsys.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.rdf.model.Statement;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Rating;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.scorer.LODPriorVertexTransformer;
import di.uniba.it.lodrecsys.graph.scorer.SimpleVertexTransformer;
import di.uniba.it.lodrecsys.utility.MassPrior;
import di.uniba.it.lodrecsys.utils.Utils;
import di.uniba.it.lodrecsys.utils.mapping.PropertiesManager;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

/**
 * Class which represents the user-item-lods configuration with LODs weights
 */

public class UserItemPropLodPrior extends RecGraph {

	protected static String propFile = "C:/Users/Martina/Desktop/ML/properties/movies-genres.tsv";
	//protected static String propFile = "C:/Users/Martina/Desktop/ML/properties/movies-tags.tsv";
	//protected static String propFile = "C:/Users/Martina/Desktop/ML/properties/movies-tags-genres.tsv";
	private Map<String, Set<String>> properties;

	private ArrayListMultimap<String, Set<String>> trainingPosNeg;
	private Map<String, Set<String>> testSet;

	public UserItemPropLodPrior(String trainingFileName, String testFile) 
			throws IOException {
		generateGraph(new RequestStruct(trainingFileName, testFile, propFile));
	}


	public void generateGraph(RequestStruct requestStruct) throws IOException {
		String trainingFileName = (String) requestStruct.params.get(0), testFile = (String) requestStruct.params.get(1);
		String propFile = (String) requestStruct.params.get(2);
		
		properties = Utils.loadRatedItems(new File(propFile), true);

		trainingPosNeg = Utils.loadPosNegRatingForEachUser(trainingFileName);
		testSet = Utils.loadRatedItems(new File(testFile), false);
		

		// Loads all the items rated in the test set
		for (Set<String> ratings : testSet.values()) {
			for (String rate : ratings)
				recGraph.addVertex(rate);
		}
		
		
		for (String userID : trainingPosNeg.keySet()) {
			int edgeCounter = 0;
			int edgeCounterProp = 0;

			for (String posItemID : trainingPosNeg.get(userID).get(0)) {
				recGraph.addEdge(userID + "-" + edgeCounter, "U:" + userID, posItemID);
				if(properties.containsKey(posItemID)) {
					for(String prop : properties.get(posItemID)) {
						recGraph.addEdge(userID + "-" + posItemID + "-" + edgeCounterProp, posItemID, prop);
						edgeCounterProp++;
				}
				}
				edgeCounter++;

			}

		}
		
		currLogger.info("Vertex: " + recGraph.getVertexCount() + " Edges: "
				+ recGraph.getEdgeCount());
	}

	

	public Map<String, Set<Rating>> runPageRank(RequestStruct requestParam) {
		Map<String, Set<Rating>> usersRecommendation = new HashMap<>();

		double massProb = (double) requestParam.params.get(0); // max proportion
								       							// of positive
								       							// items for
								       							// user
		
		MassPrior mp = (MassPrior) requestParam.params.get(1); // proportion
																// prior between
																// items and LOD
		double itemsProb = mp.getItemsPrior();
		double LODProb = mp.getLodPrior();
		
		// print recommendation for all users
		int uId = 1;
		for (String userID : testSet.keySet()) {
			//if(uId >= 501) {
			currLogger.info("Page rank for user: " + userID);
			currLogger.info("Number:" + uId);
			List<Set<String>> posNegativeRatings = trainingPosNeg.get(userID);

			currLogger.info("Grandezza" + posNegativeRatings.size());
			Set<String> testItems = testSet.get(userID);
			
			Set<String> posProp = Utils.getPositivePropertyForUser(properties, posNegativeRatings.get(0));

			usersRecommendation.put(
					userID,
					profileUser(userID, posNegativeRatings.get(0), posNegativeRatings.get(1), testItems, massProb, posProp, itemsProb, LODProb));
			
			//}
			uId++;
			//if (uId >= 601) {//1501
				//break;
			//}
			
		}

		return usersRecommendation;
	}

	private Set<Rating> profileUser(String userID, Set<String> trainingPos,
			Set<String> trainingNeg, Set<String> testItems, double massProb,
			Set<String> posProperty, double itemsProb, double LODProb) {
		
		Set<Rating> allRecommendation = new TreeSet<>();

		LODPriorVertexTransformer transformer = new LODPriorVertexTransformer(
				trainingPos, trainingNeg, this.recGraph.getVertexCount(),
				posProperty, massProb, itemsProb, LODProb);
		PageRankWithPriors<String, String> priors = new PageRankWithPriors<>(
				this.recGraph, transformer, 0.15);

		priors.setMaxIterations(25);
		priors.evaluate();
		for (String currItemID : testItems) {
				allRecommendation.add(new Rating(currItemID, priors.getVertexScore(currItemID) + ""));
		}

		return allRecommendation;
	}

	public static void main(String[] args) throws IOException {
		String trainPath = "C:/Users/Martina/Desktop/ML/train.tsv",
			testPath = "C:/Users/Martina/Desktop/ML/test.tsv";
			

		long meanTimeElapsed = 0, startTime;

		for (int numSplit = 1; numSplit <= 1; numSplit++) {
			startTime = System.nanoTime();

			UserItemPropLodPrior graph = new UserItemPropLodPrior(trainPath, testPath);
			Map<String, Set<Rating>> ratings = graph.runPageRank(new RequestStruct(0.80, new MassPrior(0.6, 0.2)));

			Set<String> keySet = ratings.keySet();
			Iterator<String> it = keySet.iterator();
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(new File("C:\\Users\\Martina\\Desktop\\ML\\results\\raccomandazioniPRPprop1.tsv"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			StringBuilder builder = new StringBuilder();
			String ColumnNamesList = "user\titem\trating";
			builder.append(ColumnNamesList + "\n");

			while (it.hasNext()) {
				String key = it.next();
				System.out.println("user: " + key + " raccomandazioni: " + ratings.get(key));
				Set<Rating> rcm = ratings.get(key);
				Iterator<Rating> iteratorrcm = rcm.iterator();
				int itemCt = 1;
				while (iteratorrcm.hasNext()) {
					if(itemCt <= 10) {
						
						Rating i = iteratorrcm.next();
						System.out.println(i);
						builder.append(key + "\t");
						builder.append(i.getItemID() + "\t");
						builder.append(i.getRating() + "\n");
					}
					else
						break;
					itemCt++;
					
				}

				builder.append('\n');
			}
			meanTimeElapsed += (System.nanoTime() - startTime);
			try {
				pw.write(builder.toString());
			}catch(Exception e){
				System.out.print(e);
			}
			try {
				pw.close();
			}catch(Exception e){
				System.out.print(e);
			}

			System.out.println("done!");

		}

		meanTimeElapsed /= 5;
		currLogger.info("Total running time: " + meanTimeElapsed);
	}

}