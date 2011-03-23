package org.lmnl.rdbms;

import java.net.URI;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.lmnl.QName;
import org.lmnl.QNameRepository;

import com.google.common.collect.MapMaker;

public class RelationalQNameRepository implements QNameRepository {

	private Map<QNameRelation, Integer> nameCache;
	private int cacheSize = 1000;
	private SessionFactory sessionFactory;

	public void setCacheSize(int cacheSize) {
		this.cacheSize = cacheSize;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public synchronized QName get(QName name) {
		if (nameCache == null) {
			nameCache = new MapMaker().maximumSize(cacheSize).makeMap();
		}

		final Session session = sessionFactory.getCurrentSession();
		final Integer nameId = nameCache.get(name);
		if (nameId != null) {
			QName loaded = (QName) session.get(QNameRelation.class, nameId);
			if (loaded != null) {
				return loaded;
			}
		}

		final Criteria c = addRestrictions(session.createCriteria(QNameRelation.class), name.getNamespace(),
				name.getLocalName());

		QNameRelation qNameRelation = (QNameRelation) c.uniqueResult();
		if (qNameRelation == null) {
			session.save(qNameRelation = new QNameRelation(name.getNamespace(), name.getLocalName()));
		}

		nameCache.put(qNameRelation, qNameRelation.getId());
		return qNameRelation;
	}
	
	public synchronized void clearCache() {
		nameCache = null;
	}
	
	static Criteria addRestrictions(Criteria c, URI namespace, String localName) {
		c.add(namespace == null ? Restrictions.isNull("namespace") : Restrictions.eq("namespace", namespace));
		c.add(Restrictions.eq("localName", localName));
		return c;
	}

}
