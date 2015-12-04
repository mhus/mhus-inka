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
 * Datum: 04.03.2008
 * Zeit: 18:40
 * 
 * Sie können diese Vorlage unter Extras > Optionen > Codeerstellung > Standardheader ändern.
 */

using System;
using System.Drawing;
using System.Windows.Forms;

namespace CryptPad
{
	/// <summary>
	/// Description of Form2.
	/// </summary>
	public partial class Form2 : Form
	{
		public Form2()
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
			this.Hide();
		}
		
		public string getPass() {
			return pass.Text;
		}

		
		void PassKeyPress(object sender, KeyPressEventArgs e)
		{
			if ( e.KeyChar == '\r' ) {
				BOkClick( null, null );
			}
		}
	}
}
