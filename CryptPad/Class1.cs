/* 
   Copyright 2008 Mike Hummel

   This file is part of CryptPad.

    CryptPad is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CryptPad is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CryptPad.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 *  http://www.csharp-home.com/index/tiki-read_article.php?articleId=114
 *  http://support.microsoft.com/kb/307010
 *  http://blogs.msdn.com/shawnfa/archive/2004/04/14/113514.aspx
 *  http://www.codeproject.com/KB/security/DotNetCrypto.aspx
 *  http://msdn2.microsoft.com/en-us/library/system.security.cryptography.md5cryptoserviceprovider.aspx
 *  http://en.allexperts.com/q/C-3307/Font-Serialization.htm
 * 
 */

using System;
using System.IO;
using System.Windows.Forms;
using System.Drawing;
using System.ComponentModel;

using  System.Security.Cryptography;
using System.Text;

using System.Runtime.Serialization;
using System.Runtime.Serialization.Formatters.Binary;

namespace CryptPad
{


	class Editor:Form
	{
		ToolBar tbEditor;
		static bool IsContentModified=false;
		StatusBar sbEditor;
		ImageList imlEditor;
		RichTextBox txtEditor;
		Timer tmrEditor;
		MainMenu mnuMain;
		MenuItem mnuFile;
		MenuItem mnuEdit;
		MenuItem mnuFormat;
		MenuItem mnuHelp;
		MenuItem mnuWordWrap;
		static bool IsDirectionDownward=true; //true means "Down" false  means "Up"
		static string TextToFind="";
		static string TextToReplace="";
		string lastPass = null;
		
		public Editor ( string[] args )
		{
			tbEditor=new ToolBar();
			tbEditor.ButtonClick+=new ToolBarButtonClickEventHandler(ToolBarButton_Click);
			sbEditor=new StatusBar();
			sbEditor.ShowPanels=true;
			imlEditor=new ImageList();
			tbEditor.ImageList=imlEditor;
			LoadToolBarButton();
			LoadStatusBarPanel();
			
			txtEditor=new RichTextBox();
			txtEditor.Location=new Point(0,tbEditor.Top+tbEditor.Height+2);
			txtEditor.Size=new Size(this.Width-10,this.Height-tbEditor.Height-sbEditor.Height-30);
			txtEditor.Anchor=AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right | AnchorStyles.Bottom;
			txtEditor.Multiline=true;
			txtEditor.AcceptsTab=true;
			txtEditor.HideSelection=false;
			txtEditor.ScrollBars=RichTextBoxScrollBars.Both;
			txtEditor.KeyUp+=new KeyEventHandler(TextBox_KeyUp);
			txtEditor.KeyPress+=new KeyPressEventHandler(TextBox_KeyPress);
			txtEditor.MouseDown+=new MouseEventHandler(TextBox_MouseDown);
			txtEditor.TextChanged+=new EventHandler(TextBox_Change);
			txtEditor.LinkClicked+=new LinkClickedEventHandler(TextBox_LinkClicked);
			txtEditor.WordWrap=true;
			txtEditor.AutoSize=false;
			
			mnuMain=new MainMenu();
			LoadMainMenu();
			
			LoadFileMenu();
			LoadEditMenu();
			LoadFormatMenu();
			LoadHelpMenu();
			
			this.Controls.Add(tbEditor);
			this.Controls.Add(sbEditor);
			this.Controls.Add(txtEditor);
			this.Text="Untitled";
			this.Menu=mnuMain;
			this.WindowState=FormWindowState.Maximized;
			
			if ( args.Length != 0 ) {
				DoOpen( args[0] );
			}
			
		}
		
		private byte[] createMD5Hash( string password ) {
			// This is one implementation of the abstract class MD5.
			MD5 md5 = new MD5CryptoServiceProvider();
			byte[] result = md5.ComputeHash( Encoding.UTF8.GetBytes( password ) );
			return result;
		}
		/*
		private byte[] encrypt( string password, string msg ) {
			PasswordDeriveBytes cdk = new PasswordDeriveBytes( password, null);
			
			// generate an RC2 key
			byte[] iv = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
			byte[] key = cdk.CryptDeriveKey("RC2", "SHA1", 128, iv);
			
			// setup an RC2 object to encrypt with the derived key
			RC2CryptoServiceProvider rc2 = new RC2CryptoServiceProvider();
			rc2.Key = key;
			rc2.IV = new byte[] { 21, 22, 23, 24, 25, 26, 27, 28};
			
			// now encrypt with it
			byte[] plaintext = Encoding.UTF8.GetBytes( msg );
			MemoryStream ms = new MemoryStream();
			CryptoStream cs = new CryptoStream(ms, rc2.CreateEncryptor(), CryptoStreamMode.Write);
			
			cs.Write(plaintext, 0, plaintext.Length);
			cs.Close();
			byte[] encrypted = ms.ToArray();
			return encrypted;
		}
		
		private string decrypt( string password, byte[] msg, int offset, int len ) {
			PasswordDeriveBytes cdk = new PasswordDeriveBytes( password, null);
			
			// generate an RC2 key
			byte[] iv = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
			byte[] key = cdk.CryptDeriveKey("RC2", "SHA1", 128, iv);
			
			// setup an RC2 object to encrypt with the derived key
			RC2CryptoServiceProvider rc2 = new RC2CryptoServiceProvider();
			rc2.Key = key;
			rc2.IV = new byte[] { 21, 22, 23, 24, 25, 26, 27, 28};
			
			// now decrypt with it
			MemoryStream ms = new MemoryStream();
			CryptoStream cs = new CryptoStream(ms, rc2.CreateDecryptor(), CryptoStreamMode.Write);
			
			cs.Write(msg, offset, len );
			cs.Close();
			byte[] encrypted = ms.ToArray();
			return System.Text.Encoding.UTF8.GetString( encrypted );

		}
		*/
			
		private void encrypt( string password, string msg, Stream stream ) {
			PasswordDeriveBytes cdk = new PasswordDeriveBytes( password, null);
			
			// generate an RC2 key
			byte[] iv = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
			byte[] key = cdk.CryptDeriveKey("RC2", "SHA1", 128, iv);
			
			// setup an RC2 object to encrypt with the derived key
			RC2CryptoServiceProvider rc2 = new RC2CryptoServiceProvider();
			rc2.Key = key;
			rc2.IV = new byte[] { 21, 22, 23, 24, 25, 26, 27, 28};
			
			// now encrypt with it			
			CryptoStream cs = new CryptoStream( stream, rc2.CreateEncryptor(), CryptoStreamMode.Write);
			
			StreamWriter sWriter = new StreamWriter( cs );
			sWriter.Write( msg );
			sWriter.Close();
			cs.Close();
		}
		
		private string decrypt( string password, Stream stream ) {
			PasswordDeriveBytes cdk = new PasswordDeriveBytes( password, null);
			
			// generate an RC2 key
			byte[] iv = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 };
			byte[] key = cdk.CryptDeriveKey("RC2", "SHA1", 128, iv);
			
			// setup an RC2 object to encrypt with the derived key
			RC2CryptoServiceProvider rc2 = new RC2CryptoServiceProvider();
			rc2.Key = key;
			rc2.IV = new byte[] { 21, 22, 23, 24, 25, 26, 27, 28};
			
			// now decrypt with it
			CryptoStream cs = new CryptoStream(stream, rc2.CreateDecryptor(), CryptoStreamMode.Read);
			StreamReader sReader = new StreamReader(cs);
			string ret = sReader.ReadToEnd();
			
			sReader.Close();
			cs.Close();
			return ret;
		}
		
		//To set the Content modified flag
		
		private void TextBox_Change(object sender,EventArgs eArgs)
		{
			IsContentModified=true;
		}
		
		//To Set Line,Col in StatusBar
		private void TextBox_KeyUp(object sender,KeyEventArgs kArgs)
		{
			try
			{
				int ColCount=1;
				int RowCount=1;
				int Pos;
				//To Set Column
				if(txtEditor.SelectionStart>-1)
				{
					Pos=txtEditor.Text.LastIndexOf("\n",txtEditor.SelectionStart);
					if(Pos>-1)
					{
						//If the cursor is at CRLF
						if(Pos!=txtEditor.SelectionStart)
							ColCount=txtEditor.SelectionStart-Pos;
						else
						{
							//Col position is diff between PrevEnter and CurPos
							Pos=txtEditor.Text.LastIndexOf("\n",txtEditor.SelectionStart-1);
							ColCount=txtEditor.SelectionStart-Pos;
						}
					}
					else
					{
						ColCount=txtEditor.SelectionStart+1;
					}
					while(Pos>-1)
					{
						RowCount++;
						Pos=txtEditor.Text.LastIndexOf("\n",Pos-1);
					}
				}
				sbEditor.Panels[1].Text="Col: " + ColCount.ToString();
				sbEditor.Panels[0].Text="Line: " + RowCount.ToString();
			}
			catch(Exception)
			{
			}
		}
		
		//To Set Line,Col in StatusBar
		private void TextBox_KeyPress(object sender,KeyPressEventArgs kpArgs)
		{
			TextBox_KeyUp(null,null);
		}
		
		//To Set Line,Col in StatusBar
		private void TextBox_MouseDown(object sender,MouseEventArgs mArgs)
		{
			TextBox_KeyUp(null,null);
		}
		
		// Open Explorer with clicked Link
		private void TextBox_LinkClicked(object sender, System.Windows.Forms.LinkClickedEventArgs e)
		{
		   // Call Process.Start method to open a browser
		   // with link text as URL.
   			System.Diagnostics.Process.Start( e.LinkText );
		}

		//To Load MainMenu Entires
		private void LoadMainMenu()
		{
			mnuFile=new MenuItem();
			mnuFile.Text="&File";
			mnuEdit=new MenuItem();
			mnuEdit.Text="&Edit";
			mnuFormat=new MenuItem();
			mnuFormat.Text="F&ormat";
			mnuHelp=new MenuItem();
			mnuHelp.Text="&Help";
			
			mnuMain.MenuItems.Add(mnuFile);
			mnuMain.MenuItems.Add(mnuEdit);
			mnuMain.MenuItems.Add(mnuFormat);
			mnuMain.MenuItems.Add(mnuHelp);
		}
		
		private void LoadFileMenu()
		{
			string[] MenuCaption={"New","Open...","Save","Save As...","-","Exit"};//,"Page Setup","Print","-"
			for(int i=0;i<MenuCaption.Length;i++)
			{
				MenuItem mnuItem=new MenuItem();
				mnuItem.Text=MenuCaption[i];
				mnuItem.Click+=new EventHandler(Menu_Click);
				switch(MenuCaption[i])
				{
					case "New":
						mnuItem.Shortcut=Shortcut.CtrlN;
						break;
					case "Open":
						mnuItem.Shortcut=Shortcut.CtrlO;
						break;
					case "Save":
						mnuItem.Shortcut=Shortcut.CtrlS;
						break;
					case "Print":
						mnuItem.Shortcut=Shortcut.CtrlP;
						break;
				}
				mnuFile.MenuItems.Add(mnuItem);
			}
		}
		
		private void LoadEditMenu()
		{
			string[] MenuCaption={"Undo","-","Cut","Copy","Paste","Delete","-","Find...","FindNext","Replace...","Go To...","-","Select All","Time/Date"};
			Shortcut[] shortKey={Shortcut.CtrlZ,Shortcut.None,Shortcut.CtrlX,Shortcut.CtrlC,Shortcut.CtrlV,Shortcut.Del,Shortcut.None,Shortcut.CtrlF,Shortcut.F3,Shortcut.CtrlH,Shortcut.CtrlG,Shortcut.None,Shortcut.CtrlA,Shortcut.F5};
			for(int i=0;i<MenuCaption.Length;i++)
			{
				MenuItem mnuItem=new MenuItem();
				mnuItem.Text=MenuCaption[i];
				mnuItem.Click+=new EventHandler(Menu_Click);
				mnuItem.Shortcut=shortKey[i];
				mnuEdit.MenuItems.Add(mnuItem);
			}
		}
		
		private void LoadFormatMenu()
		{
			string[] MenuCaption={"WordWrap","Font..."};
			for(int i=0;i<MenuCaption.Length;i++)
			{
				MenuItem mnuItem=new MenuItem();
				mnuItem.Text=MenuCaption[i];
				if(i==0)
				{
					mnuItem.Checked=true;
					txtEditor.WordWrap=true;
					mnuWordWrap = mnuItem;
				}
				mnuItem.Click+=new EventHandler(Menu_Click);
				mnuFormat.MenuItems.Add(mnuItem);
			}
		}
		
		private void LoadHelpMenu()
		{
			string[] MenuCaption={"About"};
			for(int i=0;i<MenuCaption.Length;i++)
			{
				MenuItem mnuItem=new MenuItem();
				mnuItem.Text=MenuCaption[i];
				mnuItem.Click+=new EventHandler(Menu_Click);
				mnuHelp.MenuItems.Add(mnuItem);
			}
		}
		
		//To Load ToolBar Buttons
		private void LoadToolBarButton()
		{
			string[] strToolTip={"New","Open","Save","","Cut","Copy","Paste","","Undo","Redo"};//,"","Print","Preview"};
			ToolBarButton[] tbButton=new ToolBarButton[strToolTip.Length];
			ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
			this.Icon = (Icon)resources.GetObject( "ICON" );
			for(int i=0;i<strToolTip.Length;i++)
			{
				tbButton[i]=new ToolBarButton();
				if(strToolTip[i].Length>0)
				{
					// Image img= Image.FromFile("images/" + strToolTip[i] + ".bmp");
					Image img = (Bitmap)resources.GetObject( strToolTip[i].ToUpper() );
					imlEditor.Images.Add( img );
					tbButton[i].ImageIndex=imlEditor.Images.Count-1;
					tbButton[i].ToolTipText=strToolTip[i];
				}
				else
				{
					tbButton[i].Style=ToolBarButtonStyle.Separator;
				}
				tbEditor.Buttons.Add(tbButton[i]);
			}
		}
		
		//To Show Time in StatusBar
		private void Timer_Tick(object sender,EventArgs eArgs)
		{
			DateTime CurrentTime=DateTime.Now;
			int h=CurrentTime.Hour;
			int m=CurrentTime.Minute;
			int s=CurrentTime.Second;
			string TimeInString=(h>9)?h.ToString():"0" + h.ToString();
			TimeInString+=":";
			TimeInString+=(m>9)?m.ToString():"0" + m.ToString();
			TimeInString+=":";
			TimeInString+=(s>9)?s.ToString():"0" + s.ToString();
			sbEditor.Panels[sbEditor.Panels.Count-1].Text=TimeInString;
		}
		
		//To Load StatusBar Panels
		private void LoadStatusBarPanel()
		{
			string[] strToolTip={"Line:1","Col:1","","Time"};
			for(int i=0;i<strToolTip.Length;i++)
			{
				StatusBarPanel sbPanel=new StatusBarPanel();
				sbPanel.Text=strToolTip[i];
				sbPanel.BorderStyle=StatusBarPanelBorderStyle.Sunken;
				if(strToolTip[i].Length==0)
					sbPanel.Width=500;
				sbEditor.Panels.Add(sbPanel);
				if(strToolTip[i]=="Time")
					Timer_Tick(null,null);
			}
			tmrEditor=new Timer();
			tmrEditor.Interval=1000;
			tmrEditor.Tick+=new EventHandler(Timer_Tick);
			tmrEditor.Start();
		}
		
		//ToolBar Button Click Event Handler
		
		private void ToolBarButton_Click(object sender,ToolBarButtonClickEventArgs tbArgs)
		{
			MenuToolBarEventHandler(((ToolBar)sender).Buttons.IndexOf(tbArgs.Button));
		}
		
		public void Menu_Click(object sender,EventArgs eArgs)
		{
			string MenuCaption=((MenuItem)sender).Text;
			switch(MenuCaption)
			{
				case "New":
					DoNew();
					break;
				case "Open...":
					DoOpen();
					break;
				case "Save":
					DoSave();
					break;
				case "Save As...":
					DoSaveAs(true);
					break;
				case "Print":
					break;
				case "PageSetup":
					break;
				case "Exit":
					this.Dispose();
					Application.Exit();
					break;
				case "Undo":
					DoUndo();
					break;
				case "Cut":
					DoCut();
					break;
				case "Copy":
					DoCopy();
					break;
				case "Paste":
					DoPaste();
					break;
				case "Delete":
					if(txtEditor.SelectionLength==0)
						txtEditor.SelectionLength=1;
					txtEditor.SelectedText="";
					break;
				case "Find...":
					ShowFind();
					break;
				case "FindNext":
					Find_Click(null,null);
					break;
				case "Replace...":
					ShowReplace();
					break;
				case "Go To...":
					int LineNumber=GoTo(sbEditor.Panels[0].Text.Substring(6).Trim());
					if(LineNumber>0)
					{
						MoveToLine(LineNumber);
					}
					else
					{
						if(LineNumber<0)
							MessageBox.Show("Invalid Line Number.");
					}
					break;
				case "Select All":
					txtEditor.SelectionStart=0;
					txtEditor.SelectionLength=txtEditor.Text.Length;
					break;
				case "Time/Date":
					txtEditor.SelectedText=DateTime.Now.ToString();
					break;
				case "WordWrap":
					((MenuItem)sender).Checked=!((MenuItem)sender).Checked;
					txtEditor.WordWrap=((MenuItem)sender).Checked;
					break;
				case "Font...":
					SetFont();
					break;
				case "About":
					MessageBox.Show("CryptPad By Mike Hummel (mh@mikehummel.de)\nBased on C# Editor By\n H.Prasad");
					break;
			}
		}
		
		public void MenuToolBarEventHandler(int OperationIndex)
		{
			switch(OperationIndex)
			{
				case 0: //New
					DoNew();
					break;
				case 1: //Open
					DoOpen();
					break;
				case 2: //Save
					DoSave();
					break;
				case 3: //Save As
					DoSaveAs(true);
					break;
				case 4: //Cut
					DoCut();
					break;
				case 5: //Copy
					DoCopy();
					break;
				case 6: //Paste
					DoPaste();
					break;
				case 8: //Undo
					DoUndo();
					break;
				case 9: //Redo
					DoRedo();
					break;
				case 11:  //Print
					break;
				case 12: //Preview
					break;
				case 13:  //Page Setup
					break;
			}
			
		}
		
		public void MoveToLine(int LineNumber)
		{
			int CRPosition=txtEditor.Text.IndexOf("\n");
			int LineCount=0;
			bool IsMoved=false;
			while(CRPosition >-1 && !IsMoved)
			{
				LineCount++;
				if(LineNumber==LineCount)
				{
					txtEditor.SelectionStart=CRPosition-1;
					TextBox_KeyUp(null,null);
					IsMoved=true;
				}
				CRPosition=txtEditor.Text.IndexOf("\n",CRPosition+1);
			}
			if(!IsMoved)
			{
				if(LineNumber==LineCount+1)
					txtEditor.SelectionStart=txtEditor.Text.Length;
				else
					MessageBox.Show("Line Number Out of Range");
			}
		}
		
		private void DoCut()
		{
			if(txtEditor.SelectedText!="")
			{
				txtEditor.Cut();
				TextBox_KeyUp(null,null);
			}
			
		}
		
		private void DoCopy()
		{
			if(txtEditor.SelectedText!="")
			{
				txtEditor.Copy();
				TextBox_KeyUp(null,null);
			}
		}
		
		private void DoPaste()
		{
			if(Clipboard.GetDataObject().GetDataPresent("System.String")==true)
			{
				txtEditor.Paste();
				TextBox_KeyUp(null,null);
			}
		}
		
		private void DoUndo()
		{
			if(txtEditor.CanUndo)
			{
				txtEditor.Undo();
				TextBox_KeyUp(null,null);
			}
		}
		
		private void DoRedo()
		{
			if(txtEditor.CanUndo)
			{
				txtEditor.Undo();
				TextBox_KeyUp(null,null);
			}
		}
		
		
		public void DoSaveAs(bool ShowDialog)
		{
			string FileName="";
			if(ShowDialog)
			{
				SaveFileDialog  dlgSave=new SaveFileDialog();
				dlgSave.Filter="Text Files (*.txt)|*.txt|Crypted File (*.ctxt)|*.ctxt|All Files (*.*)|*.*";
				dlgSave.FilterIndex=2;
				dlgSave.RestoreDirectory=true;
				if(dlgSave.ShowDialog()==DialogResult.OK)
				{
					FileName=dlgSave.FileName;
				} else {
					return;
				}
			}
			else
			{
				FileName=this.Text;
			}
			
			bool isCrypt = FileName.EndsWith( ".ctxt" );
			string pass = null;
			if ( isCrypt ) {
				Form1 form = new Form1();
				form.setPass( lastPass );
				form.ShowDialog( this );
				if ( form.isCancel ) {
					return;
				}
				pass = form.getPass();
				lastPass = pass;
				
				try
				{
					FileStream fs = File.Create( FileName );
					byte[] buffer = new byte[ 10 ];
					for ( int i = 0; i < 10; i++ ) buffer[ i ] = 0;
					buffer[0] = (byte)'C';
					buffer[1] = (byte)'P';
					buffer[9] = (byte)( txtEditor.WordWrap ? 1 : 0 );
					fs.Write( buffer, 0, 10 );
					fs.Write( createMD5Hash( pass ), 0, 16 );
					
					IFormatter formatter = new BinaryFormatter();
					formatter.Serialize( fs, txtEditor.Font );
					
					encrypt( pass, txtEditor.Text, fs );
					fs.Close();
				}
				catch(Exception e)
				{
					MessageBox.Show(e.ToString());
				}
				
			} else {
				try
				{
					StreamWriter sw=File.CreateText(FileName);
					sw.Write( txtEditor.Text );
					sw.Flush();
					sw.Close();
					this.Text=FileName;
				}
				catch(Exception e)
				{
					MessageBox.Show(e.ToString());
				}
			}
			
		}
		
		public void DoSave()
		{
			if(this.Text=="Untitled")
			{
				DoSaveAs(true);
			}
			DoSaveAs(false);
			IsContentModified=false;
		}
		
		private void DoNew()
		{
			if(IsContentModified)
			{
				string Message="The text in the " + this.Text + " has been changed.\n Do you want to Save Changes?";
				switch(MessageBox.Show(Message,"SuperPad",MessageBoxButtons.YesNoCancel))
				{
					case DialogResult.Yes:
						DoSave();
						txtEditor.Text="";
						this.Text="Untitled";
						break;
					case DialogResult.No:
						txtEditor.Text="";
						this.Text="Untitled";
						break;
					case DialogResult.Cancel:
						break;
				}
			}
			else
			{
				txtEditor.Text="";
				this.Text="Untitled";
			}
			lastPass = null;
		}
		
		private void DoOpen() {
			OpenFileDialog dlgOpen=new OpenFileDialog();
			dlgOpen.Filter="Text Files (*.txt)|*.txt|Crypted Text (*.ctxt)|*.ctxt|All Files (*.*)|*.*";
			dlgOpen.FilterIndex=2;
			dlgOpen.RestoreDirectory=true;
			if(dlgOpen.ShowDialog()==DialogResult.OK)
				DoOpen( dlgOpen.FileName );
		}
		
		private void DoOpen( string FileName )
		{
			lastPass = null;			
			try
			{
				bool cryptMode = FileName.EndsWith( ".ctxt" );
				string pass = null;
				if ( cryptMode ) {
					Form2 form = new Form2();
					form.ShowDialog( this );
					pass = form.getPass();
					lastPass = pass;
				}
				txtEditor.Text="";
				string strLine;
				if ( cryptMode ) {
					FileStream fs = File.OpenRead( FileName );
					byte[] buffer = new byte[ 16 ];
					fs.Read( buffer, 0, 10 );
					if ( buffer[0] != (byte)'C' || buffer[1] != (byte)'P' ) {
						MessageBox.Show( this, "Not a crypted file" );
						fs.Close();
						return;
					} else {
						txtEditor.WordWrap = ( buffer[9] == 1 );
						mnuWordWrap.Checked = txtEditor.WordWrap;
						fs.Read( buffer, 0, 16 );
						byte[] md5 = createMD5Hash( pass );
						for ( int i = 0; i < 16; i++ ) {
							if ( buffer[i] != md5[i] ) {
								MessageBox.Show( this, "Wrong Password" );
								fs.Close();
								return;
							}
						}
						
						IFormatter formatter = new BinaryFormatter();
						txtEditor.Font = (Font)formatter.Deserialize( fs );
						txtEditor.Text = decrypt( pass, fs );
						fs.Close();
					}
				} else {
					StreamReader sr=File.OpenText(FileName);
					while((strLine=sr.ReadLine())!=null)
					{
						txtEditor.Text+=strLine+"\n";
					}
					sr.Close();
				}
				txtEditor.SelectionStart=0;
				txtEditor.SelectionLength=0;
				this.Text=FileName;
			}
			catch(Exception e)
			{
				MessageBox.Show("Error Occurred:\n" + e.ToString());
			}
		
		}
		
		public void SetFont()
		{
			FontDialog dlgFont=new FontDialog();
			dlgFont.Font=txtEditor.Font;
			dlgFont.ShowColor=true;
			dlgFont.Color=txtEditor.ForeColor;
			if(dlgFont.ShowDialog()==DialogResult.OK)
			{
				txtEditor.Font=dlgFont.Font;
				txtEditor.ForeColor=dlgFont.Color;
			}
		}
		
		public int GoTo(string LineNumber)
		{
			Form frmDialog=new Form();
			
			TextBox LineBox=new TextBox();
			Button cmdOK=new Button();
			Button cmdCancel=new Button();
			
			cmdOK.Text="OK";
			cmdOK.DialogResult=DialogResult.OK;
			cmdOK.Click-=new EventHandler(Find_Click);
			
			cmdCancel.Text="Cancel";
			cmdCancel.DialogResult=DialogResult.Cancel;
			LineBox.Text=LineNumber;
			if(LineBox.Text.Length==0) LineBox.Text="0";
			frmDialog.Size=new Size(230,100);
			frmDialog.Text="Go to Line";
			frmDialog.AcceptButton=cmdOK;
			frmDialog.CancelButton=cmdCancel;
			frmDialog.MaximizeBox=false;
			frmDialog.MinimizeBox=false;
			frmDialog.FormBorderStyle=FormBorderStyle.FixedDialog;
			frmDialog.Location=new Point(this.Left+Math.Abs((this.Width-frmDialog.Width))/2,this.Top + 100);
			
			LineBox.Location=new Point(10,10);
			cmdOK.Location=new Point(LineBox.Left+LineBox.Width+20,10);
			cmdCancel.Location=new Point(cmdOK.Left,cmdOK.Top+cmdOK.Height+10);
			
			frmDialog.Controls.Add(LineBox);
			frmDialog.Controls.Add(cmdOK);
			frmDialog.Controls.Add(cmdCancel);
			frmDialog.ShowDialog(this);
			
			if(frmDialog.DialogResult==DialogResult.OK)
			{
				try
				{
					frmDialog.Dispose();
					return Int32.Parse(LineBox.Text);
				}
				catch(Exception)
				{
					return -1;
				}
			}
			return 0;
		}
		
		private void ShowFind()
		{
			TextBox txtFind=new TextBox();
			Label lblFind=new Label();
			CheckBox chkMatch=new CheckBox();
			GroupBox grpDirection=new GroupBox();
			RadioButton optUp=new RadioButton();
			RadioButton optDown=new RadioButton();
			Button cmdOK=new Button();
			Button cmdCancel=new Button();
			
			cmdOK.Text="Find Next";
			cmdOK.DialogResult=DialogResult.OK;
			cmdOK.Click+=new EventHandler(Find_Click);
			
			cmdCancel.Text="Cancel";
			cmdCancel.DialogResult=DialogResult.Cancel;
			cmdCancel.Click+=new EventHandler(Cancel_Click);
			
			Form frmDialog=new Form();
			frmDialog.Size=new Size(380,150);
			frmDialog.Text="Find";
			frmDialog.MaximizeBox=false;
			frmDialog.MinimizeBox=false;
			frmDialog.FormBorderStyle=FormBorderStyle.FixedDialog;
			
			lblFind.Location=new Point(5,15);
			lblFind.Text="Fi&nd what:";
			lblFind.AutoSize=true;
			
			txtFind.Location=new Point(lblFind.Left+lblFind.Width+5,lblFind.Top);
			txtFind.Size=new Size(200,txtFind.Height);
			txtFind.Text=TextToFind;
			txtFind.TextChanged+=new EventHandler(txtFind_Change);
			
			cmdOK.Location=new Point(txtFind.Left+txtFind.Width+5,lblFind.Top-2);
			cmdCancel.Location=new Point(cmdOK.Left,cmdOK.Top+cmdOK.Height+5);
			
			grpDirection.Location=new Point(lblFind.Left,cmdCancel.Top+cmdCancel.Height);
			grpDirection.Size=new Size(120,45);
			grpDirection.Text="Match Direction";
			
			optUp.Location=new Point(10,15);
			optUp.Click+=new EventHandler(Direction_Click);
			optUp.Text="&Up";
			optUp.Width=45;
			
			optDown.Text="&Down";
			optDown.Width=55;
			optDown.Click+=new EventHandler(Direction_Click);
			optDown.Checked=true;
			optDown.Location=new Point(optUp.Left+optUp.Width,optUp.Top);
			
			grpDirection.Controls.Add(optUp);
			grpDirection.Controls.Add(optDown);
			
			chkMatch.Location=new Point(grpDirection.Left+grpDirection.Width+5,grpDirection.Top+(grpDirection.Height-chkMatch.Height)/2);
			chkMatch.Text="Match Case";
			
			if(txtFind.Text.Trim().Length==0) cmdOK.Enabled=false;
			
			frmDialog.Controls.Add(txtFind);
			frmDialog.Controls.Add(lblFind);
			frmDialog.Controls.Add(grpDirection);
			//frmDialog.Controls.Add(chkMatch);
			frmDialog.Controls.Add(cmdOK);
			frmDialog.Controls.Add(cmdCancel);
			frmDialog.ShowInTaskbar=false;
			frmDialog.TopMost=true;
			frmDialog.Show();
		}
		
		private void ShowReplace()
		{
			TextBox txtFind=new TextBox();
			TextBox txtReplace=new TextBox();
			Label lblFind=new Label();
			Label lblReplace=new Label();
			CheckBox chkMatch=new CheckBox();
			GroupBox grpDirection=new GroupBox();
			RadioButton optUp=new RadioButton();
			RadioButton optDown=new RadioButton();
			Button cmdOK=new Button();
			Button cmdReplaceALL=new Button();
			Button cmdCancel=new Button();
			
			cmdOK.Text="Replace";
			cmdOK.DialogResult=DialogResult.OK;
			cmdOK.Click+=new EventHandler(Replace_Click);
			
			cmdReplaceALL.Text="Replace All";
			cmdReplaceALL.DialogResult=DialogResult.OK;
			cmdReplaceALL.Click+=new EventHandler(ReplaceALL_Click);
			
			cmdCancel.Text="Cancel";
			cmdCancel.DialogResult=DialogResult.Cancel;
			cmdCancel.Click+=new EventHandler(Cancel_Click);
			
			Form frmDialog=new Form();
			frmDialog.Size=new Size(380,200);
			frmDialog.Text="Replace";
			frmDialog.MaximizeBox=false;
			frmDialog.MinimizeBox=false;
			frmDialog.FormBorderStyle=FormBorderStyle.FixedDialog;
			
			lblFind.Location=new Point(5,15);
			lblFind.Text="Find:";
			lblFind.AutoSize=true;
			
			txtFind.Location=new Point(lblFind.Left+lblFind.Width+5,lblFind.Top);
			txtFind.Size=new Size(200,txtFind.Height);
			txtFind.Text=TextToFind;
			txtFind.TextChanged+=new EventHandler(txtFind_Change);
			
			lblReplace.Location=new Point(lblFind.Left,lblFind.Top+lblFind.Height+15);
			lblReplace.Text="Replace:";
			lblReplace.AutoSize=true;
			
			txtReplace.Location=new Point(lblReplace.Left+lblReplace.Width+5,lblReplace.Top);
			txtReplace.Size=new Size(200,txtFind.Height);
			txtFind.Left=txtReplace.Left;
			txtReplace.Text=TextToReplace;
			txtReplace.TextChanged+=new EventHandler(txtReplace_Change);
			
			
			cmdOK.Location=new Point(txtFind.Left+txtFind.Width+5,lblFind.Top-2);
			cmdReplaceALL.Location=new Point(cmdOK.Left,cmdOK.Top+cmdOK.Height+5);
			cmdCancel.Location=new Point(cmdReplaceALL.Left,cmdReplaceALL.Top+cmdReplaceALL.Height+5);
			
			grpDirection.Location=new Point(lblReplace.Left,cmdCancel.Top+cmdCancel.Height);
			grpDirection.Size=new Size(120,45);
			grpDirection.Text="Match Direction";
			
			optUp.Location=new Point(10,15);
			optUp.Click+=new EventHandler(Direction_Click);
			optUp.Text="&Up";
			optUp.Width=45;
			
			optDown.Text="&Down";
			optDown.Width=55;
			optDown.Click+=new EventHandler(Direction_Click);
			optDown.Checked=true;
			optDown.Location=new Point(optUp.Left+optUp.Width,optUp.Top);
			
			grpDirection.Controls.Add(optUp);
			grpDirection.Controls.Add(optDown);
			
			chkMatch.Location=new Point(grpDirection.Left+grpDirection.Width+5,grpDirection.Top+(grpDirection.Height-chkMatch.Height)/2);
			chkMatch.Text="Match Case";
			
			if(txtFind.Text.Trim().Length==0)
			{
				cmdOK.Enabled=false;
				cmdReplaceALL.Enabled=false;
			}
			
			frmDialog.Controls.Add(txtFind);
			frmDialog.Controls.Add(lblFind);
			frmDialog.Controls.Add(txtReplace);
			frmDialog.Controls.Add(lblReplace);
			frmDialog.Controls.Add(grpDirection);
			//frmDialog.Controls.Add(chkMatch);
			frmDialog.Controls.Add(cmdOK);
			frmDialog.Controls.Add(cmdReplaceALL);
			frmDialog.Controls.Add(cmdCancel);
			frmDialog.ShowInTaskbar=false;
			frmDialog.TopMost=true;
			frmDialog.Show();
		}
		
		private void txtFind_Change(object sender,EventArgs eArgs)
		{
			TextToFind=((TextBox)sender).Text;
			Form frmTemp=(Form)(((Control)sender).Parent);
			for(int i=0;i<frmTemp.Controls.Count;i++)
			{
				if(frmTemp.Controls[i].GetType() ==typeof(Button) && frmTemp.Controls[i].Text!="&Cancel")
				{
					frmTemp.Controls[i].Enabled=(((TextBox)sender).Text.Length>0);
				}
			}
			frmTemp=null;
		}
		
		private void txtReplace_Change(object sender,EventArgs eArgs)
		{
			TextToReplace=((TextBox)sender).Text;
		}
		
		private void Direction_Click(object sender,EventArgs eArgs)
		{
			string Caption=((RadioButton)sender).Text;
			if(Caption=="&Up")
				IsDirectionDownward=false;
			else
				IsDirectionDownward=true;
		}
		
		private void Find_Click(object sender,EventArgs eArgs)
		{
			int Pos=-1;
			
			if(IsDirectionDownward)
			{
				if(txtEditor.SelectionLength==0)
					Pos=txtEditor.Text.IndexOf(TextToFind,txtEditor.SelectionStart);
				else
					Pos=txtEditor.Text.IndexOf(TextToFind,txtEditor.SelectionStart+txtEditor.SelectionLength);
			}
			else
			{
				if(txtEditor.SelectionStart>0)
					Pos=txtEditor.Text.LastIndexOf(TextToFind,txtEditor.SelectionStart-1);
			}
			if(Pos!=-1)
			{
				txtEditor.SelectionStart=Pos;
				txtEditor.SelectionLength=TextToFind.Length;
				if(sender!=null) ((Control)sender).Focus();
			}
			else
			{
				MessageBox.Show("Cannot Find: \"" + TextToFind + "\"");
			}
		}
		
		private void Replace_Click(object sender,EventArgs eArgs)
		{
			Find_Click(null,null);
			if(txtEditor.SelectionLength>0)
			{
				txtEditor.SelectedText=TextToReplace;
				txtEditor.SelectionStart=txtEditor.SelectionStart+TextToReplace.Length;
			}
		}
		
		private void ReplaceALL_Click(object sender,EventArgs eArgs)
		{
			txtEditor.Text=Replace(txtEditor.Text,TextToFind,TextToReplace);
		}
		
		private string Replace(string StrSource,string StrFind,string StrReplace)
		{
			int iPos=StrSource.IndexOf(StrFind);
			String StrReturn="";
			
			while(iPos!=-1)
			{
				StrReturn+=StrSource.Substring(0,iPos)+StrReplace;
				StrSource=StrSource.Substring(iPos+StrFind.Length);
				iPos=StrSource.IndexOf(StrFind);
			}
			if(StrSource.Length>0)
				StrReturn+=StrSource;
			return StrReturn;
		}
		
		private void Cancel_Click(object sender,EventArgs eArgs)
		{
			((Form)((Control)sender).Parent).Close();
		}
		
	}
	
}

