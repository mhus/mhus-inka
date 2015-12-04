package de.mhu.com.morse.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import de.mhu.com.morse.eecm.MorseConnection;
import de.mhu.lib.AFile;
import de.mhu.lib.AThread;
import de.mhu.lib.ATimekeeper;
import de.mhu.lib.ATimerTask;
import de.mhu.lib.AToolbox;
import de.mhu.lib.ArgsParser;
import de.mhu.lib.config.ConfigManager;
import de.mhu.lib.io.YOutputStream;
import de.mhu.lib.log.AL;
import de.mhu.lib.log.ALUtilities;
import de.mhu.lib.log.SwingAppender;
import de.mhu.com.morse.client.AuthPassword;
import de.mhu.com.morse.client.MConnection;
import de.mhu.com.morse.client.MConnectionTcp;
import de.mhu.com.morse.mql.IQuery;
import de.mhu.com.morse.mql.IQueryResult;
import de.mhu.com.morse.mql.Query;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.com.morse.net.IMessage;
import de.mhu.com.morse.obj.ITableRead;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.types.IType;
import de.mhu.com.morse.types.ITypes;
import de.mhu.com.morse.utils.MorseException;
import de.mhu.lib.plugin.AfPluginException;
import de.mhu.lib.plugin.AfPluginNode;
import de.mhu.lib.plugin.AfPluginRoot;
import de.mhu.lib.plugin.utils.ALLogger;
import de.mhu.lib.plugin.utils.XmlConfig;
import de.mhu.lib.swing.OutputArea;

public class MainClient {

	private static AL log = new AL( MainClient.class );
	// private static OutputArea oa;
	private static JTextArea text;
	private static MConnection con;
	private static JCheckBox cbWait;
	private static JCheckBox cbMql;
	private static SwingAppender sa;
	private static JCheckBox cbAutoCommit;
	private static JButton bCommit;
	private static JButton bRollback;
	private static JFileChooser chooser = new JFileChooser();
	private static JButton bSave;
	private static JButton bLoad;
	private static JButton bDefMql;
	private static LinkedList<String> history = new LinkedList<String>();
	private static JButton bHist;

	public static void main( String[] args ) {
		
		try {
			ArgsParser argp = new ArgsParser( args );
			
			ConfigManager.initialize( argp );
			ALUtilities.configure(argp );
			
			MorseConnection info = LoginDialog.showDialog( null );
			if ( info == null )
				System.exit( 0 );
			
			sa = new SwingAppender();
			AL.eventLog().register( sa );
			
			con = info.getConnecion();
			
			JFrame jf = new JFrame();
			jf.setTitle( "Console" );
			text = new JTextArea();
			JScrollPane textScroller = new JScrollPane( text );
			sa.setPreferredSize( new Dimension( 300, 300 ) );
			
			// oa = new OutputArea();
			// oa.setLinked( System.out );
			// System.setOut( new PrintStream( oa.getOutputStream() ) );
			// System.setErr( new PrintStream( oa.getOutputStream() ) );
	
			// System.setOut( new PrintStream( new YOutputStream( new OutputStream[] { System.out, oa.getOutputStream() } ) ) );
			// System.setErr( new PrintStream( new YOutputStream( new OutputStream[] { System.err, oa.getOutputStream() } ) ) );
			
			JSplitPane split = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
			split.setTopComponent( sa );
			split.setBottomComponent( textScroller );
			
			jf.getContentPane().add( split, BorderLayout.CENTER );
			
			jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			jf.setSize( 800, 500 );
			jf.setLocation( 100, 100 );
			jf.show();
			
			/*
			Timer timer = new Timer();
			
			timer.schedule( new ATimerTask() {
	
				public void run0() throws Exception {
					oa.refreshOutput();
				}
				
			}, 200, 200 );
			*/
			final Timer worker = new Timer();
			
			text.addKeyListener( new KeyAdapter() {
	
				@Override
				public void keyTyped(KeyEvent e) {
					
					if ( ! e.isControlDown() || e.getKeyChar() != KeyEvent.VK_ENTER ) {
						return;
						
					}
					
					worker.schedule( new ATimerTask() {
	
						@Override
						public void run0() throws Exception {
							
							String s = text.getSelectedText();
							if ( s == null ) {
								s = text.getText();
								int start = s.lastIndexOf( "\n\n", text.getCaretPosition() );
								if ( start < 0 ) start = 0; else start+=2;
								int end = s.indexOf( "\n\n", text.getCaretPosition() );
								if ( end < 0 ) end = s.length();								
								s = s.substring( start, end );
								if ( s == null )
									s = text.getText();
							}
							if ( s == null ) return;
							s = s.trim();
							if ( s.length() == 0 ) return;
							
							history.remove( s );
							history.addFirst( s );
							if ( history.size() > 20 )
								history.removeLast();
							
							if ( cbMql.isSelected() ) {
								try {
									if ( log.t4() ) log.info( "MQL: " + s );
									Query query = new Query( 
											con.getSession().getDbProvider().getDefaultConnection(),
											s );
									ATimekeeper tk = new ATimekeeper();
									text.setBackground( Color.LIGHT_GRAY );
									tk.start();
									IQueryResult res = query.execute();
									tk.stop();
									text.setBackground( Color.WHITE );
									log.info( "Execute MQL: " + tk.getCurrentTimeAsString( true ) );
									log.info( "RC: " + res.getReturnCode() );
									
									if ( res.getReturnCode() < 0 ) {
										System.out.println( "ERROR: " + res.getErrorCode() + " " + res.getErrorInfo() );
									} else {
										tk.reset();
										tk.start();
										createQueueTable( res, s );
										tk.stop();
										log.info( "Load Results: " + tk.getCurrentTimeAsString( true ) );
									}
									res.close();
									
									// text.setText( "" );
									
								} catch (Throwable e1) {
									log.error( e1 );
									text.setBackground( Color.WHITE );
								}						
							} else {
								IMessage msg = con.createMessage();
								String[] parts = s.split(",");
								for ( int i = 0; i < parts.length; i++ ) {
									if ( parts[i].startsWith( "i:") )
										msg.append( Integer.valueOf( parts[i].substring( 2 ) ).intValue() );
									else
									if ( parts[i].startsWith( "l:") )
										msg.append( Long.valueOf( parts[i].substring( 2 ) ).longValue() );
									else
									if ( parts[i].startsWith( "d:") )
										msg.append( Double.valueOf( parts[i].substring( 2 ) ).doubleValue() );
									else
									if ( parts[i].startsWith( "s:") )
										msg.append( parts[i].substring( 2 ) );
									else
										msg.append( parts[i] );
								}
								try {
									System.out.println( "EXECUTE: " + s );
									if ( cbWait.isSelected() ) {
										IMessage res = null;
										text.setBackground( Color.LIGHT_GRAY );
										try {
											res = con.sendAndWait(msg, 3000 );
										} catch (AfPluginException e1) {
											log.error( e1 );
										}
										text.setBackground( Color.WHITE );
										if ( res == null )
											log.info( "RESULT is NULL" );
										else
											log.info( "RESULT: " + res.toString() );
									} else
										con.sendMessage( msg );
									
								} catch (Throwable e1) {
									log.error( e1 );
									text.setBackground( Color.WHITE );
								}
							}
						}
						
					}, 100 );
				
			} } );
			
			text.grabFocus();
			// text.setText( "qry.str,i:0,select distinct * from test_2 WHERE test1 = 'abc' @*" );
			
			JToolBar tb = new JToolBar();
			
			jf.getContentPane().add( tb, BorderLayout.NORTH );
			
			cbMql = new JCheckBox("MQL");
			cbMql.setSelected( true );
			tb.add( cbMql );
	
			cbWait = new JCheckBox("W");
			cbWait.setToolTipText( "Wait for Answer" );
			tb.add( cbWait );
			cbWait.setSelected( true );
			
			cbAutoCommit = new JCheckBox( "AC" );
			cbAutoCommit.setToolTipText( "AutoCommit" );
			tb.add( cbAutoCommit );
			cbAutoCommit.setSelected( con.getSession().getDbProvider().getDefaultConnection().isAutoCommit() );
			cbAutoCommit.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					con.getSession().getDbProvider().getDefaultConnection().setAutoCommit( cbAutoCommit.isSelected() );
				}
				
			});
			
			bCommit = new JButton( "C" );
			bCommit.setToolTipText( "Commit" );
			tb.add( bCommit );
			bCommit.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					try {
						con.getSession().getDbProvider().getDefaultConnection().commit();
					} catch (MorseException e1) {
						log.error( e1 );
					}
				}
				
			});
			
			bRollback = new JButton( "R" );
			bRollback.setToolTipText( "Rollback" );
			tb.add( bRollback );
			bRollback.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					try {
						con.getSession().getDbProvider().getDefaultConnection().rollback();
					} catch (MorseException e1) {
						log.error( e1 );
					}
				}
				
			});
			
			tb.addSeparator();
			
			bSave = new JButton( "S" );
			bSave.setToolTipText( "Save File into Morse");
			tb.add( bSave );
			bSave.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					actionRenditionSave( null );
				}
				
			});
			
			bLoad = new JButton( "L" );
			bLoad.setToolTipText( "Load File From Morse" );
			tb.add( bLoad );
			bLoad.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					actionRenditionShow( null );
				}
				
			});
			
			tb.addSeparator();
	
			JButton bTypes = new JButton( "T" );
			bTypes.setToolTipText( "Print Business Types Structure" );
			tb.add( bTypes );
			bTypes.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					ITypes types = con.getTypeModel();
					for ( Iterator i = types.getTypes(); i.hasNext(); ) {
						IType type = (IType)i.next();
						System.out.println( "TYPE " + type.getName() + " extends " + type.getSuperName() + " {" );
						for ( Iterator j = type.getAttributes(); j.hasNext(); ) {
							IAttribute attr = (IAttribute)j.next();
							System.out.print( "  " + attr.getCanonicalName() + " " + attr.getType() + '(' + attr.getSize() + ") - " + attr.getSourceType().getName() );
							System.out.println( "  " + attr.getAcoName() + ' ' + attr.getAco() );
							if ( attr.isTable() ) {
								System.out.println( "  {" );
								for ( Iterator k = attr.getAttributes(); k.hasNext(); ) {
									IAttribute attr2 = (IAttribute)k.next();
									System.out.print( "    " + attr2.getCanonicalName() + " " + attr2.getType() + '(' + attr2.getSize() + ") - " + attr2.getSourceType().getName() );
									System.out.println( "  " + attr2.getAcoName() + ' ' + attr2.getAco() );
								}
								System.out.println( "  }" );
							}
						}
						System.out.println( "}" );
					}
				} 
				
			});
			
			tb.addSeparator();
	
			bDefMql = new JButton( "DEF");
			tb.add( bDefMql );
			bDefMql.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					try {
						JPopupMenu menu = new JPopupMenu();
						
						FileReader fr = new FileReader( "client.mql" );
						LineNumberReader lnr = new LineNumberReader(fr );
						String line = null;
						ActionListener listener = new ActionListener() {
		
							public void actionPerformed(ActionEvent e) {
								try {
									String str = e.getActionCommand();
									text.getDocument().insertString( 0, str + "\n\n", null );
									text.setSelectionStart( 0 );
									text.setSelectionEnd( str.length() );
									
								} catch (BadLocationException e1) {
									log.error( e1 );
								}
								text.grabFocus();						
							}
							
						};
						
						JComponent current = menu;
						LinkedList<JComponent> history = new LinkedList<JComponent>();
						do {
							line = lnr.readLine();
							if ( line == null ) {}
							else
							if ( line.startsWith( ">>>" ) ) {
								history.add( current );
								JMenu m = new JMenu( line.substring( 3 ) );
								current.add( m );
								current = m;
							} else
							if ( line.startsWith( "<<<" ) ) {
								current = history.removeLast();
							} else
							if ( line.startsWith( "===" ) ) {
								JMenuItem item = new JMenuItem( line.substring( 3 ) );
								item.setEnabled( false );
								current.add( item );
							} else
							if ( line.startsWith( "#" ) ) {
								
							} else
							if ( line.startsWith( "=" ) ) {
								int pos = line.indexOf( '=', 1 );
								JMenuItem item = new JMenuItem( line.substring( 1, pos ) );
								item.setActionCommand( line.substring( pos+1 ) );
								item.addActionListener( listener );
								current.add( item );
							} else
							if ( line.length() != 0 ) {
								JMenuItem item = new JMenuItem( line );
								item.setActionCommand( line );
								item.addActionListener( listener );
								current.add( item );
							}
						} while ( line != null );
						fr.close();
						
						menu.show( bDefMql, 0, bDefMql.getHeight() );
						
					} catch ( IOException ioe ) {
						log.error( ioe );
					}
					
				}
				
			});
			
			bHist = new JButton( "Hist" );
			tb.add( bHist );
			bHist.addActionListener( new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					
					try {
						JPopupMenu menu = new JPopupMenu();
	
						ActionListener listener = new ActionListener() {
							
							public void actionPerformed(ActionEvent e) {
								try {
									String str = e.getActionCommand();
									text.getDocument().insertString( 0, str + "\n\n", null );
									text.setSelectionStart( 0 );
									text.setSelectionEnd( str.length() );
									
								} catch (BadLocationException e1) {
									log.error( e1 );
								}
								text.grabFocus();						
							}
							
						};
						
						for ( Iterator<String> i = history.iterator(); i.hasNext(); ) {
							String line = i.next();
							JMenuItem item = new JMenuItem( line );
							item.setActionCommand( line );
							item.addActionListener( listener );
							menu.add( item );
						}
						
						menu.show( bHist, 0, bHist.getHeight() );
						
					} catch ( Exception ioe ) {
						log.error( ioe );
					}
					
				}
				
			});
			text.grabFocus();
			
			/*
			 * r,123,qry.tst, select distinct object_id from test_2 WHERE test1 = 'abc' @*
			IMessage msg = con.getClient().createMessage( new String[] { "r","123","qry.tst", "select distinct object_id from test_2 WHERE test1 = 'abc' @*" });
			con.getClient().sendMessage( msg );
			AThread.sleep( 3000 );
			System.exit(0);
			*/
			
		} catch ( Throwable e ) {
			e.printStackTrace();
			System.exit( 1 );
		}
	}

	protected static void actionRenditionSave( String oId ) {

		try {
			int rc = chooser.showOpenDialog( bSave );
			if ( rc != JFileChooser.APPROVE_OPTION ) return;
			File file = chooser.getSelectedFile();
			if ( file == null ) return;
			if ( oId == null )
				oId = JOptionPane.showInputDialog( bSave, "Object ID", "" );
			if ( oId == null ) return;
			String index = JOptionPane.showInputDialog( bSave, "Index", "-1" );
			if ( index == null ) return;
			int idx = Integer.parseInt( index );
			String ext = file.getName();
			String format = "";
			int pos = ext.lastIndexOf( '.' );
			if ( pos > 0 ) {
				ext = ext.substring( pos+1 );
				IQueryResult rFormat = new Query( con.getSession().getDbProvider().getDefaultConnection(),
						"SELECT name FROM mc_format WHERE dos_extension='" + ext + "' LIMIT 1 @sys" ).execute();
				if ( rFormat.next() )
					format = rFormat.getString( 0 );
				rFormat.close();
			}
			format = JOptionPane.showInputDialog( bSave, "Format", format );
			String mql = "RENDITION " + oId + " SAVE `" + idx +"` FORMAT " + format;
			Query query = new Query( 
					con.getSession().getDbProvider().getDefaultConnection(),
					mql );
			ATimekeeper tk = new ATimekeeper();
			tk.start();
			IQueryResult res = query.execute();
			tk.stop();
			log.info( "### Execute MQL: " + tk.getCurrentTimeAsString( true ) );
			tk.reset();
			FileInputStream is = new FileInputStream( file );
			OutputStream os = res.getOutputStream();
			AFile.copyFile( is, os );
			os.close();
			is.close();
		} catch ( Exception ex ) {
			log.error( ex );
		}
		
	}

	protected static void actionRenditionLoad( String oId ) {
		try {
			if ( oId == null )
				oId = JOptionPane.showInputDialog( bSave, "Object ID", "" );
			if ( oId == null ) return;
			String index = JOptionPane.showInputDialog( bSave, "Index", "-1" );
			if ( index == null ) return;
						
			int idx = Integer.parseInt( index );
			String mql = "RENDITION " + oId + " LOAD `"+idx+"`";
			Query query = new Query( 
					con.getSession().getDbProvider().getDefaultConnection(),
					mql );
			ATimekeeper tk = new ATimekeeper();
			tk.start();
			IQueryResult res = query.execute();
			tk.stop();
			log.info( "### Execute MQL: " + tk.getCurrentTimeAsString( true ) );
			tk.reset();
			InputStream is = res.getInputStream();
			
			int rc = chooser.showSaveDialog( bSave );
			if ( rc != JFileChooser.APPROVE_OPTION ) return;
			
			File dst = chooser.getSelectedFile();
			FileOutputStream os = new FileOutputStream( dst );
			AFile.copyFile( is, os );
			os.close();
			is.close();
			res.close();
			
			/*
			String content = AFile.readFile( is );
			JFrame frame = new JFrame();
			frame.setTitle( oId );
			JTextArea ta = new JTextArea();
			JScrollPane scroller = new JScrollPane( ta );
			frame.getContentPane().add( scroller );
			frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			ta.setText( content );
			frame.setLocation( 30, 30 );
			frame.setSize( 400, 400 );
			frame.show();
			*/
			// TODO 
		} catch ( Exception ex ) {
			log.error( ex );
		}
	}

	protected static void actionRenditionShow( String oId ) {
		try {
			if ( oId == null )
				oId = JOptionPane.showInputDialog( bSave, "Object ID", "" );
			if ( oId == null ) return;
			String index = JOptionPane.showInputDialog( bSave, "Index", "-1" );
			if ( index == null ) return;
			int idx = Integer.parseInt( index );
			String mql = "RENDITION " + oId + " LOAD `"+idx+"`";
			Query query = new Query( 
					con.getSession().getDbProvider().getDefaultConnection(),
					mql );
			ATimekeeper tk = new ATimekeeper();
			tk.start();
			IQueryResult res = query.execute();
			tk.stop();
			log.info( "### Execute MQL: " + tk.getCurrentTimeAsString( true ) );
			tk.reset();
			InputStream is = res.getInputStream();
			String content = AFile.readFile( is );
			JFrame frame = new JFrame();
			frame.setTitle( oId );
			JTextArea ta = new JTextArea();
			JScrollPane scroller = new JScrollPane( ta );
			frame.getContentPane().add( scroller );
			frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			ta.setText( content );
			frame.setLocation( 30, 30 );
			frame.setSize( 400, 400 );
			frame.show();
		} catch ( Exception ex ) {
			log.error( ex );
		}
	}
	
	protected static void createQueueTable(ITableRead res, String title ) throws MorseException {
		String[] cols = res.getColumns(); 
		if ( cols == null || cols.length == 0 )
			return;
		
		JFrame f = new JFrame();
		if ( title == null ) title = "?";
		title = title.replaceAll( "\n", " " );
		if ( title.length() > 50 ) title = title.substring( 0, 40 ) + "...";
		f.setTitle( title );
		f.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		final DefaultTableModel tm = new DefaultTableModel() {
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return false;
		    }
		};
		final JTable table = new JTable( tm );
		table.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		JScrollPane scr = new JScrollPane( table );
		
		for ( int i = 0; i < cols.length; i++ ) {
			tm.addColumn( cols[i] );
		}
		
		while ( res.next() ) {
			Object[] row = new Object[ cols.length ];
			for ( int i = 0; i < cols.length; i++ ) {
				row[ i ] = res.getString( i );
				if ( row[i] == null && ( res instanceof IQueryResult ) ) {
					row[i] = ((IQueryResult)res).getTable( i );
				}
			}
			tm.addRow( row );
		}
		table.addMouseListener( new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				
				if ( e.getButton() == MouseEvent.BUTTON3 ) {
					
					final Object val = tm.getValueAt( table.getSelectedRow(), table.getSelectedColumn() );
					if ( val == null ) return;
					
					JPopupMenu menu = new JPopupMenu();
					JMenuItem item = null;
					item = new JMenuItem( "Open Object ID" );
					item.addActionListener( new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							try {
								IQueryResult r = new Query( con.getDefaultConnection(), "FETCH " + val.toString() ).execute();
								createQueueTable(r, val.toString() );
								r.close();
							} catch (MorseException e1) {
								log.error( e1 );
							}
						}
						
					});
					menu.add( item );
					
					item = new JMenuItem( "Show Rendition" );
					item.addActionListener( new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							actionRenditionShow( val.toString() );
						}
						
					});
					menu.add( item );
					
					item = new JMenuItem( "Load Rendition" );
					item.addActionListener( new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							actionRenditionLoad( val.toString() );
						}
						
					});
					menu.add( item );
					
					item = new JMenuItem( "Store Rendition" );
					item.addActionListener( new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							actionRenditionSave( val.toString() );
						}
						
					});
					menu.add( item );

					menu.show( (JComponent)e.getSource(), e.getX(), e.getY() );
					
				} else
				if ( e.getClickCount() > 1 && e.getButton() == MouseEvent.BUTTON1 ) {
					Object val = tm.getValueAt( table.getSelectedRow(), table.getSelectedColumn() );
					if ( val instanceof ITableRead ) {
						try {
							ITableRead t = (ITableRead)val;
							t.reset();
							createQueueTable( t, (String)table.getColumnName( table.getSelectedColumn() ) );
						} catch (MorseException e1) {
							log.error( e1 );
						}
					} else {
						JFrame fr = new JFrame();
						JTextArea txt = new JTextArea();
						txt.setWrapStyleWord( true );
						txt.setText( val.toString() );
						JScrollPane scroller = new JScrollPane( txt );
						fr.getContentPane().add( scroller );
						fr.setSize( 250, 150 );
						fr.setLocation( 300, 300 );
						fr.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
						fr.show();
					}
				}
				
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		f.getContentPane().add( scr );
		f.setSize( 500, 500 );
		f.setLocation( 200 + (int)(Math.random() * 200 - 100), 200  + (int)(Math.random() * 200 - 100));
		f.show();

	}
	
	
}
