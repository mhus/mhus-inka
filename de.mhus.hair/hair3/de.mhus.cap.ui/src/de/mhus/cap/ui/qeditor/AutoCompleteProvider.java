package de.mhus.cap.ui.qeditor;

import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.swt.widgets.Text;

import de.mhus.lib.MString;

public class AutoCompleteProvider implements IAutoCompleteProvider {

	private static final String[] SEPARATORS = new String[] { " ", "\t", "\n", ",", "-", "+", "(", ")" };
	private String[] values;

	public AutoCompleteProvider(String[] values) {
		this.values = values;
		Arrays.sort(this.values, String.CASE_INSENSITIVE_ORDER);
	}
	
	@Override
	public String[] getValues(Text owner) {
		
		String text = owner.getText();
		int caret = owner.getCaretPosition();
		
		text = MString.getSelection(text, MString.getSelectedPart(text, caret, SEPARATORS ), null);
		if (MString.isEmptyTrim(text))
			return this.values;
		
		LinkedList<String> out = new LinkedList<String>();
		for (String value : values) {
			if (value.startsWith(text))
				out.add(value);
		}
		
		return out.toArray(new String[out.size()]);
	}

	@Override
	public void doAutoCompleate(Text owner, String selection) {
		String text = owner.getText();
		int caret = owner.getCaretPosition();
		
		int[] pos = MString.getSelectedPart(text, caret, SEPARATORS );
		if (pos == null) {
			owner.setText(text);
			owner.setSelection(0, text.length());
			return;
		}
		
		text = MString.replaceSelection(text, pos, selection);
		if (text == null) return;
		
		owner.setText(text);
		owner.setSelection(pos[0], pos[0] + selection.length());
	}

}
