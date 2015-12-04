/*
 * Erstellt mit SharpDevelop.
 * Benutzer: mike
 * Datum: 28.02.2008
 * Zeit: 14:10
 * 
 * Sie können diese Vorlage unter Extras > Optionen > Codeerstellung > Standardheader ändern.
 */
namespace CryptPad
{
	partial class Form1
	{
		/// <summary>
		/// Designer variable used to keep track of non-visual components.
		/// </summary>
		private System.ComponentModel.IContainer components = null;
		
		/// <summary>
		/// Disposes resources used by the form.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing) {
				if (components != null) {
					components.Dispose();
				}
			}
			base.Dispose(disposing);
		}
		
		/// <summary>
		/// This method is required for Windows Forms designer support.
		/// Do not change the method contents inside the source code editor. The Forms designer might
		/// not be able to load this method if it was changed manually.
		/// </summary>
		private void InitializeComponent()
		{
			this.bOk = new System.Windows.Forms.Button();
			this.pass1 = new System.Windows.Forms.MaskedTextBox();
			this.pass2 = new System.Windows.Forms.MaskedTextBox();
			this.bCancel = new System.Windows.Forms.Button();
			this.SuspendLayout();
			// 
			// bOk
			// 
			this.bOk.Location = new System.Drawing.Point(215, 64);
			this.bOk.Name = "bOk";
			this.bOk.Size = new System.Drawing.Size(129, 23);
			this.bOk.TabIndex = 3;
			this.bOk.Text = "Ok";
			this.bOk.UseVisualStyleBackColor = true;
			this.bOk.Click += new System.EventHandler(this.BOkClick);
			// 
			// pass1
			// 
			this.pass1.Location = new System.Drawing.Point(12, 12);
			this.pass1.Name = "pass1";
			this.pass1.PasswordChar = '*';
			this.pass1.Size = new System.Drawing.Size(332, 20);
			this.pass1.TabIndex = 0;
			this.pass1.UseSystemPasswordChar = true;
			this.pass1.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.Pass1KeyPress);
			// 
			// pass2
			// 
			this.pass2.Location = new System.Drawing.Point(12, 38);
			this.pass2.Name = "pass2";
			this.pass2.PasswordChar = '*';
			this.pass2.Size = new System.Drawing.Size(332, 20);
			this.pass2.TabIndex = 1;
			this.pass2.UseSystemPasswordChar = true;
			this.pass2.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.Pass2KeyPress);
			// 
			// bCancel
			// 
			this.bCancel.Location = new System.Drawing.Point(12, 64);
			this.bCancel.Name = "bCancel";
			this.bCancel.Size = new System.Drawing.Size(111, 23);
			this.bCancel.TabIndex = 2;
			this.bCancel.Text = "Cancel";
			this.bCancel.UseVisualStyleBackColor = true;
			this.bCancel.Click += new System.EventHandler(this.BCancelClick);
			// 
			// Form1
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(356, 98);
			this.Controls.Add(this.bCancel);
			this.Controls.Add(this.pass2);
			this.Controls.Add(this.pass1);
			this.Controls.Add(this.bOk);
			this.MaximizeBox = false;
			this.MinimizeBox = false;
			this.Name = "Form1";
			this.ShowIcon = false;
			this.ShowInTaskbar = false;
			this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Hide;
			this.Text = "Insert Password";
			this.ResumeLayout(false);
			this.PerformLayout();
		}
		private System.Windows.Forms.Button bCancel;
		private System.Windows.Forms.MaskedTextBox pass2;
		private System.Windows.Forms.MaskedTextBox pass1;
		private System.Windows.Forms.Button bOk;
	}
}
