package di.uniba.it.lodrecsys.graph.scorer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Graph;

/**
 * Class which defines a  method that is used in order
 * to distribute weights to the nodes.
 * In particular, 80% of the weights is distributed equally among
 * positively rated items and LODs associate to positive items.
 *  20% is distributed among all the other items.
 */


public class LODPriorVertexTransformer implements Transformer<String, Double>{
	private Set<String> trainingPos;

    private Set<String> trainingNeg;

    private int graphSize;

    private double massProb = 0.8;
    
    
    String proportion;
    //weight to LODs associate to positive items
    private double massProbLod;
    //weight to LODs associate to negative items
    private double massProbItemID;
    
    private final Map<String, String> uriIdMap;
    
    private Set<String> positiveProperty;


    public LODPriorVertexTransformer (Set<String> trainingPos, Set<String> trainingNeg, int graphSize, Map<String, String> uriIdMap) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.uriIdMap = uriIdMap;
    }

    public LODPriorVertexTransformer (Set<String> trainingPos, Set<String> trainingNeg, int graphSize, double massProb) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.massProb = massProb;
        this.uriIdMap = new HashMap<>();
    }

    public LODPriorVertexTransformer (Set<String> trainingPos, Set<String> trainingNeg, int graphSize, double massProb, Map<String, String> uriIdMap) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.uriIdMap = uriIdMap;
        this.massProb = massProb;
    }
    
    public LODPriorVertexTransformer (Set<String> trainingPos, Set<String> trainingNeg, int graphSize, Set<String> positiveProperty, double massProb, Map<String, String> uriIdMap) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.uriIdMap = uriIdMap;
        this.massProb = massProb;
        this.positiveProperty=positiveProperty;
    }
    
    public LODPriorVertexTransformer (Set<String> trainingPos, Set<String> trainingNeg, int graphSize, Set<String> positiveProperty, double massProb, double massProbItemID, double massProbLod) {
        this.trainingPos = trainingPos;
        this.trainingNeg = trainingNeg;
        this.graphSize = graphSize;
        this.uriIdMap = new HashMap<>();
        this.massProb = massProb;
        this.positiveProperty=positiveProperty;
        this.massProbItemID=massProbItemID;
        this.massProbLod=massProbLod;
    }

    @Override
    public Double transform(String node) {
        String resourceID = uriIdMap.get(node),
                itemID = (resourceID == null) ? node : resourceID;
     
        boolean containsPos = trainingPos.contains(itemID),
        		lod=positiveProperty.contains(itemID),
                containsNeg = false;

        if (!containsPos)
            containsNeg = trainingNeg.contains(itemID);


        if (containsPos) {
            return massProbItemID / (double) (trainingPos.size());
            
        } else if (containsNeg) {
            return 0d;
        } else if (lod){
        	
        	return massProbLod / (double) (positiveProperty.size());
        	
        } else {
        	//System.out.println("else:"+ (1 - massProb) / (double) (graphSize - positiveProperty.size()-(trainingPos.size() + trainingNeg.size())));
            return (1 - massProb) / (double) (graphSize - positiveProperty.size()-(trainingPos.size() + trainingNeg.size()));
        }
    }

    public int getGraphSize() {
        return graphSize;
    }

    public void setGraphSize(int graphSize) {
        this.graphSize = graphSize;
    }

    public double getMassProb() {
        return massProb;
    }

    public void setMassProb(double massProb) {
        this.massProb = massProb;
    }
    

}

