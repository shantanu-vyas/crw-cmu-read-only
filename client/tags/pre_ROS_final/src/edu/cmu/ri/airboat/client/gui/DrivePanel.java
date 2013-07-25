/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DrivePanel.java
 *
 * Created on Jan 13, 2011, 3:11:53 PM
 */

package edu.cmu.ri.airboat.client.gui;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author pkv
 */
public class DrivePanel extends AbstractAirboatPanel {

    public static final int DEFAULT_UPDATE_MS = 750;
    public static final int DEFAULT_COMMAND_MS = 500;

    // Ranges for thrust and rudder signals
    public static final double THRUST_MIN = 0.0;
    public static final double THRUST_MAX = 2.0;
    public static final double RUDDER_MIN = -2.5;
    public static final double RUDDER_MAX = 2.5;

    // Sets up a flag limiting the rate of velocity command transmission
    public AtomicBoolean _sentVelCommand = new AtomicBoolean(false);
    public AtomicBoolean _queuedVelCommand = new AtomicBoolean(false);

    /** Creates new form DrivePanel */
    public DrivePanel() {
        initComponents();
        setUpdateRate(DEFAULT_UPDATE_MS);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRudder = new javax.swing.JSlider();
        jThrust = new javax.swing.JSlider();
        jRudderBar = new javax.swing.JProgressBar();
        jThrustBar = new javax.swing.JProgressBar();
        jAutonomyBox = new javax.swing.JCheckBox();
        jConnectedBox = new ReadOnlyCheckBox();

        jRudder.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRudderStateChanged(evt);
            }
        });

        jThrust.setOrientation(javax.swing.JSlider.VERTICAL);
        jThrust.setValue(0);
        jThrust.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jThrustStateChanged(evt);
            }
        });

        jRudderBar.setValue(50);

        jThrustBar.setOrientation(1);

        jAutonomyBox.setText("Autonomous");
        jAutonomyBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jAutonomyBoxMouseClicked(evt);
            }
        });

        jConnectedBox.setForeground(new java.awt.Color(51, 51, 51));
        jConnectedBox.setText("Connected");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jThrust, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jThrustBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jAutonomyBox)
                            .addComponent(jConnectedBox)))
                    .addComponent(jRudder, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addComponent(jRudderBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jThrustBar, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .addComponent(jThrust, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jAutonomyBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jConnectedBox)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRudder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRudderBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jThrustStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jThrustStateChanged
        updateVelocity();
    }//GEN-LAST:event_jThrustStateChanged

    private void jRudderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRudderStateChanged
        updateVelocity();
    }//GEN-LAST:event_jRudderStateChanged

    private void jAutonomyBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jAutonomyBoxMouseClicked
        _control.setAutonomous(jAutonomyBox.isSelected());
    }//GEN-LAST:event_jAutonomyBoxMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAutonomyBox;
    private javax.swing.JCheckBox jConnectedBox;
    private javax.swing.JSlider jRudder;
    private javax.swing.JProgressBar jRudderBar;
    private javax.swing.JSlider jThrust;
    private javax.swing.JProgressBar jThrustBar;
    // End of variables declaration//GEN-END:variables

    // Callback that handles GUI events that change velocity
    protected void updateVelocity() {
        // Check if there is already a command queued up, if not, queue one up
        if (!_sentVelCommand.getAndSet(true)) {

            // Send one command immediately
            sendVelocity();

            // Queue up a command at the end of the refresh timestep
            _timer.schedule(new UpdateVelTask(), DEFAULT_COMMAND_MS);
        } else {
            _queuedVelCommand.set(true);
        }
    }

    // Simple update task that periodically checks whether velocity needs updating
    class UpdateVelTask extends TimerTask {
        @Override
        public void run() {
            if (_queuedVelCommand.getAndSet(false)) {
                sendVelocity();
                _timer.schedule(new UpdateVelTask(), DEFAULT_COMMAND_MS);
            } else {
                _sentVelCommand.set(false);
            }
        }
    }

    // Sets velocities from sliders to control proxy
    protected void sendVelocity() {
        double[] vels = new double[6];
        vels[0] = fromProgressToRange(jThrust.getValue(), THRUST_MIN, THRUST_MAX);
        vels[5] = fromProgressToRange(jRudder.getValue(), RUDDER_MIN, RUDDER_MAX);
        _control.setVelocity(vels);
    }

    // Converts from progress bar value to linear scaling between min and max
    private double fromProgressToRange(int progress, double min, double max) {
            return (min + (max - min) * ((double)progress)/100.0);
    }

    // Converts from progress bar value to linear scaling between min and max
    private int fromRangeToProgress(double value, double min, double max) {
            return (int)(100.0 * (value - min)/(max - min));
    }

    /**
     * Performs periodic update of GUI elements
     */
    public void update() {
        if (_control != null) {
            try {
                jAutonomyBox.setSelected(_control.isAutonomous());
                jAutonomyBox.setEnabled(true);
                jConnectedBox.setSelected(_control.isConnected());
                jAutonomyBox.setEnabled(true);

                double[] vels = _control.getVelocity();
                jThrustBar.setValue(fromRangeToProgress(vels[0], THRUST_MIN, THRUST_MAX));
                jRudderBar.setValue(fromRangeToProgress(vels[5], RUDDER_MIN, RUDDER_MAX));

            } catch (java.lang.reflect.UndeclaredThrowableException ex) {
                jAutonomyBox.setEnabled(false);
                jConnectedBox.setEnabled(false);

                jThrustBar.setValue(0);
                jRudderBar.setValue(0);
            }

            DrivePanel.this.repaint();
        }
    }
}
