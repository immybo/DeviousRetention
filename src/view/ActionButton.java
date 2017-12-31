package view;

import controller.TrainAction;
import network.CTSConnection;

import javax.swing.*;

/**
 * Created by Robert Campbell on 31/12/2017.
 */
public class ActionButton extends JButton {
    public ActionButton(CTSConnection server, controller.Action a) {
        super();

        this.addActionListener((ae)->server.send(a));
        if (a instanceof TrainAction) {
            this.setText("Train " + ((TrainAction)a).unitType.name());
        } else {
            this.setText("Unknown action");
        }
    }
}
