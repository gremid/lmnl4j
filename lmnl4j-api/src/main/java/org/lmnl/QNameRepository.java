package org.lmnl;

import java.net.URI;

public interface QNameRepository {

	QName get(URI namespace, String localName);
}
