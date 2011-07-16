package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VideoControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final EmbeddedMediaPlayer mediaPlayer;
    private JLabel timeLabel;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton playButton;

    public VideoControlPanel(EmbeddedMediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;

        init();
        executorService.scheduleAtFixedRate(new UpdateRunnable(mediaPlayer), 0L, 1L, TimeUnit.SECONDS);

    }

    private void init() {
        // create components
        timeLabel = new JLabel("hh:mm:ss");
        stopButton = new JButton("Stop");
        stopButton.setToolTipText("Stop");

        pauseButton = new JButton("Pause");
        pauseButton.setToolTipText("Play/pause");

        playButton = new JButton("Play");
        playButton.setToolTipText("Play");

        // layout components
        setBorder(new EmptyBorder(4, 4, 4, 4));

        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();

        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(stopButton);
        buttonPanel.add(pauseButton);
        buttonPanel.add(playButton);
        buttonPanel.add(timeLabel);

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
                            TimeUnit.MILLISECONDS.toMinutes(time)
                                    - TimeUnit.HOURS
                                            .toMinutes(TimeUnit.MILLISECONDS
                                                    .toHours(time)),
                            TimeUnit.MILLISECONDS.toSeconds(time)
                                    - TimeUnit.MINUTES
                                            .toSeconds(TimeUnit.MILLISECONDS
                                                    .toMinutes(time)));
                    timeLabel.setText(s);

                }
            });
        }
    }

}
