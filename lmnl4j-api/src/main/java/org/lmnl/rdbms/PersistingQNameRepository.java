package org.lmnl.rdbms;

import java.net.URI;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.lmnl.QName;
import org.lmnl.QNameImpl;
import org.lmnl.QNameRepository;

import com.google.common.collect.MapMaker;

public class PersistingQNameRepository implements QNameRepository {

	private Map<PersistentQName, Integer> nameCache;
	private int cacheSize = 1000;
	private SessionFactory sessionFactory;

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public QName get(QName name) {
		if (nameCache == null) {
			nameCache = new MapMaker().maximumSize(cacheSize).makeMap();
		}

		final Session session = sessionFactory.getCurrentSession();
		final Integer nameId = nameCache.get(name);
		if (nameId != null) {
			return (QName) session.get(PersistentQName.class, nameId);
		}

		final Criteria c = addRestrictions(session.createCriteria(PersistentQName.class), name.getNamespace(),
				name.getLocalName());

		PersistentQName persistentQName = (PersistentQName) c.uniqueResult();
		if (persistentQName == null) {
			session.save(persistentQName = new PersistentQName(name.getNamespace(), name.getLocalName()));
		}

		nameCache.put(persistentQName, persistentQName.getId());
		return persistentQName;
	}

	public synchronized QName get(URI namespace, String localName) {
		return get(new QNameImpl(namespace, localName));
	}

	static Criteria addRestrictions(Criteria c, URI namespace, String localName) {
		c.add(namespace == null ? Restrictions.isNull("namespace") : Restrictions.eq("namespace", namespace));
		c.add(Restrictions.eq("localName", localName));
		return c;
	}

}
