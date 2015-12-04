/*
 * Erstellt mit SharpDevelop.
 * Benutzer: mike
 * Datum: 04.03.2008
 * Zeit: 18:40
 * 
 * Sie können diese Vorlage unter Extras > Optionen > Codeerstellung > Standardheader ändern.
 */
namespace CryptPad
{
	partial class Form2
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
			this.pass = new System.Windows.Forms.TextBox();
			this.bOk = new System.Windows.Forms.Button();
			this.SuspendLayout();
			// 
			// pass
			// 
			this.pass.AcceptsReturn = true;
			this.pass.Location = new System.Drawing.Point(12, 12);
			this.pass.Name = "pass";
			this.pass.PasswordChar = '*';
			this.pass.Size = new System.Drawing.Size(311, 20);
			this.pass.TabIndex = 0;
			this.pass.UseSystemPasswordChar = true;
			this.pass.KeyPress += new System.Windows.Forms.KeyPressEventHandler(this.PassKeyPress);
			// 
			// bOk
			// 
			this.bOk.Location = new System.Drawing.Point(180, 50);
			this.bOk.Name = "bOk";
			this.bOk.Size = new System.Drawing.Size(143, 23);
			this.bOk.TabIndex = 1;
			this.bOk.Text = "OK";
			this.bOk.UseVisualStyleBackColor = true;
			this.bOk.Click += new System.EventHandler(this.BOkClick);
			// 
			// Form2
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(335, 85);
			this.ControlBox = false;
			this.Controls.Add(this.bOk);
			this.Controls.Add(this.pass);
			this.MaximizeBox = false;
			this.MinimizeBox = false;
			this.Name = "Form2";
			this.ShowIcon = false;
			this.ShowInTaskbar = false;
			this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Hide;
			this.Text = "Password";
			this.ResumeLayout(false);
			this.PerformLayout();
		}
		private System.Windows.Forms.Button bOk;
		private System.Windows.Forms.TextBox pass;
	}
}
