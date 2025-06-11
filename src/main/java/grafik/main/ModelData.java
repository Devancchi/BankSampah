package grafik.main;

/**
 *
 * @author RAVEN
 */
public class ModelData {

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getGrossProfit() {
        return grossprofit;
    }

    public void setGrossProfit(double grossprofit) {
        this.grossprofit = grossprofit;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public ModelData(String month, double grossprofit, double cost, double profit) {
        this.month = month;
        this.grossprofit = grossprofit;
        this.cost = cost;
        this.profit = profit;
    }

    public ModelData() {
    }

    private String month;
    private double grossprofit;
    private double cost;
    private double profit;
}
