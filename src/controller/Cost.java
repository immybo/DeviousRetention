package controller;

import java.io.Serializable;

/**
 * Created by Robert Campbell on 17/12/2017.
 */
public class Cost implements Serializable {
    public final static Cost NULL_COST = new Cost(0);

    public final int creditCost;
    public Cost(int creditCost) {
        this.creditCost = creditCost;
    }
}
