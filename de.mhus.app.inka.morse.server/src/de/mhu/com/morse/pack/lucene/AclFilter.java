package de.mhu.com.morse.pack.lucene;

import java.io.IOException;
import java.util.BitSet;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.Filter;

import de.mhu.com.morse.aaa.AclManager;
import de.mhu.com.morse.aaa.IAclManager;
import de.mhu.com.morse.types.IAttribute;
import de.mhu.com.morse.usr.UserInformation;

public class AclFilter extends Filter {

	private UserInformation user;
	private IAclManager aclManager;

	public AclFilter(IAclManager pAclManager, UserInformation pUser) {
		aclManager = pAclManager;
		user = pUser;
	}

	@Override
	public BitSet bits(IndexReader reader) throws IOException {
		BitSet bits = new BitSet(reader.maxDoc());
		
		TermDocs termDocs = reader.termDocs();
		TermEnum enumerator = reader.terms( new Term( IAttribute.M_ACL, "" ) );
		try {
			do {
                Term term = enumerator.term();
                if (term != null && term.field().equals(IAttribute.M_ACL)) {
                	if ( aclManager.hasRead( user,  term.text() ) ) {
                		termDocs.seek(enumerator.term());
                        while (termDocs.next()) {
                            bits.set(termDocs.doc());
                        }
                	}
                }
			}
            while (enumerator.next());
			
		} finally {
			enumerator.close();
			termDocs.close();
		}
		return bits;
	}

}
