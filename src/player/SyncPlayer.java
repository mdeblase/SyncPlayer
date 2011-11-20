package player;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import ui.MediaControlPanel;
import ui.SyncDialog;
import uk.co.caprica.vlcj.filter.swing.SwingFileFilterFactory;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.DefaultFullScreenStrategy;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.FullScreenStrategy;

import com.sun.jna.NativeLibrary;

public class SyncPlayer implements ActionListener{
    private JFrame frame;
    private Canvas videoSurface;
    private MediaControlPanel videoControlsPanel;
    private MediaControlPanel audioControlsPanel;
    private JPanel controlsPanel;
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer videoPlayer;
    private MediaPlayer audioPlayer;
    private JMenuItem openMovieMenuItem = new JMenuItem("Open Movie File...");
    private JMenuItem openAudioMenuItem = new JMenuItem("Open Audio File...");
    private JMenuItem openDiscMenuItem = new JMenuItem("Open Disc...");
    private JMenuItem exitMenuItem = new JMenuItem("Exit");
    private JMenuItem syncMenuItem = new JMenuItem("Sync To Time Interval...");
    
    private JFileChooser chooser = new JFileChooser();

    public static void main(final String[] args) throws Exception {
        NativeLibrary.addSearchPath("vlc",
                "/Applications/VLC.app/Contents/MacOS/lib");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SyncPlayer(); 
            }
        });
    }

    public SyncPlayer() {
        videoSurface = new Canvas();
        videoSurface.setBackground(Color.BLACK);
        videoSurface.setSize(800, 600);

        List<String> vlcArgs = new ArrayList<String>();
        vlcArgs.add("--no-plugins-cache");
        vlcArgs.add("--no-video-title-show");
        vlcArgs.add("--no-snapshot-preview");
        vlcArgs.add("--quiet");
        vlcArgs.add("--quiet-synchro");
        vlcArgs.add("--vout=macosx");// don't need this guy for windows
        vlcArgs.add("--global-key-nav-activate=10");
        vlcArgs.add("--global-key-nav-up=38");
        vlcArgs.add("--global-key-nav-down=40");
        vlcArgs.add("--global-key-nav-left=37");
        vlcArgs.add("--global-key-nav-right=39");

        frame = new JFrame("SyncPlayer");
        FullScreenStrategy fullScreenStrategy = new DefaultFullScreenStrategy(
                frame);

        mediaPlayerFactory = new MediaPlayerFactory(vlcArgs.toArray(new String[vlcArgs.size()]));
        mediaPlayerFactory.setUserAgent("SyncPlayer");
        videoPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer(fullScreenStrategy);
        videoPlayer.setVideoSurface(mediaPlayerFactory.newVideoSurface(videoSurface));
        videoPlayer.setPlaySubItems(true);
        videoPlayer.setEnableKeyInputHandling(false);
        videoPlayer.setEnableMouseInputHandling(false);

        audioPlayer = mediaPlayerFactory.newHeadlessMediaPlayer();

        controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel,
                BoxLayout.PAGE_AXIS));
        videoControlsPanel = new MediaControlPanel(videoPlayer);
        audioControlsPanel = new MediaControlPanel(audioPlayer);
        controlsPanel.add(videoControlsPanel);
        controlsPanel.add(audioControlsPanel);
        
        frame.setLayout(new BorderLayout());
        frame.setBackground(Color.GRAY);
        frame.add(videoSurface, BorderLayout.CENTER);
        frame.add(controlsPanel, BorderLayout.SOUTH);
        frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                if (videoPlayer != null) {
                    videoPlayer.release();
                    videoPlayer = null;
                }
                
                if (audioPlayer != null) {
                    audioPlayer.release();
                    audioPlayer = null;
                }

                if (mediaPlayerFactory != null) {
                    mediaPlayerFactory.release();
                    mediaPlayerFactory = null;
                }
                System.exit(0);
            }
        });
        //Get global key events Not working so far, might need vlc 1.2 native libraries
        //this might cause other issues with OSX
        
        /*
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
              if(event instanceof KeyEvent) {
                KeyEvent e = (KeyEvent)event;
                if(e.getID() == KeyEvent.KEY_PRESSED) {
                    int code = e.getKeyCode();
                    System.out.println(code);
                    
                    //This will only work with VLC 1.2
        
                    if (code == KeyEvent.VK_UP) {
                        videoPlayer.menuUp();
                    } else if (code == KeyEvent.VK_DOWN) {
                        System.out.println("In there");
                        videoPlayer.menuDown();
                    } else if (code == KeyEvent.VK_LEFT) {
                        videoPlayer.menuLeft();
                    } else if (code == KeyEvent.VK_RIGHT) {
                        videoPlayer.menuRight();
                    } else if (code == KeyEvent.VK_ENTER) {
                        System.out.println("entered");
                        videoPlayer.menuActivate();
                    } 
                }
              }
            }
          }, AWTEvent.KEY_EVENT_MASK);
         */
        
        
        frame.setVisible(true);
    }// end SyncPlayer

    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu mediaMenu = new JMenu("File");
        mediaMenu.setMnemonic('f');

        openMovieMenuItem.setMnemonic('m');
        openMovieMenuItem.addActionListener(this);
        mediaMenu.add(openMovieMenuItem);

        openAudioMenuItem.setMnemonic('a');
        openAudioMenuItem.addActionListener(this);
        mediaMenu.add(openAudioMenuItem);
        
        openDiscMenuItem.setMnemonic('d');
        openDiscMenuItem.addActionListener(this);
        mediaMenu.add(openDiscMenuItem);

        mediaMenu.add(new JSeparator());

        exitMenuItem.setMnemonic('x');
        exitMenuItem.addActionListener(this);
        mediaMenu.add(exitMenuItem);

        menuBar.add(mediaMenu);
        
        JMenu syncMenu = new JMenu("Sync");
        syncMenu.setMnemonic('s');
        
        syncMenuItem.addActionListener(this);
        syncMenu.add(syncMenuItem);
        
        menuBar.add(syncMenu);
        
        return menuBar;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openMovieMenuItem) {
            chooser.setFileFilter(SwingFileFilterFactory.newVideoFileFilter());

            int result = chooser.showOpenDialog(frame);

            // if image file accepted, set it as icon of the label
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getPath();
                videoControlsPanel.setMediaName(chooser.getSelectedFile().getName());
                videoPlayer.playMedia(path);
            }

        } else if (e.getSource() == openAudioMenuItem) {
            chooser.setFileFilter(SwingFileFilterFactory.newAudioFileFilter());
            int result = chooser.showOpenDialog(frame);

            // if image file accepted, set it as icon of the label
            if (result == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getPath();
                audioControlsPanel.setMediaName(chooser.getSelectedFile().getName());
                audioPlayer.playMedia(path);
            }
        } else if (e.getSource() ==  openDiscMenuItem) {
            videoPlayer.playMedia("dvd:////dev/rdisk1");
            
        }
        
        
        else if (e.getSource() == exitMenuItem) {
            if (videoPlayer != null) {
                videoPlayer.release();
                videoPlayer = null;
            }
            
            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            }

            if (mediaPlayerFactory != null) {
                mediaPlayerFactory.release();
                mediaPlayerFactory = null;
            }
            System.exit(0);
        } else if (e.getSource() == syncMenuItem) {
            SyncDialog sd = new SyncDialog(videoPlayer, audioPlayer);
            sd.setVisible(true);
        }

    }

}
