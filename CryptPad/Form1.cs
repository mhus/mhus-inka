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
 * Erstellt mit SharpDevelop.
 * Benutzer: mike
 * Datum: 28.02.2008
 * Zeit: 14:10
 * 
 * Sie können diese Vorlage unter Extras > Optionen > Codeerstellung > Standardheader ändern.
 */

using System;
using System.Drawing;
using System.Windows.Forms;

namespace CryptPad
{
	/// <summary>
	/// Description of Form1.
	/// </summary>
	public partial class Form1 : Form
	{
		public bool isCancel = true;
		
		public Form1()
		{
			//
			// The InitializeComponent() call is required for Windows Forms designer support.
			//
			InitializeComponent();
			
			//
			// TODO: Add constructor code after the InitializeComponent() call.
			//
		}
		
		void BOkClick(object sender, EventArgs e)
		{
			isCancel = false;
			if ( pass1.Text != pass2.Text ) isCancel = true;
			this.Hide();
		}
		
		void BCancelClick(object sender, EventArgs e)
		{
			isCancel = true;
			this.Hide();
		}
		
		public string getPass() {
			return pass1.Text;
		}
		
		public void setPass( string pass ) {
			if ( pass == null ) return;
			pass1.Text = pass;
			pass2.Text = pass;
			pass1.SelectionStart=pass.Length;
			pass1.SelectionLength=0;
			pass2.SelectionStart=pass.Length;
			pass2.SelectionLength=0;			
		}
		
		
		void Pass1KeyPress(object sender, KeyPressEventArgs e)
		{
			if ( e.KeyChar == '\r' )
				pass2.Focus();
		}
		
		void Pass2KeyPress(object sender, KeyPressEventArgs e)
		{
			if ( e.KeyChar == '\r' )
				BOkClick( null, null );
		}
	}
}
