package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import uk.co.caprica.vlcj.player.MediaPlayer;
import util.SpringUtilities;

@SuppressWarnings("serial")
public class SyncDialog extends JDialog {

    private MediaPlayer videoPlayer;
    private MediaPlayer audioPlayer;
    private JPanel formPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private JSpinner minuteSpin;
    private JSpinner secondSpin;
    private JButton syncButton;
    private String[] sources = { "Audio", "Video" };
    private JComboBox sourceBox = new JComboBox(sources);

    public SyncDialog(MediaPlayer movie, MediaPlayer audio) {
        super();
        this.setSize(270,200);
        this.setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        this.videoPlayer = movie;
        this.audioPlayer = audio;
        this.init();
    }

    private void init() {
        formPanel.setLayout(new SpringLayout());
        JLabel sourceLabel = new JLabel("Source To Sync Ahead:");
        JLabel minuteLabel = new JLabel("Minutes:");
        JLabel secondLabel = new JLabel("Seconds:");
        SpinnerModel minModel = new SpinnerNumberModel(0,0,59,1);
        SpinnerModel secModel = new SpinnerNumberModel(0,0,59,1);
        minuteSpin = new JSpinner(minModel);
        secondSpin = new JSpinner(secModel);
        formPanel.add(sourceLabel);
        formPanel.add(sourceBox);
        formPanel.add(minuteLabel);
        formPanel.add(minuteSpin);
        formPanel.add(secondLabel);
        formPanel.add(secondSpin);
        SpringUtilities.makeCompactGrid(formPanel, 3, 2, 10, 10, 6, 10);
        this.add(formPanel, BorderLayout.CENTER);
        
        buttonPanel.setLayout(new FlowLayout());
        syncButton = new JButton("Sync");
        buttonPanel.add(syncButton);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        syncButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int minutes = (Integer)minuteSpin.getValue();
                int seconds = (Integer)secondSpin.getValue();
                if(minutes == 0 && seconds == 0) {
                    showError();

                } else {
                    syncPlayers(minutes, seconds);
                }
            } 
        });
    }

    protected void showError() {
        JOptionPane.showMessageDialog(this,
                "Please enter a value greater than 0 for Minutes and/or Seconds",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    protected void syncPlayers(int minutes, int seconds) {
        //Get total sync value in milliseconds
        long syncTime = TimeUnit.MINUTES.toMillis(minutes) + TimeUnit.SECONDS.toMillis(seconds);
        //pause both players
        videoPlayer.pause();
        audioPlayer.pause();
        //if syncing audio, add sync time to current video time
        //set audio time to his total
        if ("Audio".equals((String)sourceBox.getSelectedItem())) {
            long curVideoTime = videoPlayer.getTime();
            long newAudioTime = curVideoTime + syncTime;
            audioPlayer.setTime(newAudioTime);
        } else if("Video".equals((String)sourceBox.getSelectedItem())){
            long curAudioTime = audioPlayer.getTime();
            long newVideoTime = curAudioTime + syncTime;
            videoPlayer.setTime(newVideoTime);
        }
        //play both media players, then dispose
        audioPlayer.play();
        videoPlayer.play();
        dispose();
    }

}
