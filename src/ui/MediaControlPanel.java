package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import uk.co.caprica.vlcj.binding.LibVlcConst;
import uk.co.caprica.vlcj.player.MediaPlayer;

public class MediaControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final MediaPlayer mediaPlayer;
    private JLabel timeLabel;
    private JLabel mediaNameLabel;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton playButton;
    private JSlider volume;

    public MediaControlPanel(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        init();
        executorService.scheduleAtFixedRate(new UpdateRunnable(mediaPlayer), 0L, 1L, TimeUnit.SECONDS);
    }

    private void init() {
        // create components
        mediaNameLabel = new JLabel("");
        timeLabel = new JLabel("hh:mm:ss");
        stopButton = new JButton("Stop");
        stopButton.setToolTipText("Stop");

        pauseButton = new JButton("Pause");
        pauseButton.setToolTipText("Play/pause");

        playButton = new JButton("Play");
        playButton.setToolTipText("Play");
        
        volume = new JSlider();
        volume.setOrientation(JSlider.HORIZONTAL);
        volume.setMinimum(LibVlcConst.MIN_VOLUME);
        volume.setMaximum(LibVlcConst.MAX_VOLUME);
        volume.setPreferredSize(new Dimension(100, 40));
        volume.setToolTipText("Change volume");

        // layout components
        setBorder(new EmptyBorder(4, 4, 4, 4));
        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(mediaNameLabel);
        buttonPanel.add(stopButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(playButton);
        buttonPanel.add(timeLabel);
        buttonPanel.add(volume);

        add(buttonPanel, BorderLayout.SOUTH);

        // add listeners
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.stop();
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.pause();
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mediaPlayer.play();
            }
        });
        
        volume.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                mediaPlayer.setVolume(source.getValue());
            }
        });

    }
    
    public void setMediaName(String name) {
        mediaNameLabel.setText(name);
    }

    private final class UpdateRunnable implements Runnable {

        private final MediaPlayer mediaPlayer;

        private UpdateRunnable(MediaPlayer mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
        }

        @Override
        public void run() {
            final long time = mediaPlayer.getTime();

            // Updates to user interface components must be executed on the
            // Event
            // Dispatch Thread
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    String s = String.format(
                            "%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(time),
                            TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time)),
                            TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
                    timeLabel.setText(s);

                }
            });
        }
    }

    
}
