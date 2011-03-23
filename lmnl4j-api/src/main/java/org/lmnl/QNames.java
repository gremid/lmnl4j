package org.lmnl;

import java.net.URI;
import java.util.Comparator;

import com.google.common.base.Objects;

public class QNames {
	public static final Comparator<QName> COMPARATOR = new Comparator<QName>() {

		public int compare(QName o1, QName o2) {
			final URI o1Ns = o1.getNamespace();
			final URI o2Ns = o2.getNamespace();

			final String o1LocalName = o1.getLocalName();
			final String o2LocalName = o2.getLocalName();

			if (o1Ns != null && o2Ns != null) {
				final int nsComp = o1Ns.compareTo(o2Ns);
				return (nsComp == 0 ? o1LocalName.compareTo(o2LocalName) : nsComp);
			} else if (o1Ns == null && o2Ns == null) {
				return o1LocalName.compareTo(o2LocalName);
			} else {
				return (o1Ns == null ? 1 : -1);
			}
		}
	};

	public static boolean equal(QName name1, QName name2) {
		return Objects.equal(name1.getLocalName(), name2.getLocalName())
				&& Objects.equal(name1.getNamespace(), name2.getNamespace());
	}
	
	public static int hashCode(QName name) {
		return Objects.hashCode(name.getLocalName(), name.getNamespace());
	}
	
	public static String toString(QName name) {
		final URI ns = name.getNamespace();
		return "{" + (ns == null ? "" : ns) + "}" + name.getLocalName();
	}


}
