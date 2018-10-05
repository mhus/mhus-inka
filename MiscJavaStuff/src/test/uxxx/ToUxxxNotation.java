package test.uxxx;

public class ToUxxxNotation {

	public static void main(String[] args) {
		String x = "!àáâäãåąčćęèéêëėįìíîïłńòóôöõøùúûüųūÿýżźñçčšžÀÁÂÄÃÅĄĆČĖĘÈÉÊËÌÍÎÏĮŁŃÒÓÔÖÕØÙÚÛÜŲŪŸÝŻŹÑßÇŒÆČŠŽ∂ð";
		for (int i = 0; i < x.length(); i++) {
			String o = Integer.toUnsignedString(x.charAt(i), 16);
			while (o.length() < 4) o = '0' + o;
			System.out.print("\\u" + o );
		}
		System.out.println();
		
		String y = "\u00e0\u00e1\u00e2\u00e4\u00e3\u00e5\u0105\u010d\u0107\u0119\u00e8\u00e9\u00ea\u00eb\u0117\u012f\u00ec\u00ed\u00ee\u00ef\u0142\u0144\u00f2\u00f3\u00f4\u00f6\u00f5\u00f8\u00f9\u00fa\u00fb\u00fc\u0173\u016b\u00ff\u00fd\u017c\u017a\u00f1\u00e7\u010d\u0161\u017e\u00c0\u00c1\u00c2\u00c4\u00c3\u00c5\u0104\u0106\u010c\u0116\u0118\u00c8\u00c9\u00ca\u00cb\u00cc\u00cd\u00ce\u00cf\u012e\u0141\u0143\u00d2\u00d3\u00d4\u00d6\u00d5\u00d8\u00d9\u00da\u00db\u00dc\u0172\u016a\u0178\u00dd\u017b\u0179\u00d1\u00df\u00c7\u0152\u00c6\u010c\u0160\u017d\u2202\u00f0";
		
		System.out.println("Equals: "  + x.equals(y));
	}

}
