package di.uniba.it.lodrecsys.utility;

//Classe che setta i pesi degli items e dei LODs ad essa associati
public class MassPrior {
	
	double itemsPrior;
	double LodPrior;
	
	public MassPrior(double itemsPrior,double LodPrior){
		this.itemsPrior=itemsPrior;
		this.LodPrior=LodPrior;
	}
	
	
	public void setItemsPrior(int itemsPrior){
		this.itemsPrior=itemsPrior;
	}
	
	public void setLodPrior(double LodPrior){
		this.LodPrior=LodPrior;
	}
	
	public double getItemsPrior(){
		return itemsPrior;
	}
	
	public double getLodPrior(){
		return LodPrior;
	}

}

