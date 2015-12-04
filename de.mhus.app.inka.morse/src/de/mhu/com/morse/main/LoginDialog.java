package de.mhu.com.morse.main;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import de.mhu.com.morse.eecm.MorseConnection;
import de.mhu.lib.eecm.model.EcmManager;
import de.mhu.lib.log.AL;
import de.mhu.lib.swing.ASwing;
import de.mhu.lib.swing.layout.TopGridLayout;

public class LoginDialog extends JPanel {
	
	private static final String ACTION_CANCEL = "cancel";
	private static final String ACTION_CONNECT = "connect";
	
	private static AL log = new AL(LoginDialog.class);
	
	private JTextField tService;
	private JTextField tHost;
	private JTextField tUser;
	private JPasswordField tPass;
	private JButton bConnect;
	private JButton bCancel;
	private Action action;
	private String result;

	public LoginDialog() {
		initUI();		
		
	}
	
	private void initUI() {
		setLayout( new TopGridLayout( 5, 2 ) );
		add( new JLabel( "Service: " ) );
		tService = new JTextField();
		add( tService );
		
		add( new JLabel( "Host: ") );
		tHost = new JTextField();
		add( tHost );
		
		add( new JLabel( "User: " ) );
		tUser = new JTextField();
		add( tUser );
		
		add( new JLabel( "Password: " ) );
		tPass = new JPasswordField();
		add( tPass );
		
		bConnect = new JButton( " Connect " );
		add( bConnect );
		bCancel = new JButton( " Cancel " );
		add( bCancel );
		
		bConnect.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionConnect();
			}
			
		});
		
		bCancel.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				actionCancel();
			}
			
		});
		
	}

	protected void actionCancel() {
		result = ACTION_CANCEL;
		fireActionPerformed( new ActionEvent( this, 0, ACTION_CANCEL ) );
	}

	protected void actionConnect() {
		result = ACTION_CONNECT;
		fireActionPerformed( new ActionEvent( this, 0, ACTION_CONNECT ) );		
	}
	
	public void addActionListener(ActionListener l) {
        listenerList.add(ActionListener.class, l);
    }
	
	public void removeActionListener(ActionListener l) {
		if ((l != null) && (getAction() == l)) {
		    setAction(null);
		} else {
		    listenerList.remove(ActionListener.class, l);
		}
    }
	
	public ActionListener[] getActionListeners() {
        return (ActionListener[])(listenerList.getListeners(
            ActionListener.class));
    }
    
	public void setAction(Action a) {
		action = a;
    }
	
	public Action getAction() {
		return action;
    }
	
	protected void fireActionPerformed(ActionEvent event) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==ActionListener.class) {
                // Lazily create the event:
                if (e == null) {
                      String actionCommand = event.getActionCommand();
                      
                      e = new ActionEvent( this,
                                          ActionEvent.ACTION_PERFORMED,
                                          actionCommand,
                                          event.getWhen(),
                                          event.getModifiers());
                }
                ((ActionListener)listeners[i+1]).actionPerformed(e);
            }          
        }
    }
	
	public static MorseConnection showDialog( JComponent parent ) {
		
		LoginDialog login = new LoginDialog();
		
		login.tService.setText( "service" );
		login.tUser.setText( "root" );
		login.tHost.setText( "localhost" );
		login.tPass.setText( "nein" );
		
		final JDialog dialog = new JDialog( parent == null ? null : (Frame)SwingUtilities.getWindowAncestor( parent ) );
		dialog.getContentPane().add( login );
		
		login.addActionListener( new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible( false );
			}
			
		});
		
		dialog.setSize( 300, 170 );
		ASwing.centerDialog( parent == null ? null : (Frame)SwingUtilities.getWindowAncestor( parent ), dialog );
		
		dialog.setModal( true );
		dialog.setVisible( true );
		
		dialog.setTitle( "Login" );
		
		if ( login.result.equals( ACTION_CANCEL ) )
			return null;
		
		String url = "morse://" + login.tHost.getText() + '/' + login.tService.getText();
		
		MorseConnection con = null;
		try {
			Class.forName( "de.mhu.com.morse.eecm.MorseDriver" );
			con = (MorseConnection)EcmManager.connect( url, login.tUser.getText(), login.tPass.getText() );
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			log.error( e1 );
		}
		
		return con;
		
	}
	
}
