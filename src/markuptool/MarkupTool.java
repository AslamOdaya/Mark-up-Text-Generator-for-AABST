

    package markuptool;

    import javax.swing.UIManager;
    import javax.swing.UnsupportedLookAndFeelException;

    /**
     *
     * @author Aslam    
     */
    public class MarkupTool {


        public static void main(String[] args) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                      try {
            //make the GUI look like the default OS GUI.
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(       ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        }
                   new GUI().setVisible(true);
                }
            });
        }

        }


