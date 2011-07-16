package player;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ui.VideoControlPanel;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;

import com.sun.jna.NativeLibrary;

public class SyncPlayer {
    private JFrame frame;
    private Canvas videoSurface;
    private JPanel videoControlsPanel;
    
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;

    public static void main(final String[] args) throws Exception {
        NativeLibrary.addSearchPath("vlc", "/Applications/VLC.app/Contents/MacOS/lib");
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            new SyncPlayer(args).start(""); //For now add the path to the movie file here, until menus and selection work.
          }
        });
      }

    public SyncPlayer(String[] args) {
        videoSurface = new Canvas();
        videoSurface.setBackground(Color.BLACK);
        videoSurface.setSize(800, 600);
        
        List<String> vlcArgs = new ArrayList<String>();
        vlcArgs.add("--no-plugins-cache");
        vlcArgs.add("--no-video-title-show");
        vlcArgs.add("--no-snapshot-preview");
        vlcArgs.add("--quiet");
        vlcArgs.add("--quiet-synchro");
        vlcArgs.add("--vout=macosx");//don't need this guy for windows
        
        frame = new JFrame("SyncPlayer");
        FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(frame);
        
        mediaPlayerFactory = new MediaPlayerFactory(vlcArgs.toArray(new String[vlcArgs.size()]));
        mediaPlayerFactory.setUserAgent("vlcj test player");
        mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer(fullScreenStrategy);
        mediaPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(videoSurface));
        mediaPlayer.setPlaySubItems(true);
        mediaPlayer.setEnableKeyInputHandling(false);
        mediaPlayer.setEnableMouseInputHandling(false);
        
        videoControlsPanel = new VideoControlPanel(mediaPlayer);
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.GRAY);
        frame.add(videoSurface, BorderLayout.CENTER);
        frame.add(videoControlsPanel, BorderLayout.SOUTH);
        //frame.setJMenuBar(buildMenuBar());
        frame.pack();
        frame.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent evt) {
            if(mediaPlayer != null) {
              mediaPlayer.release();
              mediaPlayer = null;
            }

            if(mediaPlayerFactory != null) {
              mediaPlayerFactory.release();
              mediaPlayerFactory = null;
            }
            System.exit(0);
          }
        });
        
        frame.setVisible(true);
        
        //add listeners to mediaPlayer  
    }//end SyncPlayer
    
    private void start(String file) {
        mediaPlayer.playMedia(file);
      }
}
